import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

import controller.Com;
import controller.GameController;
import controller.NetworkHandshakeController;
import controller.ShipPlacementController;
import models.GameDifficulty;
import models.GameModel;
import view.MainFrame;

public class AppStartup {

    private static enum OpponentType {
        COMPUTER, HOST, JOIN
    }

    private static OpponentType opponentType = OpponentType.COMPUTER;
    private static GameDifficulty difficulty = GameDifficulty.EASY;
    private static ShipPlacementController placementController;
    private static GameController gameController;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            askOpponent(frame);

            frame.setRotateAction(() -> {
                if (placementController != null) {
                    placementController.rotateCurrentShip();
                }
            });

            frame.setVisible(true);

            if (opponentType == OpponentType.COMPUTER) {
                difficulty = askDifficulty(frame);
                startPlacement(frame, null, false, false);
            } else {
                startNetworkConnection(frame);
            }
        });
    }

    private static void askOpponent(MainFrame frame) {
        JDialog dialog = new JDialog(frame, "Startbildschirm", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        JLabel title = new JLabel("Wähle deinen Gegner", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(18f));
        content.add(title, BorderLayout.NORTH);

        JLabel info = new JLabel("Bitte wähle, gegen wen du spielen möchtest.", JLabel.CENTER);
        content.add(info, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton computerBtn = new JButton("Computer");
        JButton hostBtn = new JButton("Host (Netzwerk)");
        JButton joinBtn = new JButton("Beitreten (Netzwerk)");

        computerBtn.addActionListener(e -> {
            opponentType = OpponentType.COMPUTER;
            dialog.dispose();
        });
        hostBtn.addActionListener(e -> {
            opponentType = OpponentType.HOST;
            dialog.dispose();
        });
        joinBtn.addActionListener(e -> {
            opponentType = OpponentType.JOIN;
            dialog.dispose();
        });

        buttonPanel.add(computerBtn);
        buttonPanel.add(hostBtn);
        buttonPanel.add(joinBtn);
        content.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private static void startPlacement(MainFrame frame, Com com, boolean networkMode, boolean iStart) {
        placementController = new ShipPlacementController(frame, () -> startGame(frame, com, networkMode, iStart));
        frame.setStatus(networkMode ? "Verbindung hergestellt. Platziere deine Schiffe." : "Platziere deine Schiffe.");
    }

    private static void startNetworkConnection(MainFrame frame) {
        final int port = 50000;

        if (opponentType == OpponentType.HOST) {
            NetworkHandshakeController.startHost(frame, port, new NetworkHandshakeController.ReadyCallback() {
                @Override
                public void onReady(Com com, boolean iStart) {
                    startPlacement(frame, com, true, iStart);
                }

                @Override
                public void onError(String message, Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, message + ": " + e.getMessage());
                    opponentType = OpponentType.COMPUTER;
                    frame.setConnectionStatus("Starte lokales Spiel.");
                    frame.setLocalIpAddress("");
                    startPlacement(frame, null, false, false);
                }
            });
        } else if (opponentType == OpponentType.JOIN) {
            String host = JOptionPane.showInputDialog(frame, "Host IP:", Com.getLocalIpAddresses());
            if (host == null || host.trim().isEmpty()) {
                opponentType = OpponentType.COMPUTER;
                frame.setConnectionStatus("Kein Host ausgewählt. Starte lokales Spiel.");
                frame.setLocalIpAddress("");
                startPlacement(frame, null, false, false);
                return;
            }
            NetworkHandshakeController.startClient(frame, host, port, new NetworkHandshakeController.ReadyCallback() {
                @Override
                public void onReady(Com com, boolean iStart) {
                    startPlacement(frame, com, true, iStart);
                }

                @Override
                public void onError(String message, Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, message + ": " + e.getMessage());
                    opponentType = OpponentType.COMPUTER;
                    frame.setConnectionStatus("Starte lokales Spiel.");
                    frame.setLocalIpAddress("");
                    startPlacement(frame, null, false, false);
                }
            });
        }
    }

    private static GameDifficulty askDifficulty(MainFrame frame) {
        GameDifficulty[] options = GameDifficulty.values();
        String[] labels = Arrays.stream(options)
                .map(GameDifficulty::getDisplayName)
                .toArray(String[]::new);
        int choice = JOptionPane.showOptionDialog(
                frame,
                "Wähle die Schwierigkeit für das Spiel gegen den Computer.",
                "Schwierigkeit wählen",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                labels,
                labels[0]);

        if (choice >= 0 && choice < options.length) {
            return options[choice];
        }
        return GameDifficulty.EASY;
    }

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
}