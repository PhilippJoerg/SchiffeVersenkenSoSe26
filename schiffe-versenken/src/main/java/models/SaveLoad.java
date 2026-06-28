package models;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * de: Utility zum Speichern und Laden des Spielzustands als JSON.
 * en: Utility to save and load game state as JSON.
 */
public class SaveLoad {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * de: Speichert den aktuellen Spielzustand in einer Datei.
     * en: Saves the current game state to a file.
     *
     * @param file de: Parameter file. en: Parameter file.
     * @param model de: Parameter model. en: Parameter model.
     * @throws IOException de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
    public static void saveGame(File file, GameModel model) throws IOException {
        GameState gs = new GameState();
        gs.difficulty = model.getDifficulty() != null ? model.getDifficulty().name() : null;
        gs.gameOver = model.isGameOver();
        gs.playerWon = model.didPlayerWin();
        gs.ownBoard = toStringGrid(model.getOwnBoard());
        gs.enemyBoard = toStringGrid(model.getEnemyBoard());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, gs);
    }

    /**
     * de: Lädt den Spielzustand aus einer Datei.
     * en: Loads the game state from a file.
     *
     * @param file de: Parameter file. en: Parameter file.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     * @throws IOException de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
    public static GameModel loadGame(File file) throws IOException {
        GameState gs = MAPPER.readValue(file, GameState.class);
        CellState[][] own = fromStringGrid(gs.ownBoard);
        CellState[][] enemy = fromStringGrid(gs.enemyBoard);
        GameDifficulty difficulty = gs.difficulty != null ? GameDifficulty.valueOf(gs.difficulty) : GameDifficulty.EASY;
        return new GameModel(own, enemy, difficulty, gs.gameOver, gs.playerWon);
    }

    /**
     * de: Wandelt ein Spielfeld in ein String-Array um.
     * en: Converts a game board to a string array.
     *
     * @param board de: Parameter board. en: Parameter board.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
    private static String[][] toStringGrid(CellState[][] board) {
        if (board == null) {
            return null;
        }
        int size = board.length;
        String[][] res = new String[size][size];
        for (int c = 0; c < size; c++) {
            for (int r = 0; r < size; r++) {
                res[c][r] = board[c][r] != null ? board[c][r].name() : null;
            }
        }
        return res;
    }

    /**
     * de: Wandelt ein String-Array in ein Spielfeld um.
     * en: Converts a string array to a game board.
     *
     * @param grid de: Parameter grid. en: Parameter grid.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     * @throws IOException de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
    private static CellState[][] fromStringGrid(String[][] grid) throws IOException {
        if (grid == null) {
            return null;
        }

        int size = grid.length;

        if (size == 0) {
            throw new IOException("Saved board must not be empty");
        }

        for (String[] column : grid) {
            if (column == null || column.length != size) {
                throw new IOException("Saved board must be square");
            }
        }

        CellState[][] res = new CellState[size][size];

        for (int c = 0; c < size; c++) {
            for (int r = 0; r < size; r++) {
                String v = grid[c][r];
                res[c][r] = v != null ? CellState.valueOf(v) : CellState.EMPTY;
            }
        }

        return res;
    }

    // DTO for JSON
    public static class GameState {

        /**
         * de: Das Feld ownBoard.
         * en: The field ownBoard.
         */
        public String[][] ownBoard;
        /**
         * de: Das Feld enemyBoard.
         * en: The field enemyBoard.
         */
        public String[][] enemyBoard;
        /**
         * de: Das Feld difficulty.
         * en: The field difficulty.
         */
        public String difficulty;
        /**
         * de: Das Feld gameOver.
         * en: The field gameOver.
         */
        public boolean gameOver;
        /**
         * de: Das Feld playerWon.
         * en: The field playerWon.
         */
        public boolean playerWon;
    }
}
