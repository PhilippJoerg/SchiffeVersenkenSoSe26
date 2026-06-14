package controller;

/*
 * Datei: controller/ShootController.java
 * Kapselt die Schuss-Logik für den Computergegner: verschiedene Schwierigkeitsstufen
 * und Auswertung eingehender Schüsse aus dem Netzwerk.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import models.BoardUtils;
import models.CellState;
import models.GameDifficulty;
import models.GameModel;

public class ShootController {
    private final GameModel gameModel;
    private final Random random;
    private List<int[]> availableCells;
    private final List<int[]> targetQueue;
    private final GameDifficulty difficulty;

    public ShootController(GameModel gameModel) {
        this.gameModel = gameModel;
        this.random = new Random();
        this.targetQueue = new ArrayList<>();
        this.difficulty = gameModel.getDifficulty();
        initAvailableCells();
    }

    private void initAvailableCells() {
        availableCells = new ArrayList<>();
        for (int col = 0; col < BoardUtils.GRID_SIZE; col++) {
            for (int row = 0; row < BoardUtils.GRID_SIZE; row++) {
                availableCells.add(new int[] { col, row });
            }
        }
    }

    /**
     * Wertet einen Schuss auf das gegnerische Board aus und markiert Treffer oder Fehlschuss.
     */
    public boolean shoot(int col, int row) {
        CellState[][] enemyBoard = gameModel.getEnemyBoard();
        if (gameModel.isGameOver() || !BoardUtils.isInsideBoard(col, row)
                || (enemyBoard[col][row] == CellState.HIT || enemyBoard[col][row] == CellState.MISS)) {
            return false;
        }
        if (enemyBoard[col][row] == CellState.SHIP) {
            enemyBoard[col][row] = CellState.HIT;
            if (checkWin()) {
                gameModel.setGameOver(true);
                gameModel.setPlayerWon(true);
            }
        } else {
            enemyBoard[col][row] = CellState.MISS;
        }
        return true;
    }

    /**
     * Überprüft, ob alle Schiffe auf dem gegnerischen Board versenkt wurden.
     */
    private boolean checkWin() {
        CellState[][] enemyBoard = gameModel.getEnemyBoard();
        for (int col = 0; col < BoardUtils.GRID_SIZE; col++) {
            for (int row = 0; row < BoardUtils.GRID_SIZE; row++) {
                if (enemyBoard[col][row] == CellState.SHIP) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Bestimmt den nächsten Schuss des Computers abhängig von der Schwierigkeit.
     */
    public int[] computerShoot() {
        switch (difficulty) {
            case GameDifficulty.EASY:
                return shootRandom();

            case GameDifficulty.MEDIUM:
                return shootCheckerboardAndHunt();

            case GameDifficulty.HARD:
                return shootProbabilityDensity();
        
            default:
                return null;
        }
    }

    /**
     * Wertet einen eingehenden Schuss vom Netzwerkgegner aus und liefert das Ergebnis zurück.
     * 0 = Wasser, 1 = Treffer, 2 = Versenkt
     */
    public int evaluateIncomingShot(int col, int row) {
        if (gameModel.isGameOver() || !BoardUtils.isInsideBoard(col, row)) {
            return 0;
        }

        int result = 0;
        CellState[][] ownBoard = gameModel.getOwnBoard();
        if (ownBoard[col][row] == CellState.SHIP) {
            ownBoard[col][row] = CellState.HIT;
            result = 1;
            if (checkComputerWin()) {
                gameModel.setGameOver(true);
                gameModel.setPlayerWon(false);
                result = 2;
            }
        } else {
            ownBoard[col][row] = CellState.MISS;
            result = 0;
        }
        return result;
    }

    /**
     * Platzhalter für eine schwere KI-Strategie auf Basis von Wahrscheinlichkeitsdichte.
     */
    private int[] shootProbabilityDensity() {
        // TODO: implement shootProbabilityDensity()
        // 3. Schwer: Hunt-and-Target mit Parität (Checkerboard Strategy)
        // Dieser Algorithmus kombiniert ein systematisches Suchmuster mit einer gezielten Verfolgung bei Treffern. 
        // - Suchphase (Hunt): Das Spielfeld wird wie ein Schachbrett betrachtet. Da das kleinste Schiff meist zwei Felder lang ist, reicht es aus, 
        //   nur jedes zweite Feld (z.B. nur die dunklen Felder) zu beschießen, um jedes Schiff mindestens einmal zu treffen.
        // - Angriffsphase (Target): Sobald ein Schiff getroffen wurde, wechselt der Algorithmus in den "Target"-Modus und prüft die vier direkt 
        //   angrenzenden Felder (oben, unten, links, rechts), um das Schiff vollständig zu versenken.
        // - Effizienz: Deutlich höher als der Zufall, da die Anzahl der benötigten Schüsse in der Suchphase halbiert wird. 
        throw new UnsupportedOperationException("Unimplemented method 'shootProbabilityDensity'");
    }

    /**
     * Platzhalter für eine mittlere KI-Strategie mit Schachbrett- und Jagd-Modus.
     * TODO: add a sunk ship tracking to avoid shooting around already sunk ships
     */
    private int[] shootCheckerboardAndHunt() {
        if (availableCells.isEmpty()) {
            return null;
        }

        int[] cell = pollNextTargetCell();
        if (cell == null) {
            cell = pickParityCell();
        }

        if (cell == null) {
            return null;
        }

        int col = cell[0];
        int row = cell[1];
        removeAvailableCell(col, row);

        CellState[][] ownBoard = gameModel.getOwnBoard();
        int result = 0;
        if (ownBoard[col][row] == CellState.SHIP) {
            ownBoard[col][row] = CellState.HIT;
            result = 1;
            enqueueTargetCells(col, row);
            if (checkComputerWin()) {
                gameModel.setGameOver(true);
                gameModel.setPlayerWon(false);
            }
        } else {
            ownBoard[col][row] = CellState.MISS;
        }

        return new int[] { col, row, result };
    }

    private int[] pollNextTargetCell() {
        while (!targetQueue.isEmpty()) {
            int[] next = targetQueue.remove(0);
            if (isCellAvailable(next[0], next[1])) {
                return next;
            }
        }
        return null;
    }

    private int[] pickParityCell() {
        List<int[]> parityCells = new ArrayList<>();
        for (int[] cell : availableCells) {
            if ((cell[0] + cell[1]) % 2 == 0) {
                parityCells.add(cell);
            }
        }
        if (!parityCells.isEmpty()) {
            return parityCells.get(random.nextInt(parityCells.size()));
        }
        return availableCells.get(random.nextInt(availableCells.size()));
    }

    private boolean isCellAvailable(int col, int row) {
        for (int[] cell : availableCells) {
            if (cell[0] == col && cell[1] == row) {
                return true;
            }
        }
        return false;
    }

    private void removeAvailableCell(int col, int row) {
        for (int i = 0; i < availableCells.size(); i++) {
            int[] cell = availableCells.get(i);
            if (cell[0] == col && cell[1] == row) {
                availableCells.remove(i);
                return;
            }
        }
    }

    private boolean containsTargetCell(int col, int row) {
        for (int[] cell : targetQueue) {
            if (cell[0] == col && cell[1] == row) {
                return true;
            }
        }
        return false;
    }

    private void enqueueTargetCells(int col, int row) {
        int[][] offsets = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };
        for (int[] offset : offsets) {
            int nextCol = col + offset[0];
            int nextRow = row + offset[1];
            if (BoardUtils.isInsideBoard(nextCol, nextRow) && isCellAvailable(nextCol, nextRow)
                    && !containsTargetCell(nextCol, nextRow)) {
                targetQueue.add(new int[] { nextCol, nextRow });
            }
        }
    }

    /**
     * Führt einen zufälligen Schuss des Computers aus und markiert das Ergebnis.
     */
    private int[] shootRandom() {
        if (availableCells.isEmpty()) {
            return null; 
        }

        Random random = new Random();
        int[] cell = availableCells.get(random.nextInt(availableCells.size()));
        availableCells.remove(cell);
        int col = cell[0];
        int row = cell[1];
        CellState[][] ownBoard = gameModel.getOwnBoard();
        int result = 0;
        if (ownBoard[col][row] == CellState.SHIP) {
            ownBoard[col][row] = CellState.HIT;
            result = 1;
            if (checkComputerWin()) {
                gameModel.setGameOver(true);
                gameModel.setPlayerWon(false);
            }
        } else {
            ownBoard[col][row] = CellState.MISS;
        }
        return new int[] { col, row, result };
    }

    /**
     * Überprüft, ob alle eigenen Schiffe vom Computer getroffen wurden.
     * TODO: remove dupicate and refactor checkWin()
     */
    private boolean checkComputerWin() {
        CellState[][] ownBoard = gameModel.getOwnBoard();
        for (int col = 0; col < BoardUtils.GRID_SIZE; col++) {
            for (int row = 0; row < BoardUtils.GRID_SIZE; row++) {
                if (ownBoard[col][row] == CellState.SHIP) {
                    return false;
                }
            }
        }
        return true;
    }
}