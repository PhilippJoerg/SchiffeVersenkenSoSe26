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

/**
 * de: Testet die Klasse ShipPlacementController.
 * en: Tests the ShipPlacementController class.
 */
class ShipPlacementControllerTest {

    private TestPlacementView frame;
    private boolean finished;
    private ShipPlacementController controller;

    static class TestPlacementView implements PlacementView {
        private CellState[][] ownBoard;
        private Map<ShipType, Integer> remainingCounts;
        private ShipOrientation orientation;
        private String status;

        /**
         * de: Reagiert auf das Ereignis "SetOwnBoard".
         * en: Responds to the "SetOwnBoard" event.
         *
         * @param cells de: Parameter cells. en: Parameter cells.
         */
        @Override
        public void setOwnBoard(CellState[][] cells) {
            this.ownBoard = cells;
        }

        /**
         * de: Reagiert auf das Ereignis "SetShipPaletteRemainingCounts".
         * en: Responds to the "SetShipPaletteRemainingCounts" event.
         *
         * @param remainingCounts de: Parameter remainingCounts. en: Parameter remainingCounts.
         */
        @Override
        public void setShipPaletteRemainingCounts(Map<ShipType, Integer> remainingCounts) {
            this.remainingCounts = remainingCounts;
        }

        /**
         * de: Reagiert auf das Ereignis "SetShipPaletteOrientation".
         * en: Responds to the "SetShipPaletteOrientation" event.
         *
         * @param orientation de: Parameter orientation. en: Parameter orientation.
         */
        @Override
        public void setShipPaletteOrientation(ShipOrientation orientation) {
            this.orientation = orientation;
        }

        /**
         * de: Reagiert auf das Ereignis "SetOwnBoardClickListener".
         * en: Responds to the "SetOwnBoardClickListener" event.
         *
         * @param listener de: Parameter listener. en: Parameter listener.
         */
        @Override
        public void setOwnBoardClickListener(BoardClickListener listener) {
        }

        /**
         * de: Reagiert auf das Ereignis "SetOwnBoardTransferHandler".
         * en: Responds to the "SetOwnBoardTransferHandler" event.
         *
         * @param transferHandler de: Parameter transferHandler. en: Parameter transferHandler.
         */
        @Override
        public void setOwnBoardTransferHandler(javax.swing.TransferHandler transferHandler) {
            // ignore for tests
        }

        /**
         * de: Reagiert auf das Ereignis "SetStatus".
         * en: Responds to the "SetStatus" event.
         *
         * @param text de: Parameter text. en: Parameter text.
         */
        @Override
        public void setStatus(String text) {
            this.status = text;
        }

        /**
         * de: Reagiert auf das Ereignis "GetOwnBoard".
         * en: Responds to the "GetOwnBoard" event.
         *
         * @return de: Rueckgabewert der Methode. en: Method return value.
         */
        @Override
        public view.BoardPanel getOwnBoard() {
            return new BoardPanel(false);
        }
    }

    /**
     * de: Reagiert auf das Ereignis "SetUp".
     * en: Responds to the "SetUp" event.
     *
     */
    @BeforeEach
    void setUp() {
        frame = new TestPlacementView();
        finished = false;
        controller = new ShipPlacementController(frame, () -> finished = true, GameSettings.defaultSettings());
    }

    /**
     * de: Reagiert auf das Ereignis "TestRotateCurrentShipUpdatesOrientationAndStatus".
     * en: Responds to the "TestRotateCurrentShipUpdatesOrientationAndStatus" event.
     *
     */
    @Test
    void testRotateCurrentShipUpdatesOrientationAndStatus() {
        ShipOrientation before = frame.orientation;
        controller.rotateCurrentShip();
        assertTrue(frame.orientation != before);
        assertTrue(frame.status.contains("Platziere:"));
    }

    /**
     * de: Reagiert auf das Ereignis "TestAutoPlaceShipsFinishesPlacement".
     * en: Responds to the "TestAutoPlaceShipsFinishesPlacement" event.
     *
     */
    @Test
    void testAutoPlaceShipsFinishesPlacement() {
        controller.autoPlaceShips();
        assertTrue(frame.status.contains("automatisch"));
        assertTrue(finished);
        assertTrue(controller.isPlacementFinished());
    }

    /**
     * de: Reagiert auf das Ereignis "TestPlaceShipFromDragReturnsFalseForInvalidShip".
     * en: Responds to the "TestPlaceShipFromDragReturnsFalseForInvalidShip" event.
     *
     */
    @Test
    void testPlaceShipFromDragReturnsFalseForInvalidShip() {
        assertFalse(controller.placeShipFromDrag(null, 0, 0, ShipOrientation.HORIZONTAL));
    }

    /**
     * de: Reagiert auf das Ereignis "TestGetRemainingShipsAndOwnBoard".
     * en: Responds to the "TestGetRemainingShipsAndOwnBoard" event.
     *
     */
    @Test
    void testGetRemainingShipsAndOwnBoard() {
        assertEquals(frame.ownBoard, controller.getOwnBoard());
        assertEquals(frame.remainingCounts, controller.getRemainingShips());
    }
}
