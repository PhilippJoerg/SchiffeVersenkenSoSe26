/*
 * Datei: view/GameView.java
 * Interface für die Spielsicht: Status, eigene/feindliche Boards und Steuerungszustände.
 */
package view;

import models.CellState;

/**
 * de: Schnittstelle für die Spielsicht: Status, eigene/feindliche Boards und Steuerungszustände.
 * en: Interface for the game view: status, own/enemy boards, and control states.
 */
public interface GameView {
    /**
     * de: Setzt den Status.
     * en: Sets the status.
     *
     * @param text de: Der Status. en: The status.
     */
    void setStatus(String text);
    /**
     * de: Setzt das eigene Board.
     * en: Sets the own board.
     *
     * @param cells de: Die Zellen des eigenen Boards. en: The cells of the own board.
     */
    void setOwnBoard(CellState[][] cells);
    /**
     * de: Setzt das feindliche Board.
     * en: Sets the enemy board.
     *
     * @param cells de: Die Zellen des feindlichen Boards. en: The cells of the enemy board.
     */
    void setEnemyBoard(CellState[][] cells);
    /**
     * de: Setzt den Klick-Listener für das feindliche Board.
     * en: Sets the click listener for the enemy board.
     *
     * @param listener de: Der Klick-Listener. en: The click listener.
     */
    void setEnemyBoardClickListener(BoardClickListener listener);
    /**
     * de: Setzt den Zustand des Schuss-Buttons.
     * en: Sets the state of the shoot button.
     *
     * @param enabled de: Der Zustand des Schuss-Buttons. en: The state of the shoot button.
     */
    void setShootButtonEnabled(boolean enabled);
    /**
     * de: Setzt den Zustand des Rotations-Buttons.
     * en: Sets the state of the rotate button.
     *
     * @param enabled de: Der Zustand des Rotations-Buttons. en: The state of the rotate button.
     */
    void setRotateButtonEnabled(boolean enabled);
    /**
     * de: Setzt den Zustand des Auto-Place-Buttons.
     * en: Sets the state of the auto-place button.
     *
     * @param enabled de: Der Zustand des Auto-Place-Buttons. en: The state of the auto-place button.
     */
    void setAutoPlaceButtonEnabled(boolean enabled);
    /**
     * de: Setzt die Aktion für den Speichern-Button.
     * en: Sets the action for the save button.
     *
     * @param action de: Die Aktion für den Speichern-Button. en: The action for the save button.
     */
    void setSaveAction(Runnable action);
    /**
     * de: Setzt die Aktion für den Laden-Button.
     * en: Sets the action for the load button.
     *
     * @param action de: Die Aktion für den Laden-Button. en: The action for the load button.
     */
    void setLoadAction(Runnable action);
} 
