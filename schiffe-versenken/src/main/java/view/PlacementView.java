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

public interface PlacementView {
    void setOwnBoard(CellState[][] cells);
    void setShipPaletteRemainingCounts(Map<ShipType, Integer> remainingCounts);
    void setShipPaletteOrientation(ShipOrientation orientation);
    void setOwnBoardClickListener(BoardClickListener listener);
    void setOwnBoardTransferHandler(TransferHandler transferHandler);
    void setStatus(String text);
    // allow controller to obtain the actual BoardPanel for transfer handlers
    BoardPanel getOwnBoard();
}
