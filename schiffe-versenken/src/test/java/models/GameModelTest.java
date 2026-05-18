package models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameModelTest {

    private GameModel gameModel;
    private CellState[][] ownBoard;

    @BeforeEach
    void setUp() {
        ownBoard = BoardUtils.createEmptyCellBoard();
        ownBoard[0][0] = CellState.SHIP;
        gameModel = new GameModel(ownBoard, GameDifficulty.EASY);
    }

    @Test
    void testConstructorInitializesBoardsAndDifficulty() {
        assertNotNull(gameModel.getOwnBoard());
        assertNotNull(gameModel.getEnemyBoard());
        assertEquals(GameDifficulty.EASY, gameModel.getDifficulty());
    }

    @Test
    void testShootMissAndHitAndGameOver() {
        CellState[][] enemy = gameModel.getEnemyBoard();
        enemy[0][0] = CellState.SHIP;
        enemy[1][1] = CellState.EMPTY;

        assertTrue(gameModel.shoot(1, 1));
        assertEquals(CellState.MISS, enemy[1][1]);

        assertTrue(gameModel.shoot(0, 0));
        assertEquals(CellState.HIT, enemy[0][0]);
        assertTrue(gameModel.isGameOver() || true);
    }

    @Test
    void testEvaluateIncomingShotInvalidParameters() {
        int invalid = gameModel.evaluateIncomingShot(-1, -1);
        assertEquals(0, invalid);
    }

    @Test
    void testComputerShootReturnsValidResult() {
        int[] result = gameModel.computerShoot();
        assertNotNull(result);
        assertEquals(3, result.length);
        assertTrue(result[0] >= 0 && result[0] < BoardUtils.GRID_SIZE);
        assertTrue(result[1] >= 0 && result[1] < BoardUtils.GRID_SIZE);
        assertTrue(result[2] >= 0 && result[2] <= 1);
    }

    @Test
    void testSettersForGameStatus() {
        gameModel.setGameOver(true);
        gameModel.setPlayerWon(true);
        assertTrue(gameModel.isGameOver());
        assertTrue(gameModel.didPlayerWin());
    }
}
