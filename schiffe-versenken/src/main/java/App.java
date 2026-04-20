import javax.swing.SwingUtilities;

import ui.MainFrame;
import ui.ShipPlacementController;

public class App {

    private static ShipPlacementController placementController;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            placementController = new ShipPlacementController(frame);

            frame.setRotateAction(() -> placementController.rotateCurrentShip());

            frame.setEnemyBoardClickListener((col, row) -> {
                frame.setStatus("Gegnerfeld-Klick: " + (char) ('A' + col) + (row + 1));
            });

            frame.setVisible(true);
        });
    }
}