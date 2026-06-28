package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import models.CellState;
import models.GameSettings;
import models.ShipOrientation;
import models.ShipType;
import view.BoardPanel;
import view.BoardClickListener;
import view.PlacementView;

class ShipPlacementControllerTest {

    private TestPlacementView frame;
    private boolean finished;
    private ShipPlacementController controller;

    static class TestPlacementView implements PlacementView {
        private CellState[][] ownBoard;
        private Map<ShipType, Integer> remainingCounts;
        private ShipOrientation orientation;
        private String status;

        @Override
        public void setOwnBoard(CellState[][] cells) {
            this.ownBoard = cells;
        }

        @Override
        public void setShipPaletteRemainingCounts(Map<ShipType, Integer> remainingCounts) {
            this.remainingCounts = remainingCounts;
        }

        @Override
        public void setShipPaletteOrientation(ShipOrientation orientation) {
            this.orientation = orientation;
        }

        @Override
        public void setOwnBoardClickListener(BoardClickListener listener) {
        }

        @Override
        public void setOwnBoardTransferHandler(javax.swing.TransferHandler transferHandler) {
            // ignore for tests
        }

        @Override
        public void setStatus(String text) {
            this.status = text;
        }

        @Override
        public view.BoardPanel getOwnBoard() {
            return new BoardPanel(false);
        }
    }

    @BeforeEach
    void setUp() {
        frame = new TestPlacementView();
        finished = false;
        controller = new ShipPlacementController(frame, () -> finished = true, GameSettings.defaultSettings());
    }

    @Test
    void testRotateCurrentShipUpdatesOrientationAndStatus() {
        ShipOrientation before = frame.orientation;
        controller.rotateCurrentShip();
        assertTrue(frame.orientation != before);
        assertTrue(frame.status.contains("Platziere:"));
    }

    @Test
    void testAutoPlaceShipsFinishesPlacement() {
        controller.autoPlaceShips();
        assertTrue(frame.status.contains("automatisch"));
        assertTrue(finished);
        assertTrue(controller.isPlacementFinished());
    }

    @Test
    void testPlaceShipFromDragReturnsFalseForInvalidShip() {
        assertFalse(controller.placeShipFromDrag(null, 0, 0, ShipOrientation.HORIZONTAL));
    }

    @Test
    void testGetRemainingShipsAndOwnBoard() {
        assertEquals(frame.ownBoard, controller.getOwnBoard());
        assertEquals(frame.remainingCounts, controller.getRemainingShips());
    }
}
