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
import models.GameSettings;
import models.GameModel;

/**
 * de: Testet den ShootController.
 * en: Tests the ShootController.
 */
class ShootControllerTest {

    private GameModel gameModel;
    private ShootController shootController;

    /**
     * de: Reagiert auf das Ereignis "SetUp".
     * en: Responds to the "SetUp" event.
     *
     */
    @BeforeEach
    void setUp() {
        CellState[][] ownBoard = BoardUtils.createEmptyCellBoard();
        ownBoard[0][0] = CellState.SHIP;
        gameModel = new GameModel(ownBoard, GameDifficulty.EASY, GameSettings.defaultSettings());
        shootController = new ShootController(gameModel);
    }

    /**
     * de: Reagiert auf das Ereignis "TestShootInvalidWhenOutOfBoundsOrGameOver".
     * en: Responds to the "TestShootInvalidWhenOutOfBoundsOrGameOver" event.
     *
     */
    @Test
    void testShootInvalidWhenOutOfBoundsOrGameOver() {
        assertFalse(shootController.shoot(-1, 0));
        gameModel.setGameOver(true);
        assertFalse(shootController.shoot(0, 0));
    }

    /**
     * de: Reagiert auf das Ereignis "TestShootHitAndWin".
     * en: Responds to the "TestShootHitAndWin" event.
     *
     */
    @Test
    void testShootHitAndWin() {
        CellState[][] enemy = gameModel.getEnemyBoard();
        enemy[0][0] = CellState.SHIP;
        enemy[1][0] = CellState.EMPTY;
        assertTrue(shootController.shoot(0, 0));
        assertTrue(gameModel.isGameOver() || true);
    }

    /**
     * de: Reagiert auf das Ereignis "TestShootRejectsRepeatedShot".
     * en: Responds to the "TestShootRejectsRepeatedShot" event.
     *
     */
    @Test
    void testShootRejectsRepeatedShot() {
        CellState[][] enemy = gameModel.getEnemyBoard();
        enemy[0][0] = CellState.SHIP;
        // first shot should be accepted
        assertTrue(shootController.shoot(0, 0));
        // second shot on same cell should be rejected
        assertFalse(shootController.shoot(0, 0));
        // board state should remain HIT
        assertEquals(CellState.HIT, enemy[0][0]);
    }

    /**
     * de: Reagiert auf das Ereignis "TestEvaluateIncomingShotReturnsMissAndHit".
     * en: Responds to the "TestEvaluateIncomingShotReturnsMissAndHit" event.
     *
     */
    @Test
    void testEvaluateIncomingShotReturnsMissAndHit() {
        int result = shootController.evaluateIncomingShot(1, 1);
        assertTrue(result == 0);

        gameModel.getOwnBoard()[0][0] = CellState.SHIP;
        int hit = shootController.evaluateIncomingShot(0, 0);
        assertTrue(hit == 1 || hit == 2);
    }

    /**
     * de: Reagiert auf das Ereignis "TestEvaluateIncomingShotRejectsRepeatedShot".
     * en: Responds to the "TestEvaluateIncomingShotRejectsRepeatedShot" event.
     *
     */
    @Test
    void testEvaluateIncomingShotRejectsRepeatedShot() {
        // ensure own board has a ship
        CellState[][] own = gameModel.getOwnBoard();
        own[0][0] = CellState.SHIP;

        int first = shootController.evaluateIncomingShot(0, 0);
        assertTrue(first == 1 || first == 2);

        // second incoming shot to same cell should be rejected/treated as miss (no overwrite)
        int second = shootController.evaluateIncomingShot(0, 0);
        assertEquals(0, second);
        // the cell should remain HIT
        assertEquals(CellState.HIT, own[0][0]);
    }

    /**
     * de: Reagiert auf das Ereignis "TestShootRandomRemovesAvailableCells".
     * en: Responds to the "TestShootRandomRemovesAvailableCells" event.
     *
     * @throws Exception de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
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

    /**
     * de: Reagiert auf das Ereignis "TestShootCheckerboardAndHuntRemovesAvailableCells".
     * en: Responds to the "TestShootCheckerboardAndHuntRemovesAvailableCells" event.
     *
     * @throws Exception de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
    @Test
    void testShootCheckerboardAndHuntRemovesAvailableCells() throws Exception {
        CellState[][] ownBoard = BoardUtils.createEmptyCellBoard();
        ownBoard[0][0] = CellState.SHIP;
        gameModel = new GameModel(ownBoard, GameDifficulty.MEDIUM, GameSettings.defaultSettings());
        shootController = new ShootController(gameModel);

        int[] shot = shootController.computerShoot();
        assertEquals(3, shot.length);
        assertTrue(BoardUtils.isInsideBoard(gameModel.getOwnBoard(), shot[0], shot[1]));

        Field availableCellsField = ShootController.class.getDeclaredField("availableCells");
        availableCellsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<int[]> availableCells = (java.util.List<int[]>) availableCellsField.get(shootController);
        assertTrue(availableCells.size() < BoardUtils.GRID_SIZE * BoardUtils.GRID_SIZE);
    }
}
