/*
 * Datei: models/GameModel.java
 * Zentrales Modell des Spiels: enthält beide Spielfelder, Spielstatus und die Logik
 * zur Auswertung von Schüssen über `ShootController`. Initialisiert das Gegner-Board
 * mit zufälliger Platzierung.
 */
package models;

import controller.ShootController;

/**
 * de: Das GameModel repräsentiert das zentrale Modell des Spiels, einschließlich Spielfelder, Spielstatus und Logik zur Auswertung von Schüssen.
 * en: The GameModel represents the central model of the game, including game boards, game status, and logic for evaluating shots.
 */
public class GameModel {

    private CellState[][] ownBoard;
    private CellState[][] enemyBoard;
    private boolean gameOver = false;
    private boolean playerWon = false;
    private GameDifficulty difficulty;
    private ShootController shootController;

    /**
     * de: Erzeugt das Spielmodell und platziert die Gegner-Schiffe zufällig.
     * en: Creates the game model and places the enemy ships randomly.
     */
    public GameModel(CellState[][] ownBoard, GameDifficulty difficulty, GameSettings settings) {
        this.ownBoard = ownBoard;
        this.enemyBoard = BoardUtils.createEmptyCellBoard(settings.getBoardSize());
        this.difficulty = difficulty;
        placeEnemyShips(settings);
        this.shootController = new ShootController(this);
    }

    /**
     * de: Konstruktor zum Wiederherstellen eines gespeicherten Spielzustands.
     * en: Constructor for restoring a saved game state.
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
     * de: Platziert die Schiffe des Gegners zufällig auf dem gegnerischen Board.
     * en: Places the enemy ships randomly on the enemy board.
     */
    private void placeEnemyShips(GameSettings settings) {
        BoardUtils.placeRandomShips(enemyBoard, settings.getShipCounts());
    }

    /**
     * de: Liefert das eigene Spielfeld zurück.
     * en: Returns the player's own game board.
     */
    public CellState[][] getOwnBoard() {
        return ownBoard;
    }

    /**
     * de: Liefert das gegnerische Spielfeld zurück.
     * en: Returns the enemy's game board.
     */
    public CellState[][] getEnemyBoard() {
        return enemyBoard;
    }

    /**
     * de: Gibt die Schwierigkeit des Spiels zurück.
     * en: Returns the game's difficulty level.
     */
    public GameDifficulty getDifficulty() {
        return difficulty;
    }

    /**
     * de: Setzt den Spielzustand auf beendet oder nicht.
     * en: Sets the game over state.
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    /**
     * de: Setzt, ob der Spieler das Spiel gewonnen hat.
     * en: Sets whether the player has won the game.
     */
    public void setPlayerWon(boolean playerWon) {
        this.playerWon = playerWon;
    }

    /**
     * de: Führt den Computer-Schuss aus und liefert die Schusskoordinaten zurück.
     * en: Executes the computer's shot and returns the shot coordinates.
     */
    public int[] computerShoot() {
        return shootController.computerShoot();
    }

    /**
     * de: Führt einen Spieler-Schuss auf das Gegnerfeld aus.
     * en: Executes a player's shot on the enemy board.
     */
    public boolean shoot(int col, int row) {
        return shootController.shoot(col, row);
    }

    /**
     * de: Wertet einen eingehenden Schuss des Netzwerkgegners aus.
     * en: Evaluates an incoming shot from the network opponent.
     */
    public int evaluateIncomingShot(int col, int row) {
        return shootController.evaluateIncomingShot(col, row);
    }

    /**
     * de: Gibt zurück, ob das Spiel beendet ist.
     * en: Returns whether the game is over.
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * de: Gibt zurück, ob der Spieler gewonnen hat.
     * en: Returns whether the player has won the game.
     */
    public boolean didPlayerWin() {
        return playerWon;
    }

    /**
     * de: Überschreibt den aktuellen Spielzustand mit einem geladenen Modell.
     * en: Restores the current game state from a loaded model.
     */
    public void restoreFrom(GameModel other) {
    if (other == null) {
        return;
    }

    this.ownBoard = other.getOwnBoard();
    this.enemyBoard = other.getEnemyBoard();
    this.difficulty = other.getDifficulty();
    this.gameOver = other.isGameOver();
    this.playerWon = other.didPlayerWin();
    this.shootController = new ShootController(this);
}
}
