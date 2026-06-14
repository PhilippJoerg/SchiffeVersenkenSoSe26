package models;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility to save and load game state as JSON.
 */
public class SaveLoad {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String VERSION = "1";

    public static void saveGame(File file, GameModel model) throws IOException {
        GameState gs = new GameState();
        gs.version = VERSION;
        gs.difficulty = model.getDifficulty() != null ? model.getDifficulty().name() : null;
        gs.gameOver = model.isGameOver();
        gs.playerWon = model.didPlayerWin();
        gs.ownBoard = toStringGrid(model.getOwnBoard());
        gs.enemyBoard = toStringGrid(model.getEnemyBoard());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, gs);
    }

    public static GameModel loadGame(File file) throws IOException {
        GameState gs = MAPPER.readValue(file, GameState.class);
        if (gs == null || gs.version == null) {
            throw new IOException("Invalid save file: missing version");
        }

        CellState[][] own = fromStringGrid(gs.ownBoard);
        CellState[][] enemy = fromStringGrid(gs.enemyBoard);
        GameDifficulty difficulty = gs.difficulty != null ? GameDifficulty.valueOf(gs.difficulty) : GameDifficulty.EASY;
        return new GameModel(own, enemy, difficulty, gs.gameOver, gs.playerWon);
    }

    private static String[][] toStringGrid(CellState[][] board) {
        if (board == null) return null;
        int size = board.length;
        String[][] res = new String[size][size];
        for (int c = 0; c < size; c++) {
            for (int r = 0; r < size; r++) {
                res[c][r] = board[c][r] != null ? board[c][r].name() : null;
            }
        }
        return res;
    }

    private static CellState[][] fromStringGrid(String[][] grid) throws IOException {
        if (grid == null) return null;
        int size = grid.length;
        if (size != BoardUtils.GRID_SIZE) {
            throw new IOException("Saved board size does not match expected grid size");
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
        public String version;
        public String[][] ownBoard;
        public String[][] enemyBoard;
        public String difficulty;
        public boolean gameOver;
        public boolean playerWon;
    }
}
