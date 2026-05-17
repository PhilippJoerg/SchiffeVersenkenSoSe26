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

    private static final String START_SCREEN = "START_SCREEN";
    private static final String GAME_SCREEN = "GAME_SCREEN";

    private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(950, 650);

    private final CardLayout screenLayout;
    private final JPanel screenPanel;
    private final StartScreenPanel startScreenPanel;

    // Linke Drag-and-Drop-Palette für verfügbare Schiffe
    private final ShipPalettePanel shipPalettePanel;

    // Eigenes Spielfeld
    private final BoardPanel ownBoard;

    // Gegnerisches Spielfeld
    private final BoardPanel enemyBoard;

    // Statusanzeige unten im Fenster
    private final JLabel statusLabel;

    // Button zum Drehen der Schiffe
    private final JButton rotateButton;

    // Button zum Schießen auf das Gegnerfeld
    private final JButton shootButton;

    public MainFrame() {
        super("Schiffe versenken");

        shipPalettePanel = new ShipPalettePanel();
        ownBoard = new BoardPanel(false);
        enemyBoard = new BoardPanel(true);

        statusLabel = new JLabel("Bereit.");
        rotateButton = new JButton("Drehen");
        shootButton = new JButton("Schießen");

        startScreenPanel = new StartScreenPanel();
        JPanel gamePanel = createGamePanel();

        screenLayout = new CardLayout();
        screenPanel = new JPanel(screenLayout);
        screenPanel.add(startScreenPanel, START_SCREEN);
        screenPanel.add(gamePanel, GAME_SCREEN);

        setContentPane(screenPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        pack();

        setMinimumSize(MINIMUM_WINDOW_SIZE);
        if (getWidth() < MINIMUM_WINDOW_SIZE.width || getHeight() < MINIMUM_WINDOW_SIZE.height) {
            setSize(MINIMUM_WINDOW_SIZE);
        }

        setLocationRelativeTo(null);
        showStartScreen();
    }

    private JPanel createGamePanel() {
        JPanel rootPanel = new JPanel(new BorderLayout(16, 16));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel centerPanel = new JPanel(new GridBagLayout());

        JPanel ownBoardPanel = createBoardPanel("Eigenes Feld", ownBoard, rotateButton);
        JPanel enemyBoardPanel = createBoardPanel("Gegnerfeld", enemyBoard, shootButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.insets.set(0, 0, 0, 8);
        centerPanel.add(ownBoardPanel, gbc);

        gbc.gridx = 1;
        gbc.insets.set(0, 8, 0, 0);
        centerPanel.add(enemyBoardPanel, gbc);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 14f));

        rootPanel.add(shipPalettePanel, BorderLayout.WEST);
        rootPanel.add(centerPanel, BorderLayout.CENTER);
        rootPanel.add(statusLabel, BorderLayout.SOUTH);

        return rootPanel;
    }

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

    public void showStartScreen() {
        screenLayout.show(screenPanel, START_SCREEN);
    }

    public void showGameScreen() {
        screenLayout.show(screenPanel, GAME_SCREEN);
    }

    public void setStartAction(Runnable action) {
        startScreenPanel.setStartAction(action);
    }

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

    // Verknüpft einen Klick-Listener mit dem eigenen Feld
    public void setOwnBoardClickListener(BoardClickListener listener) {
        ownBoard.setBoardClickListener(listener);
    }

    // Verknüpft einen Klick-Listener mit dem Gegnerfeld
    public void setEnemyBoardClickListener(BoardClickListener listener) {
        enemyBoard.setBoardClickListener(listener);
    }

    // Verknüpft Drag-and-Drop mit dem eigenen Feld
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

    // Aktualisiert die Restanzahl in der linken Schiffspalette
    public void setShipPaletteRemainingCounts(Map<ShipType, Integer> remainingCounts) {
        shipPalettePanel.setRemainingCounts(remainingCounts);
    }

    // Synchronisiert die Ausrichtung der Drag-and-Drop-Schiffe mit dem Drehen-Button
    public void setShipPaletteOrientation(ShipOrientation orientation) {
        shipPalettePanel.setOrientation(orientation);
    }

    public BoardPanel getOwnBoard() {
        return ownBoard;
    }

    public BoardPanel getEnemyBoard() {
        return enemyBoard;
    }

    public ShipPalettePanel getShipPalettePanel() {
        return shipPalettePanel;
    }
}