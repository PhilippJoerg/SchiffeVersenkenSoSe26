package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import view.CellState;

public class GameModel {
    private final CellState[][] ownBoard;
    private final CellState[][] enemyBoard;
    private List<int[]> Cells;
    private boolean gameOver = false;
    private boolean playerWon = false;
    private GameDifficulty difficulty;

    public GameModel(CellState[][] ownBoard, GameDifficulty difficulty) {
        this.ownBoard = copyBoard(ownBoard);
        this.enemyBoard = BoardUtils.createEmptyCellBoard();
        this.difficulty = difficulty;
        placeEnemyShips();
        initCells();

    }

    private void initCells() {
        Cells = new ArrayList<>();
        for (int col = 0; col < BoardUtils.GRID_SIZE; col++) {
            for (int row = 0; row < BoardUtils.GRID_SIZE; row++) {
                Cells.add(new int[] { col, row });
            }
        }
    }

    private CellState[][] copyBoard(CellState[][] original) {
        CellState[][] copy = new CellState[BoardUtils.GRID_SIZE][BoardUtils.GRID_SIZE];
        for (int row = 0; row < BoardUtils.GRID_SIZE; row++) {
            for (int col = 0; col < BoardUtils.GRID_SIZE; col++) {
                copy[col][row] = original[col][row];
            }
        }
        return copy;
    }

    private void placeEnemyShips() {
        Random random = new Random();
        for (ShipType shipType : ShipType.values()) {
            for (int i = 0; i < shipType.getAmount(); i++) {
                boolean placed = false;
                while (!placed) {
                    int col = random.nextInt(BoardUtils.GRID_SIZE);
                    int row = random.nextInt(BoardUtils.GRID_SIZE);
                    ShipOrientation orientation = random.nextBoolean() ? ShipOrientation.HORIZONTAL
                            : ShipOrientation.VERTICAL;
                    if (BoardUtils.canPlaceShip(enemyBoard, shipType, col, row, orientation)) {
                        BoardUtils.placeShip(enemyBoard, shipType, col, row, orientation);
                        placed = true;
                    }
                }
            }
        }
    }

    public CellState[][] getOwnBoard() {
        return ownBoard;
    }

    public CellState[][] getEnemyBoard() {
        return enemyBoard;
    }

    public int[] computerShoot() {
        switch (difficulty) {
            case GameDifficulty.EASY:
                return shootRandom();

            case GameDifficulty.MEDIUM:
                return shootCheckerboardAndHunt();

            case GameDifficulty.HARD:
                return shootProbabilityDensity();
        
            default:
                return null; // should never happen
        }
    }

    private int[] shootProbabilityDensity() {
        // TODO Auto-generated method stub
        // 2. Mittel: Hunt-and-Target mit Parität (Checkerboard Strategy)
        // Dieser Algorithmus kombiniert ein systematisches Suchmuster mit einer gezielten Verfolgung bei Treffern. 
        // - Suchphase (Hunt): Das Spielfeld wird wie ein Schachbrett betrachtet. Da das kleinste Schiff meist zwei Felder lang ist, reicht es aus, 
        //   nur jedes zweite Feld (z.B. nur die dunklen Felder) zu beschießen, um jedes Schiff mindestens einmal zu treffen.
        // - Angriffsphase (Target): Sobald ein Schiff getroffen wurde, wechselt der Algorithmus in den "Target"-Modus und prüft die vier direkt 
        //   angrenzenden Felder (oben, unten, links, rechts), um das Schiff vollständig zu versenken.
        // - Effizienz: Deutlich höher als der Zufall, da die Anzahl der benötigten Schüsse in der Suchphase halbiert wird. 
        throw new UnsupportedOperationException("Unimplemented method 'shootProbabilityDensity'");
    }

    private int[] shootCheckerboardAndHunt() {
        // TODO Auto-generated method stub
        // 3. Schwer: Wahrscheinlichkeitsdichte-Algorithmus (Probability Density Function)
        // Dies ist die stärkste Strategie, die oft von Computer-KIs genutzt wird. 
        // - Vorgehensweise: Der Algorithmus berechnet für jedes Feld auf dem Gitter, wie viele Möglichkeiten es gibt, die noch übrigen Schiffe dort zu 
        //   platzieren.Felder in der Mitte haben anfangs eine höhere Wahrscheinlichkeit, da dort Schiffe in mehr Ausrichtungen (horizontal/vertikal) hinpassen 
        //   als in den Ecken. Nach jedem Schuss (Treffer oder Fehlschuss) wird die „Heatmap“ neu berechnet. Ein Fehlschuss senkt die Wahrscheinlichkeit der 
        //   umliegenden Felder drastisch, während ein Treffer sie massiv erhöht. 
        // - Effizienz: Extrem hoch. Erfahrene Algorithmen benötigen oft nur 30 bis 40 Schüsse, um die gesamte Flotte zu versenken. YouTube·Vsauce2
        throw new UnsupportedOperationException("Unimplemented method 'shootCheckerboardAndHunt'");
    }

    private int[] shootRandom() {
        if (Cells.isEmpty()) {
            return null; // no empty cells, but shouldn't happen
        }

        Random random = new Random();
        int[] cell = Cells.get(random.nextInt(Cells.size()));
        Cells.remove(cell);
        int col = cell[0];
        int row = cell[1];
        int result = 0;
        if (ownBoard[col][row] == CellState.SHIP) {
            ownBoard[col][row] = CellState.HIT;
            result = 1;
            if (checkComputerWin()) {
                gameOver = true;
                playerWon = false;
            }
        } else {
            ownBoard[col][row] = CellState.MISS;
        }
        return new int[] { col, row, result };
    }

    private boolean checkComputerWin() {
        for (int col = 0; col < BoardUtils.GRID_SIZE; col++) {
            for (int row = 0; row < BoardUtils.GRID_SIZE; row++) {
                if (ownBoard[col][row] == CellState.SHIP) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean shoot(int col, int row) {
        if (gameOver || !BoardUtils.isInsideBoard(col, row)
                || (enemyBoard[col][row] == CellState.HIT || enemyBoard[col][row] == CellState.MISS)) {
            return false; // invalid shot
        }
        if (enemyBoard[col][row] == CellState.SHIP) {
            enemyBoard[col][row] = CellState.HIT;
            if (checkWin()) {
                gameOver = true;
                playerWon = true;
            }
        } else {
            enemyBoard[col][row] = CellState.MISS;
        }
        return true;
    }

    private boolean checkWin() {
        for (int col = 0; col < BoardUtils.GRID_SIZE; col++) {
            for (int row = 0; row < BoardUtils.GRID_SIZE; row++) {
                if (enemyBoard[col][row] == CellState.SHIP && enemyBoard[col][row] != CellState.HIT) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean didPlayerWin() {
        return playerWon;
    }
}