/*
 * Datei: models/GameModel.java
 * Zentrales Modell des Spiels: enthält beide Spielfelder, Spielstatus und die Logik
 * zur Auswertung von Schüssen über `ShootController`. Initialisiert das Gegner-Board
 * mit zufälliger Platzierung.
 */
package models;

import controller.ShootController;

public class GameModel {
    private final CellState[][] ownBoard;
    private final CellState[][] enemyBoard;
    private boolean gameOver = false;
    private boolean playerWon = false;
    private GameDifficulty difficulty;
    private ShootController shootController;

    /**
     * Erzeugt das Spielmodell und platziert die Gegner-Schiffe zufällig.
     */
    public GameModel(CellState[][] ownBoard, GameDifficulty difficulty) {
        this.ownBoard = ownBoard;
        this.enemyBoard = BoardUtils.createEmptyCellBoard();
        this.difficulty = difficulty;
        placeEnemyShips();
        this.shootController = new ShootController(this);
    }

    /**
     * Konstruktor zum Wiederherstellen eines gespeicherten Spielzustands.
     */
    public GameModel(CellState[][] ownBoard, CellState[][] enemyBoard, GameDifficulty difficulty,
            boolean gameOver, boolean playerWon) {
        this.ownBoard = ownBoard;
        this.enemyBoard = enemyBoard;
        this.difficulty = difficulty;
        this.gameOver = gameOver;
        this.playerWon = playerWon;
        this.shootController = new ShootController(this);
    }

    /**
     * Platziert die Schiffe des Gegners zufällig auf dem gegnerischen Board.
     */
    private void placeEnemyShips() {
        BoardUtils.placeRandomShips(enemyBoard);
    }

    /**
     * Liefert das eigene Spielfeld zurück.
     */
    public CellState[][] getOwnBoard() {
        return ownBoard;
    }

    /**
     * Liefert das gegnerische Spielfeld zurück.
     */
    public CellState[][] getEnemyBoard() {
        return enemyBoard;
    }

    /**
     * Gibt die Schwierigkeit des Spiels zurück.
     */
    public GameDifficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Setzt den Spielzustand auf beendet oder nicht.
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    /**
     * Setzt, ob der Spieler das Spiel gewonnen hat.
     */
    public void setPlayerWon(boolean playerWon) {
        this.playerWon = playerWon;
    }

    /**
     * Führt den Computer-Schuss aus und liefert die Schusskoordinaten zurück.
     */
    public int[] computerShoot() {
        return shootController.computerShoot();
    }

    /**
     * Führt einen Spieler-Schuss auf das Gegnerfeld aus.
     */
    public boolean shoot(int col, int row) {
        return shootController.shoot(col, row);
    }

    /**
     * Wertet einen eingehenden Schuss des Netzwerkgegners aus.
     */
    public int evaluateIncomingShot(int col, int row) {
        return shootController.evaluateIncomingShot(col, row);
    }

    /**
     * Gibt zurück, ob das Spiel beendet ist.
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Gibt zurück, ob der Spieler gewonnen hat.
     */
    public boolean didPlayerWin() {
        return playerWon;
    }

    /**
     * Überschreibt den aktuellen Spielzustand mit einem geladenen Modell.
     */
    public void restoreFrom(GameModel other) {
        if (other == null) return;
        // copy board contents
        CellState[][] otherOwn = other.getOwnBoard();
        CellState[][] otherEnemy = other.getEnemyBoard();
        for (int c = 0; c < BoardUtils.GRID_SIZE; c++) {
            for (int r = 0; r < BoardUtils.GRID_SIZE; r++) {
                this.ownBoard[c][r] = otherOwn[c][r];
                this.enemyBoard[c][r] = otherEnemy[c][r];
            }
        }
        this.difficulty = other.getDifficulty();
        this.gameOver = other.isGameOver();
        this.playerWon = other.didPlayerWin();
        this.shootController = new controller.ShootController(this);
    }
}