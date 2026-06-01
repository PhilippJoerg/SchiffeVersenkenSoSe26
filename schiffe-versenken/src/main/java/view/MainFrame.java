/*
 * Datei: view/MainFrame.java
 * Hauptfenster der Anwendung: setzt die Views zusammen (Schiffspalette, Boards, Statusleisten)
 * und bietet Methoden zur Interaktion für Controller.
 */
package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.TransferHandler;

import models.CellState;
import models.ShipOrientation;
import models.ShipType;

// Linke Drag-and-Drop-Palette für verfügbare Schiffe

public class MainFrame extends JFrame implements view.GameView, view.PlacementView, view.ConnectionView {

    // Namen der Screens im CardLayout
    private static final String START_SCREEN = "START_SCREEN";
    private static final String GAME_SCREEN = "GAME_SCREEN";

    // Mindestgröße des Fensters
    private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(950, 650);

    private enum ScreenMode {
        START,
        GAME
    }

    // Layout zum Wechseln zwischen Start- und Spielscreen
    private final CardLayout screenLayout;
    private final JPanel screenPanel;
    private final StartScreenPanel startScreenPanel;

    // Linke Drag-and-Drop-Palette
    private final ShipPalettePanel shipPalettePanel;

    // Hintergrundbilder
    private final JPanel backgroundPanel;
    private BufferedImage startBackground;
    private BufferedImage gameBackground;
    private ScreenMode currentScreen = ScreenMode.START;
    private static final String START_BACKGROUND_PATH = "/images/startscreen-background.png";
    private static final String GAME_BACKGROUND_PATH = "/images/game-background.png";

    // Eigenes Spielfeld
    private final BoardPanel ownBoard;

    // Gegnerisches Spielfeld
    private final BoardPanel enemyBoard;

    // Verbindungssatus oben im Fenster
    private final JLabel connectionLabel;

    // Host-IP-Anzeige oben im Fenster
    private final JLabel localIpLabel;

    // Ladeanzeige oben im Fenster
    private final JProgressBar loadingBar;

    // Statusanzeige unten im Fenster
    private final JLabel statusLabel;

    // Button zum Drehen der Schiffe
    private final JButton rotateButton;


    // Button zum automatische Platzierung
    private final JButton autoPlaceButton;

    // Button zum Schießen auf das Gegnerfeld
    private final JButton shootButton;

    public MainFrame() {
        super("Schiffe versenken");

        // Zentrale UI-Komponenten erstellen
        shipPalettePanel = new ShipPalettePanel();
        ownBoard = new BoardPanel(false);
        enemyBoard = new BoardPanel(true);

        statusLabel = new JLabel("Bereit.");
        statusLabel.setForeground(Color.WHITE);
        connectionLabel = new JLabel("Nicht verbunden");
        connectionLabel.setForeground(Color.WHITE);
        localIpLabel = new JLabel("");
        localIpLabel.setForeground(Color.WHITE);
        loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        rotateButton = new JButton("Drehen");
        autoPlaceButton = new JButton("Auto-Platzieren");
        shootButton = new JButton("Schießen");

        startBackground = loadBackgroundImage(START_BACKGROUND_PATH);
        gameBackground = loadBackgroundImage(GAME_BACKGROUND_PATH);

        startScreenPanel = new StartScreenPanel();
        JPanel gamePanel = createGamePanel();

        // Startscreen und Spielscreen registrieren
        screenLayout = new CardLayout();
        screenPanel = new JPanel(screenLayout);
        screenPanel.setOpaque(false);
        startScreenPanel.setOpaque(false);
        screenPanel.add(startScreenPanel, START_SCREEN);
        screenPanel.add(gamePanel, GAME_SCREEN);

        backgroundPanel = createBackgroundPanel();
        backgroundPanel.add(createTopInfoPanel(), BorderLayout.NORTH);
        backgroundPanel.add(screenPanel, BorderLayout.CENTER);

        setContentPane(backgroundPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        // rotate and auto-place buttons are added inside the board panel
        
        // Fenstergröße anhand der Inhalte berechnen
        pack();

        // Mindestgröße erzwingen
        setMinimumSize(MINIMUM_WINDOW_SIZE);
        if (getWidth() < MINIMUM_WINDOW_SIZE.width || getHeight() < MINIMUM_WINDOW_SIZE.height) {
            setSize(MINIMUM_WINDOW_SIZE);
        }

        // Fenster zentrieren
        setLocationRelativeTo(null);

        // Anwendung startet mit dem Startscreen
        showStartScreen();
    }

    /*
     * Erstellt den Spielscreen.
     * Links liegt die Schiffspalette, mittig die beiden Boards,
     * unten die Statusleiste.
     */
    private JPanel createGamePanel() {
        JPanel rootPanel = new JPanel(new BorderLayout(16, 16));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        rootPanel.setOpaque(false);

        // Enthält eigenes Feld und Gegnerfeld nebeneinander
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        JPanel ownBoardPanel = createBoardPanel("Eigenes Feld", ownBoard, rotateButton, autoPlaceButton);
        JPanel enemyBoardPanel = createBoardPanel("Gegnerfeld", enemyBoard, shootButton);

        // Einstellungen für flexible Größenverteilung
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        // Eigenes Feld links
        gbc.gridx = 0;
        gbc.insets.set(0, 0, 0, 8);
        centerPanel.add(ownBoardPanel, gbc);

        // Gegnerfeld rechts
        gbc.gridx = 1;
        gbc.insets.set(0, 8, 0, 0);
        centerPanel.add(enemyBoardPanel, gbc);

        // Statusleiste optisch anpassen
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 14f));


