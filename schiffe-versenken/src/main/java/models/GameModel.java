package models;

import controller.ShootController;

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
        BoardUtils.placeRandomShips(enemyBoard);
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

    /* Evaluate an incoming shot from the network opponent. Returns 0=miss,1=hit,2=sunk */
    public int evaluateIncomingShot(int col, int row) {
        return shootController.evaluateIncomingShot(col, row);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean didPlayerWin() {
        return playerWon;
    }
}