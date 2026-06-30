/*
 * Datei: view/PlacementView.java
 * Interface für die Platzierungsansicht: stellt Methoden bereit, um das eigene Board,
 * die Ausrichtung der Schiffe und die verbleibenden Mengen in der Palette zu setzen.
 * Zusätzlich können TransferHandler und Klick-Listener für Drag-and-Drop/Interaktion
 * bereitgestellt werden.
 */
package view;

import java.util.Map;
import javax.swing.TransferHandler;
import models.CellState;
import models.ShipOrientation;
import models.ShipType;

/**
 * de: Die Schnittstelle PlacementView.
 * en: The interface PlacementView.
 */
public interface PlacementView {
    /**
     * de: Setzt das eigene Spielfeld.
     * en: Sets the own board.
     *
     * @param cells de: Die Zellen des eigenen Spielfelds. en: The cells of the own board.
     */
    void setOwnBoard(CellState[][] cells);
    /**
     * de: Setzt die verbleibenden Schiffszahlen in der Palette.
     * en: Sets the remaining ship counts in the palette.
     *
     * @param remainingCounts de: Die verbleibenden Schiffszahlen. en: The remaining ship counts.
     */
    void setShipPaletteRemainingCounts(Map<ShipType, Integer> remainingCounts);
    /**
     * de: Setzt die Ausrichtung der Schiffe in der Palette.
     * en: Sets the orientation of the ships in the palette.
     *
     * @param orientation de: Die Ausrichtung der Schiffe. en: The orientation of the ships.
     */
    void setShipPaletteOrientation(ShipOrientation orientation);
    /**
     * de: Setzt den Klick-Listener für das eigene Spielfeld.
     * en: Sets the click listener for the own board.
     *
     * @param listener de: Der Klick-Listener für das eigene Spielfeld. en: The click listener for the own board.
     */
    void setOwnBoardClickListener(BoardClickListener listener);
    /**
     * de: Setzt den Transfer-Handler für das eigene Spielfeld. Aktiviert Drag-and-Drop auf dem eigenen Feld.
     * en: Sets the transfer handler for the own board. Enables drag-and-drop on the own board.
     *
     * @param transferHandler de: Der Transfer-Handler für das eigene Spielfeld. en: The transfer handler for the own board.
     */
    void setOwnBoardTransferHandler(TransferHandler transferHandler);
    /**
     * de: Setzt den Statustext.
     * en: Sets the status text.
     *
     * @param text de: Der Statustext. en: The status text.
     */
    void setStatus(String text);
    /**
     * de: Gibt das eigene Board zurück.
     * en: Returns the own board.
     *
     * @return de: Das eigene Board. en: The own board.
     */
    BoardPanel getOwnBoard();
}
