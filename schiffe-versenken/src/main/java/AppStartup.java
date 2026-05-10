import javax.swing.SwingUtilities;

import controller.GameController;
import controller.ShipPlacementController;
import models.GameDifficulty;
import models.GameModel;
import view.MainFrame;

public class AppStartup {

    private static ShipPlacementController placementController;
    private static GameController gameController;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            placementController = new ShipPlacementController(frame, () -> startGame(frame));

            frame.setRotateAction(() -> placementController.rotateCurrentShip());

            frame.setEnemyBoardClickListener((col, row) -> {
                frame.setStatus("Gegnerfeld-Klick: " + (char) ('A' + col) + (row + 1));
            });

            frame.setVisible(true);
        });
    }

    private static void startGame(MainFrame frame) {
        GameModel gameModel = new GameModel(placementController.getOwnBoard(), GameDifficulty.EASY);
        gameController = new GameController(frame, gameModel);
    }
}