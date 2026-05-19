package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import models.ShipOrientation;
import models.ShipType;

public class ShipPalettePanel extends JPanel {

    // Größen der Palette und einzelner Schiff-Karten
    private static final Dimension PANEL_SIZE = new Dimension(210, 420);
    private static final Dimension SHIP_CARD_SIZE = new Dimension(175, 62);

    // Farben der Karten
    private static final Color CARD_BACKGROUND = new Color(232, 236, 240);
    private static final Color CARD_BORDER = new Color(80, 90, 100);

    // Speichert, wie viele Schiffe pro Typ noch verfügbar sind
    private final EnumMap<ShipType, Integer> remainingCounts;

    // Labels für die Anzeige von Länge und Restanzahl
    private final EnumMap<ShipType, JLabel> labels;

    // Zeigt die aktuelle Schiffsausrichtung an
    private final JLabel orientationLabel;

    // Aktuelle Ausrichtung für neu gezogene Schiffe
    private ShipOrientation orientation;

    public ShipPalettePanel() {
        this.remainingCounts = new EnumMap<>(ShipType.class);
        this.labels = new EnumMap<>(ShipType.class);
        this.orientation = ShipOrientation.HORIZONTAL;

        // Vertikale Anordnung der Palette
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Schiffe ziehen"));

        setMinimumSize(new Dimension(185, 320));
        setPreferredSize(PANEL_SIZE);
        setMaximumSize(new Dimension(230, Integer.MAX_VALUE));

        JLabel hintLabel = new JLabel("Drag & Drop");
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.BOLD, 14f));

        orientationLabel = new JLabel();
        orientationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalStrut(8));
        add(hintLabel);
        add(Box.createVerticalStrut(8));
        add(orientationLabel);
        add(Box.createVerticalStrut(14));

        // Für jeden Schiffstyp eine Drag-and-Drop-Karte erstellen
        for (ShipType type : ShipType.values()) {
            remainingCounts.put(type, type.getAmount());

            JComponent shipSource = createShipSource(type);
            add(shipSource);
            add(Box.createVerticalStrut(8));
        }

        // Nimmt übrigen Platz auf, damit Karten oben bleiben
        add(Box.createVerticalGlue());

        refreshLabels();
    }

    /*
     * Erstellt eine Schiff-Karte.
     * Von dieser Karte kann ein Schiff per Drag-and-Drop gezogen werden.
     */
    private JComponent createShipSource(ShipType shipType) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        panel.setMinimumSize(SHIP_CARD_SIZE);
        panel.setPreferredSize(SHIP_CARD_SIZE);
        panel.setMaximumSize(SHIP_CARD_SIZE);

        panel.setBackground(CARD_BACKGROUND);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(shipType.getDisplayName(), SwingConstants.CENTER);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));

        JLabel detailLabel = new JLabel("", SwingConstants.CENTER);
        detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        labels.put(shipType, detailLabel);

        panel.add(Box.createVerticalGlue());
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(detailLabel);
        panel.add(Box.createVerticalGlue());

        /*
         * TransferHandler erzeugt die Daten für Drag-and-Drop.
         * Ist kein Schiff dieses Typs mehr übrig, wird nichts gezogen.
         */
        panel.setTransferHandler(new TransferHandler() {
            @Override
            protected Transferable createTransferable(JComponent c) {
                if (getRemainingCount(shipType) <= 0) {
                    return null;
                }

                return new ShipTransferable(new ShipDragData(shipType, orientation));
            }

            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }
        });

        // Startet Drag-and-Drop beim Drücken der Maustaste
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (getRemainingCount(shipType) <= 0) {
                    return;
                }

                JComponent component = (JComponent) e.getSource();
                TransferHandler handler = component.getTransferHandler();
                handler.exportAsDrag(component, e, TransferHandler.COPY);
            }
        });

        return panel;
    }

    // Aktualisiert die Restanzahl aller Schiffstypen
    public void setRemainingCounts(Map<ShipType, Integer> newRemainingCounts) {
        for (ShipType type : ShipType.values()) {
            int value = newRemainingCounts.getOrDefault(type, 0);
            remainingCounts.put(type, value);
        }

        refreshLabels();
    }

    // Setzt die aktuelle Ausrichtung der Schiffe
    public void setOrientation(ShipOrientation orientation) {
        this.orientation = orientation;
        refreshLabels();
    }

    // Gibt zurück, wie viele Schiffe eines Typs übrig sind
    private int getRemainingCount(ShipType shipType) {
        return remainingCounts.getOrDefault(shipType, 0);
    }

    // Aktualisiert Texte und Aktivierung der Labels
    private void refreshLabels() {
        for (ShipType type : ShipType.values()) {
            JLabel label = labels.get(type);

            if (label != null) {
                int remaining = getRemainingCount(type);

                label.setText("Länge " + type.getSize() + " | übrig: " + remaining);
                label.setEnabled(remaining > 0);
            }
        }

        orientationLabel.setText("Ausrichtung: " + orientationText());

        repaint();
    }

    // Wandelt die Ausrichtung in lesbaren Text um
    private String orientationText() {
        return orientation == ShipOrientation.HORIZONTAL ? "waagrecht" : "senkrecht";
    }
}