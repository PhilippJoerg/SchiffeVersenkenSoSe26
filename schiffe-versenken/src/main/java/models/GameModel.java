package models;

import java.util.Random;

import controller.ShootController;
import models.CellState;

public class GameModel {
    private final CellState[][] ownBoard;
    private final CellState[][] enemyBoard;
    private boolean gameOver = false;
    private boolean playerWon = false;
    private GameDifficulty difficulty;
    private ShootController shootController;

    public GameModel(CellState[][] ownBoard, GameDifficulty difficulty) {
        this.ownBoard = ownBoard;
        this.enemyBoard = BoardUtils.createEmptyCellBoard();
        this.difficulty = difficulty;
        placeEnemyShips();
        this.shootController = new ShootController(this);
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

    public GameDifficulty getDifficulty() {
        return difficulty;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void setPlayerWon(boolean playerWon) {
        this.playerWon = playerWon;
    }

    public int[] computerShoot() {
        return shootController.computerShoot();
    }

    public boolean shoot(int col, int row) {
        return shootController.shoot(col, row);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean didPlayerWin() {
        return playerWon;
    }
}