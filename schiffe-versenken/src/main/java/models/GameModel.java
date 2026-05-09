package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import view.CellState;

public class GameModel {
    private final CellState[][] ownBoard;
    private final CellState[][] enemyBoard;
    private boolean gameOver = false;
    private boolean playerWon = false;

    public GameModel(CellState[][] ownBoard) {
        this.ownBoard = copyBoard(ownBoard);
        this.enemyBoard = BoardUtils.createEmptyCellBoard();
        placeEnemyShips();
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
        List<int[]> emptyCells = new ArrayList<>();
        for (int col = 0; col < BoardUtils.GRID_SIZE; col++) {
            for (int row = 0; row < BoardUtils.GRID_SIZE; row++) {
                if (ownBoard[col][row] == CellState.EMPTY) {
                    emptyCells.add(new int[] { col, row });
                }
            }
        }
        if (emptyCells.isEmpty()) {
            return null; // no empty cells, but shouldn't happen
        }
        Random random = new Random();
        int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
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
        if (gameOver) {
            return false; // invalid shot
        }
        if (!BoardUtils.isInsideBoard(col, row)) {
            return false; // invalid shot
        }
        if (enemyBoard[col][row] == CellState.HIT || enemyBoard[col][row] == CellState.MISS) {
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