        rootPanel.add(shipPalettePanel, BorderLayout.WEST);
        rootPanel.add(centerPanel, BorderLayout.CENTER);
        rootPanel.add(statusLabel, BorderLayout.SOUTH);

        return rootPanel;
    }

    /*
     * Erstellt ein Panel für ein Spielfeld.
     * Besteht aus Titel, Board und Button.
     */
    private JPanel createTopInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);
        panel.add(connectionLabel, BorderLayout.WEST);
        panel.add(localIpLabel, BorderLayout.EAST);
        localIpLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panel.add(loadingBar, BorderLayout.CENTER);
        loadingBar.setVisible(false);
        return panel;
    }

    private JPanel createBackgroundPanel() {
        return new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                BufferedImage image = currentScreen == ScreenMode.GAME ? gameBackground : startBackground;
                if (image == null) {
                    return;
                }

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int width = getWidth();
                int height = getHeight();
                if (width <= 0 || height <= 0) {
                    g2.dispose();
                    return;
                }

                double imageRatio = (double) image.getWidth() / image.getHeight();
                double panelRatio = (double) width / height;

                int drawWidth;
                int drawHeight;
                if (panelRatio > imageRatio) {
                    drawWidth = width;
                    drawHeight = (int) (width / imageRatio);
                } else {
                    drawHeight = height;
                    drawWidth = (int) (height * imageRatio);
                }

                int x = (width - drawWidth) / 2;
                int y = (height - drawHeight) / 2;
                g2.drawImage(image, x, y, drawWidth, drawHeight, this);
                g2.dispose();
            }
        };
    }

    private BufferedImage loadBackgroundImage(String path) {
        try {
            URL imageUrl = getClass().getResource(path);
            if (imageUrl == null) {
                System.err.println("Hintergrundbild nicht gefunden: " + path);
                return null;
            }
            return ImageIO.read(imageUrl);
        } catch (IOException e) {
            System.err.println("Hintergrundbild konnte nicht gelesen werden: " + path + " - " + e.getMessage());
            return null;
        }
    }

    private JPanel createBoardPanel(String title, BoardPanel boardPanel, JButton... actionButtons) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setOpaque(false);
        for (JButton b : actionButtons) {
            buttonPanel.add(b);
        }

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(boardPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Zeigt den Startscreen
    public final void showStartScreen() {
        currentScreen = ScreenMode.START;
        screenLayout.show(screenPanel, START_SCREEN);
        backgroundPanel.repaint();
    }

    // Zeigt den Spielscreen
    public void showGameScreen() {
        currentScreen = ScreenMode.GAME;
        screenLayout.show(screenPanel, GAME_SCREEN);
        backgroundPanel.repaint();
    }

    // Setzt die Aktion des Start-Buttons
    public void setStartAction(Runnable action) {
        startScreenPanel.setStartAction(action);
    }

    // Gibt den Text aus dem Startscreen zurück
    public String getStartScreenText() {
        return startScreenPanel.getTextFieldValue();
    }

    public String getSelectedOpponent() {
        return startScreenPanel.getOpponentSelection();
    }

    public models.GameDifficulty getSelectedDifficulty() {
        return startScreenPanel.getSelectedDifficulty();
    }

    public String getHostIpAddress() {
        return startScreenPanel.getHostIpAddress();
    }

    // Setzt den Text der Statusleiste
    public void setStatus(String text) {
        statusLabel.setText(text);
    }

    public void setConnectionStatus(String text) {
        connectionLabel.setText(text);
    }

    public void setLocalIpAddress(String text) {
        localIpLabel.setText(text);
    }

    public void setLoading(boolean loading) {
        loadingBar.setVisible(loading);
        if (loading) {
            loadingBar.setString("Warte auf Verbindung...");
            loadingBar.setStringPainted(true);
        } else {
            loadingBar.setString("");
            loadingBar.setStringPainted(false);
        }
    }

    // Aktualisiert das eigene Spielfeld
    public void setOwnBoard(CellState[][] cells) {
        ownBoard.setCells(cells);
    }

    // Aktualisiert das gegnerische Spielfeld
    public void setEnemyBoard(CellState[][] cells) {
        enemyBoard.setCells(cells);
    }

    // Setzt den Klick-Listener für das eigene Feld
    public void setOwnBoardClickListener(BoardClickListener listener) {
        ownBoard.setBoardClickListener(listener);
    }

    // Setzt den Klick-Listener für das Gegnerfeld
    public void setEnemyBoardClickListener(BoardClickListener listener) {
        enemyBoard.setBoardClickListener(listener);
    }

    // Aktiviert Drag-and-Drop auf dem eigenen Feld
    public void setOwnBoardTransferHandler(TransferHandler transferHandler) {
        ownBoard.setTransferHandler(transferHandler);
    }

    // Setzt die Aktion für den Drehen-Button
    public void setRotateAction(Runnable action) {
        rotateButton.addActionListener(e -> action.run());
    }

    // Setzt die Aktion für den Auto-Platzieren-Button
    public void setAutoPlaceAction(Runnable action) {
        autoPlaceButton.addActionListener(e -> action.run());
    }

    // Setzt die Aktion für den Schießen-Button
    public void setShootAction(Runnable action) {
        shootButton.addActionListener(e -> action.run());
    }

    // Aktiviert oder deaktiviert den Drehen-Button
    public void setRotateButtonEnabled(boolean enabled) {
        rotateButton.setEnabled(enabled);
    }

    public void setAutoPlaceButtonEnabled(boolean enabled) {
        autoPlaceButton.setEnabled(enabled);
    }

    // Aktiviert oder deaktiviert den Schießen-Button
    public void setShootButtonEnabled(boolean enabled) {
        shootButton.setEnabled(enabled);
    }

    // Aktualisiert die Restanzahl der Schiffe
    public void setShipPaletteRemainingCounts(Map<ShipType, Integer> remainingCounts) {
        shipPalettePanel.setRemainingCounts(remainingCounts);
    }

    // Aktualisiert die Ausrichtung der Schiffe in der Palette
    public void setShipPaletteOrientation(ShipOrientation orientation) {
        shipPalettePanel.setOrientation(orientation);
    }

    // Gibt das eigene Board zurück
    public BoardPanel getOwnBoard() {
        return ownBoard;
    }

    // Gibt das Gegnerboard zurück
    public BoardPanel getEnemyBoard() {
        return enemyBoard;
    }

    // Gibt die Schiffspalette zurück
    public ShipPalettePanel getShipPalettePanel() {
        return shipPalettePanel;
    }
}