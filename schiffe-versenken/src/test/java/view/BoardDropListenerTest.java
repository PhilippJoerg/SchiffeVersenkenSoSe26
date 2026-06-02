package view;

import javax.swing.TransferHandler;

import controller.ShipPlacementController;
import models.ShipOrientation;
import models.ShipType;

class BoardDropListenerTest {

    static class TestFrame implements PlacementView {
        @Override
        public void setOwnBoard(models.CellState[][] cells) {
        }

        @Override
        public void setShipPaletteRemainingCounts(java.util.Map<ShipType, Integer> remainingCounts) {
        }

        @Override
        public void setShipPaletteOrientation(ShipOrientation orientation) {
        }

        @Override
        public void setOwnBoardClickListener(BoardClickListener listener) {
        }

        @Override
        public void setOwnBoardTransferHandler(TransferHandler transferHandler) {
        }

        @Override
        public void setStatus(String text) {
        }

        @Override
        public BoardPanel getOwnBoard() {
            return new BoardPanel(false);
        }
    }

    static class TestPlacementController extends ShipPlacementController {
        TestPlacementController() {
            super(new TestFrame(), () -> {
            });
        }

        @Override
        public boolean placeShipFromDrag(ShipType shipType, int col, int row, ShipOrientation orientation) {
            return true;
        }
    }

    //TODO: fix tests
    // @Test
    // void testCanImportReturnsFalseForUnsupportedDrag() {
    //     javax.swing.TransferHandler.TransferSupport support = mock(javax.swing.TransferHandler.TransferSupport.class);
    //     when(support.isDrop()).thenReturn(false);
    //     when(support.isDataFlavorSupported(ShipDragData.FLAVOR)).thenReturn(false);

    //     BoardDropListener listener = new BoardDropListener(new BoardPanel(false), new TestPlacementController());
    //     assertFalse(listener.canImport(support));
    // }

    // @Test
    // void testImportDataWithValidTransferSucceeds() throws Exception {
    //     javax.swing.TransferHandler.TransferSupport support = mock(javax.swing.TransferHandler.TransferSupport.class);
    //     Transferable transferable = mock(Transferable.class);
    //     TransferHandler.DropLocation dropLocation = mock(TransferHandler.DropLocation.class);

    //     when(support.isDrop()).thenReturn(true);
    //     when(support.isDataFlavorSupported(ShipDragData.FLAVOR)).thenReturn(true);
    //     when(support.getTransferable()).thenReturn(transferable);
    //     when(transferable.getTransferData(ShipDragData.FLAVOR))
    //             .thenReturn(new ShipDragData(ShipType.SUBMARINE, ShipOrientation.HORIZONTAL));
    //     when(dropLocation.getDropPoint()).thenReturn(new Point(BoardPanel.LABEL_SPACE + 1,
    //             BoardPanel.LABEL_SPACE + 1));
    //     when(support.getDropLocation()).thenReturn(dropLocation);

    //     BoardDropListener listener = new BoardDropListener(new BoardPanel(false), new TestPlacementController());
    //     assertTrue(listener.importData(support));
    // }
}
