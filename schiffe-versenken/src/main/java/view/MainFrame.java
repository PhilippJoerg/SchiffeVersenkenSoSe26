/*
 * Datei: view/MainFrame.java
 * Hauptfenster der Anwendung: setzt die Views zusammen (Schiffspalette, Boards, Statusleisten)
 * und bietet Methoden zur Interaktion für Controller.
 */
package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.util.EnumMap;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.TransferHandler;

import models.CellState;
import models.ShipOrientation;
import models.ShipType;
import models.GameSettings;

/**
 * de: Die Klasse MainFrame ist das Hauptfenster der Anwendung und setzt die Views zusammen.
 * en: The MainFrame class is the main window of the application and assembles the views.
 */
public class MainFrame extends JFrame implements view.GameView, view.PlacementView, view.ConnectionView {

    // Namen der Screens im CardLayout
    private static final String START_SCREEN = "START_SCREEN";
    private static final String GAME_SCREEN = "GAME_SCREEN";

    // Mindestgröße des Fensters
    private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(950, 650);

    /**
     * de: Das Enum ScreenMode.
     * en: The enum ScreenMode.
     */
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
    private JMenuItem saveMenuItem;
    private JMenuItem loadMenuItem;

    /**
     * de: Konstruktor der Klasse MainFrame.
     * en: Constructor of the MainFrame class.
     */
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
        // TODO: refactor: shootButton logic should be removed completely, since i disabled it. Broader refactoring across view/controller is needed
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
        setJMenuBar(createMenuBar());
    }

    /**
     * de: Erstellt die Menüleiste.
     * en: Creates the menu bar.
     *
     * @return de: Die erstellte Menüleiste. en: The created menu bar.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("Datei");
        saveMenuItem = new JMenuItem("Speichern...");
        loadMenuItem = new JMenuItem("Laden...");
        file.add(saveMenuItem);
        file.add(loadMenuItem);
        menuBar.add(file);
        return menuBar;
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
        JPanel enemyBoardPanel = createBoardPanel("Gegnerfeld", enemyBoard, createActionSpacer(shootButton));

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

    /**
     * de: Erstellt ein Panel mit Hintergrundbild.
     * en: Creates a panel with a background image.
     *
     * @return de: Das erstellte Panel. en: The created panel.
     */
    private JPanel createBackgroundPanel() {
        return new JPanel(new BorderLayout()) {
            /**
             * de: Überschreibt die Methode paintComponent, um das Hintergrundbild zu zeichnen.
             * en: Overrides the paintComponent method to draw the background image.
             *
             * @param g de: Der Graphics-Kontext. en: The graphics context.
             */
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

    /**
     * de: Lädt ein Hintergrundbild.
     * en: Loads a background image.
     *
     * @param path de: Der Pfad zum Bild. en: The path to the image.
     * @return de: Das geladene Bild. en: The loaded image.
     */
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

    /**
     * de: Erstellt ein Panel für ein Spielbrett.
     * en: Creates a panel for a game board.
     *
     * @param title de: Der Titel des Panels. en: The title of the panel.
     * @param boardPanel de: Das Spielbrett-Panel. en: The game board panel.
     * @param actionComponents de: Die Aktionskomponenten. en: The action components.
     * @return de: Das erstellte Panel. en: The created panel.
     */
    private JPanel createBoardPanel(String title, BoardPanel boardPanel, Component... actionComponents) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setOpaque(false);
        for (Component actionComponent : actionComponents) {
            buttonPanel.add(actionComponent);
        }

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(boardPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * de: Erstellt einen Abstandshalter basierend auf der Größe eines Buttons.
     * en: Creates a spacer based on the size of a button.
     *
     * @param templateButton de: Der Button, dessen Größe als Vorlage dient. en: The button whose size is used as a template.
     * @return de: Der erstellte Abstandshalter. en: The created spacer.
     */
    private Component createActionSpacer(JButton templateButton) {
        return Box.createRigidArea(templateButton.getPreferredSize());
    }

    /**
     * de: Zeigt den Startscreen an.
     * en: Shows the start screen.
     *
     */
    public final void showStartScreen() {
        currentScreen = ScreenMode.START;
        screenLayout.show(screenPanel, START_SCREEN);
        backgroundPanel.repaint();
    }

    /**
     * de: Zeigt den Spielscreen an.
     * en: Shows the game screen.
     *
     */
    public void showGameScreen() {
        currentScreen = ScreenMode.GAME;
        screenLayout.show(screenPanel, GAME_SCREEN);
        backgroundPanel.repaint();
    }

    /**
     * de: Setzt die Aktion des Start-Buttons.
     * en: Sets the action of the start button.
     *
     * @param action de: Die Aktion, die beim Klicken des Start-Buttons ausgeführt wird. en: The action to be performed when the start button is clicked.
     */
    public void setStartAction(Runnable action) {
        startScreenPanel.setStartAction(action);
    }

    /**
     * de: Gibt den Text aus dem Startscreen zurück.
     * en: Returns the text from the start screen.
     *
     * @return de: Der Text aus dem Startscreen. en: The text from the start screen.
     */
    public String getStartScreenText() {
        return startScreenPanel.getTextFieldValue();
    }

    /**
     * de: Gibt den ausgewählten Gegner zurück.
     * en: Returns the selected opponent.
     *
     * @return de: Der ausgewählte Gegner. en: The selected opponent.
     */
    public String getSelectedOpponent() {
        return startScreenPanel.getOpponentSelection();
    }

    /**
     * de: Gibt die ausgewählte Schwierigkeit zurück.
     * en: Returns the selected difficulty.
     *
     * @return de: Die ausgewählte Schwierigkeit. en: The selected difficulty.
     */
    public models.GameDifficulty getSelectedDifficulty() {
        return startScreenPanel.getSelectedDifficulty();
    }

    /**
     * de: Gibt die Spiel-Einstellungen zurück.
     * en: Returns the game settings.
     *
     * @return de: Die Spiel-Einstellungen. en: The game settings.
     */
    public GameSettings getGameSettings() {
        EnumMap<ShipType, Integer> shipCounts = new EnumMap<>(ShipType.class);

        shipCounts.put(ShipType.BATTLESHIP, startScreenPanel.getBattleshipCount());
        shipCounts.put(ShipType.CRUISER, startScreenPanel.getCruiserCount());
        shipCounts.put(ShipType.DESTROYER, startScreenPanel.getDestroyerCount());
        shipCounts.put(ShipType.SUBMARINE, startScreenPanel.getSubmarineCount());

        return new GameSettings(startScreenPanel.getBoardSize(), shipCounts);
    }

    /**
     * de: Gibt die IP-Adresse des Hosts zurück.
     * en: Returns the host's IP address.
     *
     * @return de: Die IP-Adresse des Hosts. en: The host's IP address.
     */
    public String getHostIpAddress() {
        return startScreenPanel.getHostIpAddress();
    }

    /**
     * de: Setzt den Text der Statusleiste.
     * en: Sets the text of the status bar.
     *
     * @param text de: Der Text, der in der Statusleiste angezeigt werden soll. en: The text to be displayed in the status bar.
     */
    public void setStatus(String text) {
        statusLabel.setText(text);
    }

    /**
     * de: Setzt den Text des Verbindungsstatus.
     * en: Sets the text of the connection status.
     *
     * @param text de: Der Text, der im Verbindungsstatus angezeigt werden soll. en: The text to be displayed in the connection status.
     */
    public void setConnectionStatus(String text) {
        connectionLabel.setText(text);
    }

    /**
     * de: Setzt die lokale IP-Adresse.
     * en: Sets the local IP address.
     *
     * @param text de: Die lokale IP-Adresse. en: The local IP address.
     */
    public void setLocalIpAddress(String text) {
        localIpLabel.setText(text);
    }

    /**
     * de: Setzt den Ladezustand.
     * en: Sets the loading state.
     *
     * @param loading de: Der Ladezustand. en: The loading state.
     */
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

    /**
     * de: Setzt das eigene Spielfeld.
     * en: Sets the own board.
     *
     * @param cells de: Die Zellen des eigenen Spielfelds. en: The cells of the own board.
     */
    public void setOwnBoard(CellState[][] cells) {
        ownBoard.setCells(cells);
    }

    /**
     * de: Setzt das gegnerische Spielfeld.
     * en: Sets the enemy board.
     *
     * @param cells de: Die Zellen des gegnerischen Spielfelds. en: The cells of the enemy board.
     */
    public void setEnemyBoard(CellState[][] cells) {
        enemyBoard.setCells(cells);
    }

    /**
     * de: Setzt den Klick-Listener für das eigene Spielfeld.
     * en: Sets the click listener for the own board.
     *
     * @param listener de: Der Klick-Listener für das eigene Spielfeld. en: The click listener for the own board.
     */
    public void setOwnBoardClickListener(BoardClickListener listener) {
        ownBoard.setBoardClickListener(listener);
    }

    /**
     * de: Setzt den Klick-Listener für das Gegnerfeld.
     * en: Sets the click listener for the enemy board.
     *
     * @param listener de: Der Klick-Listener für das Gegnerfeld. en: The click listener for the enemy board.
     */
    public void setEnemyBoardClickListener(BoardClickListener listener) {
        enemyBoard.setBoardClickListener(listener);
    }

    /**
     * de: Setzt den Transfer-Handler für das eigene Spielfeld. Aktiviert Drag-and-Drop auf dem eigenen Feld
     * en: Sets the transfer handler for the own board. Enables drag-and-drop on the own board.
     *
     * @param transferHandler de: Der Transfer-Handler für das eigene Spielfeld. en: The transfer handler for the own board.
     */
    public void setOwnBoardTransferHandler(TransferHandler transferHandler) {
        ownBoard.setTransferHandler(transferHandler);
    }

    /**
     * de: Setzt die Aktion für den Drehen-Button.
     * en: Sets the action for the rotate button.
     *
     * @param action de: Die Aktion für den Drehen-Button. en: The action for the rotate button.
     */
    public void setRotateAction(Runnable action) {
        rotateButton.addActionListener(e -> action.run());
    }

    /**
     * de: Setzt die Aktion für den Auto-Platzieren-Button.
     * en: Sets the action for the auto-place button.
     *
     * @param action de: Die Aktion für den Auto-Platzieren-Button. en: The action for the auto-place button.
     */
    public void setAutoPlaceAction(Runnable action) {
        autoPlaceButton.addActionListener(e -> action.run());
    }

    /**
     * de: Setzt die Aktion für den Schießen-Button.
     * en: Sets the action for the shoot button.
     *
     * @param action de: Die Aktion für den Schießen-Button. en: The action for the shoot button.
     */
    public void setShootAction(Runnable action) {
        shootButton.addActionListener(e -> action.run());
    }

    /**
     * de: Setzt die Aktion für den Speichern-Button.
     * en: Sets the action for the save button.
     *
     * @param action de: Die Aktion für den Speichern-Button. en: The action for the save button.
     */
    public void setSaveAction(Runnable action) {
        if (saveMenuItem != null) {
            for (var listener : saveMenuItem.getActionListeners()) {
                saveMenuItem.removeActionListener(listener);
            }
            saveMenuItem.addActionListener(e -> action.run());
        }
    }

    /**
     * de: Setzt die Aktion für den Laden-Button.
     * en: Sets the action for the load button.
     *
     * @param action de: Die Aktion für den Laden-Button. en: The action for the load button.
     */
    public void setLoadAction(Runnable action) {
        if (loadMenuItem != null) {
            for (var listener : loadMenuItem.getActionListeners()) {
                loadMenuItem.removeActionListener(listener);
            }
            loadMenuItem.addActionListener(e -> action.run());
        }
    }

    /**
     * de: Setzt, ob der Drehen-Button aktiviert ist.
     * en: Sets whether the rotate button is enabled.
     *
     * @param enabled de: Gibt an, ob der Button aktiviert ist. en: Indicates whether the button is enabled.
     */
    public void setRotateButtonEnabled(boolean enabled) {
        rotateButton.setEnabled(enabled);
    }

    /**
     * de: Setzt, ob der Auto-Platzieren-Button aktiviert ist.
     * en: Sets whether the auto-place button is enabled.
     *
     * @param enabled de: Gibt an, ob der Button aktiviert ist. en: Indicates whether the button is enabled.
     */
    public void setAutoPlaceButtonEnabled(boolean enabled) {
        autoPlaceButton.setEnabled(enabled);
    }

    /**
     * de: Setzt, ob der Schießen-Button aktiviert ist.
     * en: Sets whether the shoot button is enabled.
     *
     * @param enabled de: Gibt an, ob der Button aktiviert ist. en: Indicates whether the button is enabled.
     */
    public void setShootButtonEnabled(boolean enabled) {
        shootButton.setEnabled(enabled);
    }

    /**
     * de: Setzt die verbleibenden Schiffszahlen in der Palette.
     * en: Sets the remaining ship counts in the palette.
     *
     * @param remainingCounts de: Die verbleibenden Schiffszahlen. en: The remaining ship counts.
     */
    public void setShipPaletteRemainingCounts(Map<ShipType, Integer> remainingCounts) {
        shipPalettePanel.setRemainingCounts(remainingCounts);
    }

    /**
     * de: Setzt die Ausrichtung der Schiffe in der Palette.
     * en: Sets the orientation of the ships in the palette.
     *
     * @param orientation de: Die Ausrichtung der Schiffe. en: The orientation of the ships.
     */
    public void setShipPaletteOrientation(ShipOrientation orientation) {
        shipPalettePanel.setOrientation(orientation);
    }

    /**
     * de: Gibt das eigene Board zurück.
     * en: Returns the own board.
     *
     * @return de: Das eigene Board. en: The own board.
     */
    public BoardPanel getOwnBoard() {
        return ownBoard;
    }

    /**
     * de: Gibt das Gegnerboard zurück.
     * en: Returns the enemy board.
     *
     * @return de: Das Gegnerboard. en: The enemy board.
     */
    public BoardPanel getEnemyBoard() {
        return enemyBoard;
    }

    /**
     * de: Gibt die Schiffspalette zurück.
     * en: Returns the ship palette.
     *
     * @return de: Die Schiffspalette. en: The ship palette.
     */
    public ShipPalettePanel getShipPalettePanel() {
        return shipPalettePanel;
    }
}
