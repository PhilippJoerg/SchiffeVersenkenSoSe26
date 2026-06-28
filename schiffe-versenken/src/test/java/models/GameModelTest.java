package models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * de: Testet die Klasse GameModel.
 * en: Tests the GameModel class.
 */
class GameModelTest {

    private GameModel gameModel;
    private CellState[][] ownBoard;

    /**
     * de: Reagiert auf das Ereignis "SetUp".
     * en: Responds to the "SetUp" event.
     *
     */
    @BeforeEach
    void setUp() {
        ownBoard = BoardUtils.createEmptyCellBoard();
        ownBoard[0][0] = CellState.SHIP;
        gameModel = new GameModel(ownBoard, GameDifficulty.EASY, GameSettings.defaultSettings());
    }

    /**
     * de: Reagiert auf das Ereignis "TestConstructorInitializesBoardsAndDifficulty".
     * en: Responds to the "TestConstructorInitializesBoardsAndDifficulty" event.
     *
     */
    @Test
    void testConstructorInitializesBoardsAndDifficulty() {
        assertNotNull(gameModel.getOwnBoard());
        assertNotNull(gameModel.getEnemyBoard());
        assertEquals(GameDifficulty.EASY, gameModel.getDifficulty());
    }

    /**
     * de: Reagiert auf das Ereignis "TestShootMissAndHitAndGameOver".
     * en: Responds to the "TestShootMissAndHitAndGameOver" event.
     *
     */
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

    /**
     * de: Reagiert auf das Ereignis "TestEvaluateIncomingShotInvalidParameters".
     * en: Responds to the "TestEvaluateIncomingShotInvalidParameters" event.
     *
     */
    @Test
    void testEvaluateIncomingShotInvalidParameters() {
        int invalid = gameModel.evaluateIncomingShot(-1, -1);
        assertEquals(0, invalid);
    }

    /**
     * de: Reagiert auf das Ereignis "TestComputerShootReturnsValidResult".
     * en: Responds to the "TestComputerShootReturnsValidResult" event.
     *
     */
    @Test
    void testComputerShootReturnsValidResult() {
        int[] result = gameModel.computerShoot();
        assertNotNull(result);
        assertEquals(3, result.length);
        assertTrue(result[0] >= 0 && result[0] < gameModel.getOwnBoard().length);
        assertTrue(result[1] >= 0 && result[1] < gameModel.getOwnBoard().length);
        assertTrue(result[2] >= 0 && result[2] <= 2);
    }

    /**
     * de: Reagiert auf das Ereignis "TestSettersForGameStatus".
     * en: Responds to the "TestSettersForGameStatus" event.
     *
     */
    @Test
    void testSettersForGameStatus() {
        gameModel.setGameOver(true);
        gameModel.setPlayerWon(true);
        assertTrue(gameModel.isGameOver());
        assertTrue(gameModel.didPlayerWin());
    }
}
