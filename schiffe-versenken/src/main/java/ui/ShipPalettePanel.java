<<<<<<< HEAD:schiffe-versenken/src/main/java/ui/ShipPalettePanel.java
package ui;
=======
<<<<<<< HEAD:schiffe-versenken/src/main/java/view/ShipPalettePanel.java
package view;
=======
package ui;
>>>>>>> 4f43638 (added drag n drop for ships):schiffe-versenken/src/main/java/ui/ShipPalettePanel.java
>>>>>>> 44456cf (added drag n drop for ships):schiffe-versenken/src/main/java/view/ShipPalettePanel.java

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

<<<<<<< HEAD:schiffe-versenken/src/main/java/ui/ShipPalettePanel.java
=======
<<<<<<< HEAD:schiffe-versenken/src/main/java/view/ShipPalettePanel.java
import models.ShipOrientation;
import models.ShipType;

=======
>>>>>>> 4f43638 (added drag n drop for ships):schiffe-versenken/src/main/java/ui/ShipPalettePanel.java
>>>>>>> 44456cf (added drag n drop for ships):schiffe-versenken/src/main/java/view/ShipPalettePanel.java
public class ShipPalettePanel extends JPanel {
    private final EnumMap<ShipType, Integer> remainingCounts;
    private final EnumMap<ShipType, JLabel> labels;

    private ShipOrientation orientation;

    public ShipPalettePanel() {
        this.remainingCounts = new EnumMap<>(ShipType.class);
        this.labels = new EnumMap<>(ShipType.class);
        this.orientation = ShipOrientation.HORIZONTAL;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Schiffe ziehen"));
        setPreferredSize(new Dimension(190, 360));

        JLabel hintLabel = new JLabel("Drag & Drop");
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.BOLD, 14f));
        add(hintLabel);

        add(Box.createVerticalStrut(8));

        JLabel orientationLabel = new JLabel();
        orientationLabel.setName("orientationLabel");
        orientationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(orientationLabel);

        add(Box.createVerticalStrut(12));

        for (ShipType type : ShipType.values()) {
            remainingCounts.put(type, type.getAmount());

            JComponent shipSource = createShipSource(type);
            add(shipSource);
            add(Box.createVerticalStrut(8));
        }

        refreshLabels();
    }

    private JComponent createShipSource(ShipType shipType) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        panel.setMaximumSize(new Dimension(165, 58));
        panel.setPreferredSize(new Dimension(165, 58));
        panel.setBackground(new Color(232, 232, 232));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel nameLabel = new JLabel(shipType.getDisplayName(), SwingConstants.CENTER);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));

        JLabel detailLabel = new JLabel("", SwingConstants.CENTER);
        detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        labels.put(shipType, detailLabel);

        panel.add(Box.createVerticalGlue());
        panel.add(nameLabel);
        panel.add(detailLabel);
        panel.add(Box.createVerticalGlue());

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

    public void setRemainingCounts(Map<ShipType, Integer> newRemainingCounts) {
        for (ShipType type : ShipType.values()) {
            int value = newRemainingCounts.getOrDefault(type, 0);
            remainingCounts.put(type, value);
        }

        refreshLabels();
    }

    public void setOrientation(ShipOrientation orientation) {
        this.orientation = orientation;
        refreshLabels();
    }

    private int getRemainingCount(ShipType shipType) {
        return remainingCounts.getOrDefault(shipType, 0);
    }

    private void refreshLabels() {
        for (ShipType type : ShipType.values()) {
            JLabel label = labels.get(type);

            if (label != null) {
                int remaining = getRemainingCount(type);
                label.setText("Länge " + type.getSize() + " | übrig: " + remaining);
                label.setEnabled(remaining > 0);
            }
        }

        for (Component component : getComponents()) {
            if (component instanceof JLabel && "orientationLabel".equals(component.getName())) {
                JLabel label = (JLabel) component;
                label.setText("Ausrichtung: " + orientationText());
            }
        }

        repaint();
    }

    private String orientationText() {
        return orientation == ShipOrientation.HORIZONTAL ? "waagrecht" : "senkrecht";
    }
}