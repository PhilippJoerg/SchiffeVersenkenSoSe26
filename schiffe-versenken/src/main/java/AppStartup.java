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
import models.GameDifficulty;
import models.GameModel;
import models.SaveLoad;
import view.MainFrame;

public class AppStartup {

    private static GameDifficulty difficulty = GameDifficulty.EASY;
    private static ShipPlacementController placementController;
    private static GameController gameController;

    /**
     * Programmstartpunkt: erzeugt das Hauptfenster und startet den Spielauswahl-Dialog.
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
                String opponent = frame.getSelectedOpponent();
                if ("COMPUTER".equals(opponent)) {
                    difficulty = frame.getSelectedDifficulty();
                    startPlacement(frame, null, false, false);
                } else {
                    startNetworkConnection(frame);
                }
            });

            frame.setLoadAction(() -> handleGlobalLoad(frame));
            frame.setVisible(true);
        });
    }

    /**
     * Initialisiert die Schiffplatzierung und zeigt den Status für den aktuellen Spielmodus an.
     */
    private static void startPlacement(MainFrame frame, Com com, boolean networkMode, boolean iStart) {
        String playerName = getPlayerName(frame);
        frame.showGameScreen();
        placementController = new ShipPlacementController(frame, () -> startGame(frame, com, networkMode, iStart));
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
    private static void startNetworkConnection(MainFrame frame) {
        final int port = 50000;

        String opponent = frame.getSelectedOpponent();
        if ("HOST".equals(opponent)) {
            NetworkHandshakeController.startHost(frame, port, new NetworkHandshakeController.ReadyCallback() {
                @Override
                public void onReady(Com com, boolean iStart) {
                    startPlacement(frame, com, true, iStart);
                }

                @Override
                public void onError(String message, Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, message + ": " + e.getMessage());
                    frame.setConnectionStatus("Starte lokales Spiel.");
                    frame.setLocalIpAddress("");
                    startPlacement(frame, null, false, false);
                }
            });
        } else if ("JOIN".equals(opponent)) {
            String host = frame.getHostIpAddress();
            if (host == null || host.trim().isEmpty()) {
                frame.setConnectionStatus("Keine Host-IP angegeben. Starte lokales Spiel.");
                frame.setLocalIpAddress("");
                startPlacement(frame, null, false, false);
                return;
            }
            NetworkHandshakeController.startClient(frame, host.trim(), port, new NetworkHandshakeController.ReadyCallback() {
                @Override
                public void onReady(Com com, boolean iStart) {
                    startPlacement(frame, com, true, iStart);
                }

                @Override
                public void onError(String message, Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, message + ": " + e.getMessage());
                    frame.setConnectionStatus("Starte lokales Spiel.");
                    frame.setLocalIpAddress("");
                    startPlacement(frame, null, false, false);
                }
            });
        }
    }

    /**
     * Startet das eigentliche Spiel und erstellt den passenden GameController.
     */
    private static void startGame(MainFrame frame, Com com, boolean networkMode, boolean iStart) {
        GameModel gameModel = new GameModel(placementController.getOwnBoard(), networkMode ? GameDifficulty.EASY : difficulty);

        if (networkMode) {
            frame.setConnectionStatus("Netzwerkspiel gestartet");
            gameController = new GameController(frame, gameModel, com, iStart);
        } else {
            frame.setConnectionStatus("Lokales Spiel gegen Computer");
            frame.setLocalIpAddress("");
            gameController = new GameController(frame, gameModel);
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
        gameController = new GameController(frame, loadedModel);
        frame.setStatus("Geladenes Spiel gestartet.");
    }

}