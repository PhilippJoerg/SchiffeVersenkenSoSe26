package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import models.GameDifficulty;

public class StartScreenPanel extends JPanel {

    private static final int DEFAULT_BOARD_SIZE = 10;
    private static final String[] SHIP_NAMES = {"Schlachtschiff", "Kreuzer", "Zerstörer", "U-Boot"};
    private static final int[] SHIP_SIZES = {5, 4, 3, 2};

    private final JTextField nameTextField;
    private final JButton startButton;
    private final JButton settingsButton;

    private String opponentSelection = "COMPUTER"; // COMPUTER, HOST, JOIN
    private GameDifficulty selectedDifficulty = GameDifficulty.EASY;
    private String hostIpAddress = "";

    private int boardSize = DEFAULT_BOARD_SIZE;
    private int battleshipCount = 1;
    private int cruiserCount = 2;
    private int destroyerCount = 3;
    private int submarineCount = 4;

    public StartScreenPanel() {
        setOpaque(false);
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

    private void showSettingsDialog() {
        String[] temporaryOpponentSelection = {opponentSelection};
        GameDifficulty[] temporaryDifficulty = {selectedDifficulty};
        int[] temporaryBoardSize = {boardSize};
        int[] temporaryShipCounts = {battleshipCount, cruiserCount, destroyerCount, submarineCount};
        String[] temporaryHostIp = {hostIpAddress};

        JPanel settingsPanel = new JPanel(new BorderLayout(24, 0));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel opponentPanel = createOpponentPanel(temporaryOpponentSelection);
        JPanel difficultyPanel = createDifficultyPanel(temporaryDifficulty);
        JPanel hostIpPanel = createHostIpPanel(temporaryHostIp);
        JPanel boardSizePanel = createBoardSizePanel(temporaryBoardSize);
        JPanel shipSettingsPanel = createShipSettingsPanel(temporaryShipCounts);

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.add(opponentPanel, BorderLayout.WEST);
        topPanel.add(difficultyPanel, BorderLayout.EAST);
        topPanel.add(hostIpPanel, BorderLayout.SOUTH);

        settingsPanel.add(topPanel, BorderLayout.NORTH);
        settingsPanel.add(boardSizePanel, BorderLayout.WEST);
        settingsPanel.add(shipSettingsPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, settingsPanel, "Einstellungen", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            opponentSelection = temporaryOpponentSelection[0];
            selectedDifficulty = temporaryDifficulty[0];
            boardSize = temporaryBoardSize[0];
            battleshipCount = temporaryShipCounts[0];
            cruiserCount = temporaryShipCounts[1];
            destroyerCount = temporaryShipCounts[2];
            submarineCount = temporaryShipCounts[3];
        }
    }

    private JPanel createHostIpPanel(String[] temporaryHostIp) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Host-IP für Beitreten"));

        JTextField hostIpField = new JTextField(temporaryHostIp[0]);
        hostIpField.setColumns(16);
        hostIpField.setToolTipText("IP-Adresse des Hosts für den Beitritt eingeben");
        hostIpField.addActionListener(e -> temporaryHostIp[0] = hostIpField.getText().trim());
        hostIpField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                temporaryHostIp[0] = hostIpField.getText().trim();
            }
        });

        panel.add(hostIpField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOpponentPanel(String[] temporaryOpponentSelection) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Gegner"));

        JRadioButton computerBtn = new JRadioButton("Computer");
        JRadioButton hostBtn = new JRadioButton("Host (Netzwerk)");
        JRadioButton joinBtn = new JRadioButton("Beitreten (Netzwerk)");

        ButtonGroup bg = new ButtonGroup();
        bg.add(computerBtn);
        bg.add(hostBtn);
        bg.add(joinBtn);

        switch (temporaryOpponentSelection[0]) {
            case "HOST" ->
                hostBtn.setSelected(true);
            case "JOIN" ->
                joinBtn.setSelected(true);
            default ->
                computerBtn.setSelected(true);
        }

        computerBtn.addActionListener(e -> temporaryOpponentSelection[0] = "COMPUTER");
        hostBtn.addActionListener(e -> temporaryOpponentSelection[0] = "HOST");
        joinBtn.addActionListener(e -> temporaryOpponentSelection[0] = "JOIN");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 4, 2, 4);

        gbc.gridy = 0;
        panel.add(computerBtn, gbc);
        gbc.gridy = 1;
        panel.add(hostBtn, gbc);
        gbc.gridy = 2;
        panel.add(joinBtn, gbc);

        return panel;
    }

    private JPanel createDifficultyPanel(GameDifficulty[] temporaryDifficulty) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Schwierigkeit"));

        GameDifficulty[] options = GameDifficulty.values();
        JComboBox<GameDifficulty> combo = new JComboBox<>(options);
        combo.setSelectedItem(temporaryDifficulty[0]);
        combo.addActionListener(e -> temporaryDifficulty[0] = (GameDifficulty) combo.getSelectedItem());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 4, 2, 4);
        panel.add(combo, gbc);

        return panel;
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
            case 8 ->
                size8Button.setSelected(true);
            case 12 ->
                size12Button.setSelected(true);
            default ->
                size10Button.setSelected(true);
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
            addShipRow(panel, gbc, i, SHIP_NAMES[i], SHIP_SIZES[i], temporaryShipCounts, i);
        }

        return panel;
    }

    private void addShipRow(JPanel panel, GridBagConstraints gbc, int row, String shipName, int shipSize, int[] temporaryShipCounts, int shipIndex) {
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

    public String getOpponentSelection() {
        return opponentSelection;
    }

    public GameDifficulty getSelectedDifficulty() {
        return selectedDifficulty;
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

    public String getHostIpAddress() {
        return hostIpAddress == null ? "" : hostIpAddress.trim();
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
