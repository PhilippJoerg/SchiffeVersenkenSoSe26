package view;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

import models.CellState;
import models.ShipOrientation;
import models.ShipType;

class PlacementViewTest {

    @Test
    void testAnonymousImplementationCanBeCreated() {
        PlacementView view = new PlacementView() {
            @Override
            public void setOwnBoard(CellState[][] cells) {
            }

            @Override
            public void setShipPaletteRemainingCounts(Map<ShipType, Integer> remainingCounts) {
            }

            @Override
            public void setShipPaletteOrientation(ShipOrientation orientation) {
            }

            @Override
            public void setOwnBoardClickListener(BoardClickListener listener) {
            }

            @Override
            public void setOwnBoardTransferHandler(javax.swing.TransferHandler transferHandler) {
            }

            @Override
            public void setStatus(String text) {
            }

            @Override
            public BoardPanel getOwnBoard() {
                return new BoardPanel(false);
            }
        };

        assertNotNull(view);
    }
}
