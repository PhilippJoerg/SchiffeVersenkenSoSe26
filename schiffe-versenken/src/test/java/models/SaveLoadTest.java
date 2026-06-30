package models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

/**
 * de: Testet die Klasse SaveLoad.
 * en: Tests the SaveLoad class.
 */
class SaveLoadTest {

    /**
     * de: Reagiert auf das Ereignis "TestSaveLoadRoundTrip".
     * en: Responds to the "TestSaveLoadRoundTrip" event.
     *
     * @throws Exception de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
    @Test
    void testSaveLoadRoundTrip() throws Exception {
        CellState[][] own = BoardUtils.createEmptyCellBoard();
        own[0][0] = CellState.SHIP;
        CellState[][] enemy = BoardUtils.createEmptyCellBoard();
        enemy[1][1] = CellState.SHIP;

        GameModel model = new GameModel(own, enemy, GameDifficulty.MEDIUM, false, false);

        File tmp = File.createTempFile("game-save-test", ".json");
        tmp.deleteOnExit();

        SaveLoad.saveGame(tmp, model);

        GameModel loaded = SaveLoad.loadGame(tmp);

        // compare boards
        CellState[][] loadedOwn = loaded.getOwnBoard();
        CellState[][] loadedEnemy = loaded.getEnemyBoard();

        for (int c = 0; c < BoardUtils.GRID_SIZE; c++) {
            for (int r = 0; r < BoardUtils.GRID_SIZE; r++) {
                assertEquals(own[c][r], loadedOwn[c][r]);
                assertEquals(enemy[c][r], loadedEnemy[c][r]);
            }
        }

        assertEquals(model.getDifficulty(), loaded.getDifficulty());
        assertEquals(model.isGameOver(), loaded.isGameOver());
        assertEquals(model.didPlayerWin(), loaded.didPlayerWin());
    }
}
