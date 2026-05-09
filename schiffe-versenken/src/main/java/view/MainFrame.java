package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import models.ShipOrientation;
import models.ShipType;

public class MainFrame extends JFrame {
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

        // Hauptlayout des Fensters
        setLayout(new BorderLayout(12, 12));

        // Mittlerer Bereich für beide Spielfelder
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));

        // Linker Board-Bereich: eigenes Feld + Drehen-Button
        JPanel ownBoardPanel = new JPanel();
        ownBoardPanel.setLayout(new BoxLayout(ownBoardPanel, BoxLayout.Y_AXIS));
        ownBoardPanel.add(ownBoard);

        JPanel rotatePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        rotatePanel.add(rotateButton);
        ownBoardPanel.add(rotatePanel);

        // Rechter Board-Bereich: Gegnerfeld + Schießen-Button
        JPanel enemyBoardPanel = new JPanel();
        enemyBoardPanel.setLayout(new BoxLayout(enemyBoardPanel, BoxLayout.Y_AXIS));
        enemyBoardPanel.add(enemyBoard);

        JPanel shootPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        shootPanel.add(shootButton);
        enemyBoardPanel.add(shootPanel);

        // Beide Boards nebeneinander ins Zentrum setzen
        centerPanel.add(ownBoardPanel);
        centerPanel.add(enemyBoardPanel);

        // Statusleiste unten optisch etwas absetzen
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 14f));

        add(shipPalettePanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(true);
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