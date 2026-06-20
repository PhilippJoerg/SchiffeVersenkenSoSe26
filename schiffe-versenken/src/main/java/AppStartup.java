/*
 * Datei: AppStartup.java
 * Startklasse: Initialisiert die Benutzeroberfläche, fragt Gegner und Schwierigkeit ab
 * und startet das Spiel entweder lokal oder im Netzwerk.
 */
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import controller.Com;
import controller.GameController;
import controller.NetworkHandshakeController;
import controller.ShipPlacementController;
import models.BoardUtils;
import models.GameDifficulty;
import models.GameModel;
import models.GameSettings;
import models.SaveLoad;
import view.MainFrame;

public class AppStartup {

    private static GameDifficulty difficulty = GameDifficulty.EASY;
    private static ShipPlacementController placementController;
    private static GameController gameController;

    /**
     * Programmstartpunkt: erzeugt das Hauptfenster und startet den
     * Spielauswahl-Dialog.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();

            frame.setRotateAction(() -> {
                if (placementController != null) {
                    placementController.rotateCurrentShip();
                }
            });

            frame.setAutoPlaceAction(() -> {
                if (placementController != null) {
                    placementController.autoPlaceShips();
                }
            });

            frame.setStartAction(() -> {
                GameSettings settings = frame.getGameSettings();

                String opponent = frame.getSelectedOpponent();
                if ("COMPUTER".equals(opponent)) {
                    difficulty = frame.getSelectedDifficulty();
                    startPlacement(frame, null, false, false, settings);
                } else {
                    startNetworkConnection(frame, settings);
                }
            });

            frame.setLoadAction(() -> handleGlobalLoad(frame));
            frame.setVisible(true);
        });
    }

    /**
     * Initialisiert die Schiffplatzierung und zeigt den Status für den
     * aktuellen Spielmodus an.
     */
    private static void startPlacement(MainFrame frame, Com com, boolean networkMode, boolean iStart, GameSettings settings) {
        String playerName = getPlayerName(frame);
        frame.showGameScreen();
        frame.setEnemyBoard(BoardUtils.createEmptyCellBoard(settings.getBoardSize()));
        placementController = new ShipPlacementController(frame, () -> startGame(frame, com, networkMode, iStart, settings), settings);
        frame.setStatus(networkMode
                ? playerName + ", Verbindung hergestellt. Platziere deine Schiffe."
                : playerName + ", platziere deine Schiffe.");
    }

    private static String getPlayerName(MainFrame frame) {
        String playerName = frame.getStartScreenText();
        return (playerName == null || playerName.isEmpty()) ? "Spieler" : playerName;
    }

    /**
     * Baut die Netzwerkverbindung auf und startet Host- oder Client-Handshake.
     */
    private static void startNetworkConnection(MainFrame frame, GameSettings settings) {
        final int port = 50000;

        String opponent = frame.getSelectedOpponent();
        if ("HOST".equals(opponent)) {
            NetworkHandshakeController.startHost(frame, port, new NetworkHandshakeController.ReadyCallback() {
                @Override
                public void onReady(Com com, boolean iStart) {
                    startPlacement(frame, com, true, iStart, settings);
                }

                @Override
                public void onError(String message, Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, message + ": " + e.getMessage());
                    frame.setConnectionStatus("Starte lokales Spiel.");
                    frame.setLocalIpAddress("");
                    startPlacement(frame, null, false, false, settings);
                }
            });
        } else if ("JOIN".equals(opponent)) {
            String host = frame.getHostIpAddress();
            if (host == null || host.trim().isEmpty()) {
                frame.setConnectionStatus("Keine Host-IP angegeben. Starte lokales Spiel.");
                frame.setLocalIpAddress("");
                startPlacement(frame, null, false, false, settings);
                return;
            }
            NetworkHandshakeController.startClient(frame, host.trim(), port, new NetworkHandshakeController.ReadyCallback() {
                @Override
                public void onReady(Com com, boolean iStart) {
                    startPlacement(frame, com, true, iStart, settings);
                }

                @Override
                public void onError(String message, Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, message + ": " + e.getMessage());
                    frame.setConnectionStatus("Starte lokales Spiel.");
                    frame.setLocalIpAddress("");
                    startPlacement(frame, null, false, false, settings);
                }
            });
        }
    }

    private static void returnToMainMenu(MainFrame frame) {
    placementController = null;
    gameController = null;
    frame.setConnectionStatus("Nicht verbunden");
    frame.setLocalIpAddress("");
    frame.setStatus("Bereit.");
    frame.showStartScreen();
}

    /**
     * Startet das eigentliche Spiel und erstellt den passenden GameController.
     */
    private static void startGame(MainFrame frame, Com com, boolean networkMode, boolean iStart, GameSettings settings) {
        GameModel gameModel = new GameModel(
                placementController.getOwnBoard(),
                networkMode ? GameDifficulty.EASY : difficulty,
                settings
        );

        if (networkMode) {
            frame.setConnectionStatus("Netzwerkspiel gestartet");
            gameController = new GameController(frame, gameModel, com, iStart, () -> returnToMainMenu(frame));
        } else {
            frame.setConnectionStatus("Lokales Spiel gegen Computer");
            frame.setLocalIpAddress("");
            gameController = new GameController(frame, gameModel, () -> returnToMainMenu(frame));
        }
    }

    private static void handleGlobalLoad(MainFrame frame) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try {
            GameModel loaded = SaveLoad.loadGame(file);
            startLoadedGame(frame, loaded);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Fehler beim Laden: " + e.getMessage(), "Fehler",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void startLoadedGame(MainFrame frame, GameModel loadedModel) {
        frame.showGameScreen();
        frame.setConnectionStatus("Lokales Spiel geladen");
        frame.setLocalIpAddress("");
        gameController = new GameController(frame, loadedModel, () -> returnToMainMenu(frame));
        frame.setStatus("Geladenes Spiel gestartet.");
    }

}
