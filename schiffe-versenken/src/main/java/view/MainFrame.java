package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import models.CellState;
import models.ShipOrientation;
import models.ShipType;

public class MainFrame extends JFrame {

    // Namen der Screens im CardLayout
    private static final String START_SCREEN = "START_SCREEN";
    private static final String GAME_SCREEN = "GAME_SCREEN";

    // Mindestgröße des Fensters
    private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(950, 650);

    // Layout zum Wechseln zwischen Start- und Spielscreen
    private final CardLayout screenLayout;
    private final JPanel screenPanel;
    private final StartScreenPanel startScreenPanel;

    // Linke Drag-and-Drop-Palette
    private final ShipPalettePanel shipPalettePanel;

    // Eigenes Spielfeld
    private final BoardPanel ownBoard;

    // Gegnerisches Spielfeld
    private final BoardPanel enemyBoard;

    // Statusanzeige unten im Fenster
    private final JLabel statusLabel;

    // Button zum Drehen der Schiffe
    private final JButton rotateButton;

    // Button zum Schießen
    private final JButton shootButton;

    public MainFrame() {
        super("Schiffe versenken");

        // Zentrale UI-Komponenten erstellen
        shipPalettePanel = new ShipPalettePanel();
        ownBoard = new BoardPanel(false);
        enemyBoard = new BoardPanel(true);

        statusLabel = new JLabel("Bereit.");
        rotateButton = new JButton("Drehen");
        shootButton = new JButton("Schießen");

        startScreenPanel = new StartScreenPanel();
        JPanel gamePanel = createGamePanel();

        // Startscreen und Spielscreen registrieren
        screenLayout = new CardLayout();
        screenPanel = new JPanel(screenLayout);
        screenPanel.add(startScreenPanel, START_SCREEN);
        screenPanel.add(gamePanel, GAME_SCREEN);

        setContentPane(screenPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

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

        // Enthält eigenes Feld und Gegnerfeld nebeneinander
        JPanel centerPanel = new JPanel(new GridBagLayout());

        JPanel ownBoardPanel = createBoardPanel("Eigenes Feld", ownBoard, rotateButton);
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
    private JPanel createBoardPanel(String title, BoardPanel boardPanel, JButton actionButton) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.add(actionButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(boardPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Zeigt den Startscreen
    public void showStartScreen() {
        screenLayout.show(screenPanel, START_SCREEN);
    }

    // Zeigt den Spielscreen
    public void showGameScreen() {
        screenLayout.show(screenPanel, GAME_SCREEN);
    }

    // Setzt die Aktion des Start-Buttons
    public void setStartAction(Runnable action) {
        startScreenPanel.setStartAction(action);
    }

    // Gibt den Text aus dem Startscreen zurück
    public String getStartScreenText() {
        return startScreenPanel.getTextFieldValue();
    }

    // Setzt den Text der Statusleiste
    public void setStatus(String text) {
        statusLabel.setText(text);
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

    // Setzt die Aktion für den Schießen-Button
    public void setShootAction(Runnable action) {
        shootButton.addActionListener(e -> action.run());
    }

    // Aktiviert oder deaktiviert den Drehen-Button
    public void setRotateButtonEnabled(boolean enabled) {
        rotateButton.setEnabled(enabled);
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