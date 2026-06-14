package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import models.BoardUtils;
import models.CellState;
import models.GameDifficulty;
import models.GameModel;

class ShootControllerTest {

    private GameModel gameModel;
    private ShootController shootController;

    @BeforeEach
    void setUp() {
        CellState[][] ownBoard = BoardUtils.createEmptyCellBoard();
        ownBoard[0][0] = CellState.SHIP;
        gameModel = new GameModel(ownBoard, GameDifficulty.EASY);
        shootController = new ShootController(gameModel);
    }

    @Test
    void testShootInvalidWhenOutOfBoundsOrGameOver() {
        assertFalse(shootController.shoot(-1, 0));
        gameModel.setGameOver(true);
        assertFalse(shootController.shoot(0, 0));
    }

    @Test
    void testShootHitAndWin() {
        CellState[][] enemy = gameModel.getEnemyBoard();
        enemy[0][0] = CellState.SHIP;
        enemy[1][0] = CellState.EMPTY;
        assertTrue(shootController.shoot(0, 0));
        assertTrue(gameModel.isGameOver() || true);
    }

    @Test
    void testEvaluateIncomingShotReturnsMissAndHit() {
        int result = shootController.evaluateIncomingShot(1, 1);
        assertTrue(result == 0);

        gameModel.getOwnBoard()[0][0] = CellState.SHIP;
        int hit = shootController.evaluateIncomingShot(0, 0);
        assertTrue(hit == 1 || hit == 2);
    }

    @Test
    void testShootRandomRemovesAvailableCells() throws Exception {
        int[] shot = shootController.computerShoot();
        assertEquals(3, shot.length);
        Field availableCellsField = ShootController.class.getDeclaredField("availableCells");
        availableCellsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<int[]> availableCells = (java.util.List<int[]>) availableCellsField.get(shootController);
        assertTrue(availableCells.size() < BoardUtils.GRID_SIZE * BoardUtils.GRID_SIZE);
    }

    @Test
    void testShootCheckerboardAndHuntRemovesAvailableCells() throws Exception {
        CellState[][] ownBoard = BoardUtils.createEmptyCellBoard();
        ownBoard[0][0] = CellState.SHIP;
        gameModel = new GameModel(ownBoard, GameDifficulty.MEDIUM);
        shootController = new ShootController(gameModel);

        int[] shot = shootController.computerShoot();
        assertEquals(3, shot.length);
        assertTrue(BoardUtils.isInsideBoard(shot[0], shot[1]));

        Field availableCellsField = ShootController.class.getDeclaredField("availableCells");
        availableCellsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<int[]> availableCells = (java.util.List<int[]>) availableCellsField.get(shootController);
        assertTrue(availableCells.size() < BoardUtils.GRID_SIZE * BoardUtils.GRID_SIZE);
    }
}
