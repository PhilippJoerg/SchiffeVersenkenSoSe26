package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class StartScreenPanel extends JPanel {

    private static final String BACKGROUND_IMAGE_PATH = "/images/startscreen-background.png";

    private static final int DEFAULT_BOARD_SIZE = 10;

    private static final String[] SHIP_NAMES = {
            "Träger",
            "Schlachtschiff",
            "Kreuzer",
            "U-Boot",
            "Zerstörer"
    };

    private static final int[] SHIP_SIZES = {
            5,
            4,
            3,
            3,
            2
    };

    private final JTextField nameTextField;
    private final JButton startButton;
    private final JButton settingsButton;

    private final BufferedImage backgroundImage;

    private int boardSize = DEFAULT_BOARD_SIZE;

    private int carrierCount = 1;
    private int battleshipCount = 1;
    private int cruiserCount = 2;
    private int submarineCount = 1;
    private int destroyerCount = 2;

    public StartScreenPanel() {
        backgroundImage = loadBackgroundImage();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("Schiffeversenken", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 42f));
        titleLabel.setForeground(Color.WHITE);

        nameTextField = new JTextField();
        nameTextField.setPreferredSize(new Dimension(260, 36));
        nameTextField.setHorizontalAlignment(SwingConstants.CENTER);
        nameTextField.setToolTipText("Spielername eingeben");

        startButton = new JButton("Spiel starten");
        startButton.setPreferredSize(new Dimension(260, 40));
        startButton.setFont(startButton.getFont().deriveFont(Font.BOLD, 15f));

        settingsButton = new JButton("Einstellungen");
        settingsButton.setPreferredSize(new Dimension(260, 40));
        settingsButton.setFont(settingsButton.getFont().deriveFont(Font.BOLD, 15f));
        settingsButton.addActionListener(e -> showSettingsDialog());

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        centerPanel.add(nameTextField, gbc);

        gbc.gridy = 1;
        centerPanel.add(startButton, gbc);

        gbc.gridy = 2;
        centerPanel.add(settingsButton, gbc);

        add(titleLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private BufferedImage loadBackgroundImage() {
        try {
            java.net.URL imageUrl = getClass().getResource(BACKGROUND_IMAGE_PATH);

            if (imageUrl == null) {
                System.err.println("Hintergrundbild nicht gefunden: " + BACKGROUND_IMAGE_PATH);
                return null;
            }

            return ImageIO.read(imageUrl);
        } catch (IOException e) {
            System.err.println("Hintergrundbild konnte nicht gelesen werden: " + BACKGROUND_IMAGE_PATH + " - " + e.getMessage());
            return null;
        }
    }

    private void showSettingsDialog() {
        int[] temporaryBoardSize = {boardSize};

        int[] temporaryShipCounts = {
                carrierCount,
                battleshipCount,
                cruiserCount,
                submarineCount,
                destroyerCount
        };

        JPanel settingsPanel = new JPanel(new BorderLayout(24, 0));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel boardSizePanel = createBoardSizePanel(temporaryBoardSize);
        JPanel shipSettingsPanel = createShipSettingsPanel(temporaryShipCounts);

        settingsPanel.add(boardSizePanel, BorderLayout.WEST);
        settingsPanel.add(shipSettingsPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this,
                settingsPanel,
                "Einstellungen",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            boardSize = temporaryBoardSize[0];

            carrierCount = temporaryShipCounts[0];
            battleshipCount = temporaryShipCounts[1];
            cruiserCount = temporaryShipCounts[2];
            submarineCount = temporaryShipCounts[3];
            destroyerCount = temporaryShipCounts[4];
        }
    }

    private JPanel createBoardSizePanel(int[] temporaryBoardSize) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Spielfeldgröße"));

        JRadioButton size8Button = new JRadioButton("8x8");
        JRadioButton size10Button = new JRadioButton("10x10");
        JRadioButton size12Button = new JRadioButton("12x12");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(size8Button);
        buttonGroup.add(size10Button);
        buttonGroup.add(size12Button);

        switch (temporaryBoardSize[0]) {
            case 8 -> size8Button.setSelected(true);
            case 12 -> size12Button.setSelected(true);
            default -> size10Button.setSelected(true);
        }

        size8Button.addActionListener(e -> temporaryBoardSize[0] = 8);
        size10Button.addActionListener(e -> temporaryBoardSize[0] = 10);
        size12Button.addActionListener(e -> temporaryBoardSize[0] = 12);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 6, 6, 6);

        gbc.gridy = 0;
        panel.add(size8Button, gbc);

        gbc.gridy = 1;
        panel.add(size10Button, gbc);

        gbc.gridy = 2;
        panel.add(size12Button, gbc);

        return panel;
    }

    private JPanel createShipSettingsPanel(int[] temporaryShipCounts) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Schiffe"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < SHIP_NAMES.length; i++) {
            addShipRow(
                    panel,
                    gbc,
                    i,
                    SHIP_NAMES[i],
                    SHIP_SIZES[i],
                    temporaryShipCounts,
                    i
            );
        }

        return panel;
    }

    private void addShipRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String shipName,
            int shipSize,
            int[] temporaryShipCounts,
            int shipIndex
    ) {
        JLabel nameLabel = new JLabel(shipName);
        JLabel sizeLabel = new JLabel("Größe " + shipSize);
        JLabel countLabel = new JLabel(String.valueOf(temporaryShipCounts[shipIndex]), SwingConstants.CENTER);

        JButton minusButton = new JButton("-");
        JButton plusButton = new JButton("+");

        minusButton.addActionListener(e -> {
            if (temporaryShipCounts[shipIndex] > 0) {
                temporaryShipCounts[shipIndex]--;
                countLabel.setText(String.valueOf(temporaryShipCounts[shipIndex]));
            }
        });

        plusButton.addActionListener(e -> {
            if (temporaryShipCounts[shipIndex] < 9) {
                temporaryShipCounts[shipIndex]++;
                countLabel.setText(String.valueOf(temporaryShipCounts[shipIndex]));
            }
        });

        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        counterPanel.add(minusButton);
        counterPanel.add(countLabel);
        counterPanel.add(plusButton);

        gbc.gridy = row;

        gbc.gridx = 0;
        panel.add(nameLabel, gbc);

        gbc.gridx = 1;
        panel.add(sizeLabel, gbc);

        gbc.gridx = 2;
        panel.add(counterPanel, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        double imageRatio = (double) backgroundImage.getWidth() / backgroundImage.getHeight();
        double panelRatio = (double) panelWidth / panelHeight;

        int drawWidth;
        int drawHeight;

        if (panelRatio > imageRatio) {
            drawWidth = panelWidth;
            drawHeight = (int) (panelWidth / imageRatio);
        } else {
            drawHeight = panelHeight;
            drawWidth = (int) (panelHeight * imageRatio);
        }

        int x = (panelWidth - drawWidth) / 2;
        int y = (panelHeight - drawHeight) / 2;

        g2.drawImage(backgroundImage, x, y, drawWidth, drawHeight, this);
        g2.dispose();
    }

    public String getPlayerName() {
        return nameTextField.getText().trim();
    }

    public String getTextFieldValue() {
        return nameTextField.getText().trim();
    }

    public int getBoardSize() {
        return boardSize;
    }

    public int getCarrierCount() {
        return carrierCount;
    }

    public int getBattleshipCount() {
        return battleshipCount;
    }

    public int getCruiserCount() {
        return cruiserCount;
    }

    public int getSubmarineCount() {
        return submarineCount;
    }

    public int getDestroyerCount() {
        return destroyerCount;
    }

    public void setStartAction(Runnable action) {
        startButton.addActionListener(e -> action.run());
        nameTextField.addActionListener(e -> action.run());
    }
}