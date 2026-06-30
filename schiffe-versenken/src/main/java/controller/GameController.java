package controller;

/*
 * Datei: controller/GameController.java
 * Steuert den Spielablauf: Verarbeitung von Schüssen, Computergegner-Loop und Netzwerkinteraktion.
 */
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import models.BoardUtils;
import models.CellState;
import models.GameModel;
import models.SaveLoad;
import view.GameView;

/**
 * de: Die Klasse GameController.
 * en: The class GameController.
 */
public class GameController {
    private final GameView frame;
    private final GameModel model;
    private boolean computerTurn = false;
    private Timer computerTimer;

    // Network support
    private Com com;
    private boolean networkMode = false;
    private boolean myTurnNetwork = false;
    private final Runnable backToMainMenuAction;
    private int pendingShotCol = -1;
    private int pendingShotRow = -1;

    /**
     * de: Erstellt einen lokalen Spiel-Controller für ein Einzelspiel.
     * en: Creates a local game controller for a single-player game.
     */
    public GameController(GameView frame, GameModel model, Runnable backToMainMenuAction) {
    this(frame, model, null, false, backToMainMenuAction);
}

    /**
     * de: Erstellt einen Spiel-Controller für ein Netzwerkspiel mit Verbindungsobjekt und Startreihenfolge.
     * en: Creates a network game controller with a connection object and starting order.
     */
    public GameController(GameView frame, GameModel model, Com com, boolean iStart, Runnable backToMainMenuAction) {
        this.frame = frame;
        this.backToMainMenuAction = backToMainMenuAction;
        this.model = model;
        this.com = com;
        this.networkMode = (com != null);
        this.myTurnNetwork = iStart;

        frame.setOwnBoard(model.getOwnBoard());
        frame.setEnemyBoard(model.getEnemyBoard());

        if (networkMode) {
            setupNetworkHandlers();
            frame.setStatus("Netzwerkspiel gestartet. " + (myTurnNetwork ? "Du beginnst." : "Gegner beginnt."));
        } else {
            frame.setStatus("Spiel gestartet. Klicke auf das gegnerische Feld, um zu schießen.");
        }

        frame.setEnemyBoardClickListener(this::handleShoot);
        frame.setShootButtonEnabled(false); // not needed, since clicking on board
        frame.setRotateButtonEnabled(false); // disable rotate during game
        frame.setAutoPlaceButtonEnabled(false); // disable auto-placement during game

        try {
            frame.setSaveAction(this::handleSave);
            frame.setLoadAction(this::handleLoad);
        } catch (Exception e) {
            // ignore if view doesn't support save/load
        }
    }

    /**
     * de: Führt die Speicheraktion aus.
     * en: Handles the save action.
     *
     */
    private void handleSave() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showSaveDialog(null);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".json")) {
            file = new File(file.getParentFile(), file.getName() + ".json");
        }
        try {
            SaveLoad.saveGame(file, model);
            JOptionPane.showMessageDialog(null, "Spiel gespeichert: " + file.getName(), "Speichern",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Speichern: " + e.getMessage(), "Fehler",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * de: Führt die Ladeaktion aus.
     * en: Handles the load action.
     *
     */
    private void handleLoad() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(null);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        try {
            GameModel loaded = SaveLoad.loadGame(file);
            model.restoreFrom(loaded);
            frame.setOwnBoard(model.getOwnBoard());
            frame.setEnemyBoard(model.getEnemyBoard());
            frame.setStatus("Spiel geladen: " + file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Laden: " + e.getMessage(), "Fehler",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * de: Verarbeitet einen Schuss auf das Gegnerfeld und aktualisiert den Spielstatus.
     * en: Handles a shot on the enemy board and updates the game status.
     */
    private void handleShoot(int col, int row) {
        // Schuss ignorieren, wenn das Spiel bereits vorbei ist oder der Computer gerade am Zug ist
        if (model.isGameOver() || computerTurn) {
            return;
        }
        // --- Netzwerkmodus ---
        if (networkMode) {
            // Im Netzwerkspiel nur schießen, wenn man selbst an der Reihe ist
            if (!myTurnNetwork) {
                frame.setStatus("Nicht dein Zug (Netzwerk).");
                return;
            }
            try {
                // Schuss-Koordinaten an den Gegner übertragen
                com.sendShot(col, row);
                // Koordinaten zwischenspeichern, um die eingehende Antwort
                // dem richtigen Feld zuordnen zu können
                pendingShotCol = col;
                pendingShotRow = row;
                // Spieler über den gesendeten Schuss informieren und auf Rückmeldung vertörsten
                frame.setStatus("Schuss gesendet: " + (char) ('A' + col) + (row + 1) + " — warte auf Antwort...");
            } catch (Exception e) {
                // Netzwerkfehler anzeigen; der Spielzug gilt als nicht ausgeführt
                frame.setStatus("Fehler beim Senden des Schusses: " + e.getMessage());
            }
            // Weitere lokale Verarbeitung erst nach Empfang der Server-Antwort (asynchron)
            return;
        }
        // --- Lokalmodus ---
        // Schuss im Modell registrieren; false = Feld bereits beschossen oder ungültige Koordinaten
        boolean shot = model.shoot(col, row);
        if (!shot) {
            frame.setStatus("Ungültiger Schuss.");
            return;
        }
        // Gegnerfeld in der UI nach dem Schuss neu zeichnen
        frame.setEnemyBoard(model.getEnemyBoard());
        // Ergebnis des Schusses aus dem aktualisierten Spielfeld lesen
        CellState result = model.getEnemyBoard()[col][row];
        // Treffer: Schiff getroffen
        if (result == CellState.HIT) {
            if (model.isGameOver()) {
                // Letztes Schiff versenkt -> Spieler hat gewonnen
                frame.setStatus("Du hast gewonnen!");
                showEndScreen("Spiel beendet", "Du hast gewonnen!");
            } else {
                if (BoardUtils.isShipSunkAt(model.getEnemyBoard(), col, row)) {
                    frame.setStatus("Du hast ein Schiff versenkt! Schieße weiter.");
                } else {
                    frame.setStatus("Treffer! Schieße weiter.");
                }
            }
            return;
        }
        // Daneben: kein Schiff getroffen -> Zug geht an den Computer
        if (result == CellState.MISS) {
            frame.setStatus("Daneben. Computer denkt...");
            startComputerShootLoop(); // Computerlogik asynchron starten
        }
    }

    /**
     * de: Startet die Schusschleife für den Computergegner, damit dieser schießt bis er nicht getroffen hat.
     * en: Start the firing loop for the computer opponent so that it continues to fire until it misses.
     */
    private void startComputerShootLoop() {
        computerTurn = true;
        computerTimer = new Timer(1000, e -> {
            if (model.isGameOver()) {
                stopComputerTurn();
                return;
            }

            int[] compShot = model.computerShoot();
            if (compShot == null) {
                frame.setStatus("Fehler: Computer konnte nicht schießen.");
                stopComputerTurn();
                return;
            }

            frame.setOwnBoard(model.getOwnBoard());
            String coord = (char) ('A' + compShot[0]) + String.valueOf(compShot[1] + 1);
            int outcome = compShot[2]; // 0=miss,1=hit,2=sunk

            if (outcome == 2) { // sunk
                if (model.isGameOver()) {
                    frame.setStatus("Computer hat bei " + coord + " ein Schiff versenkt und gewonnen!");
                    showEndScreen("Spiel beendet", "Computer hat gewonnen!");
                    stopComputerTurn();
                } else {
                    frame.setStatus("Computer hat bei " + coord + " ein Schiff versenkt. Computer denkt...");
                }
            } else if (outcome == 1) { // hit
                if (model.isGameOver()) {
                    frame.setStatus("Computer hat bei " + coord + " getroffen und gewonnen!");
                    showEndScreen("Spiel beendet", "Computer hat gewonnen!");
                    stopComputerTurn();
                } else {
                    frame.setStatus("Computer hat bei " + coord + " getroffen. Computer denkt...");
                }
            } else { // miss
                frame.setStatus("Computer hat bei " + coord + " daneben. Dein Zug.");
                stopComputerTurn();
            }
        });
        computerTimer.setRepeats(true);
        computerTimer.start();
    }

    /**
     * de: Beendet den Zug des Computers und stoppt den zugehörigen Timer.
     * en: Ends the computer's turn and stops the associated timer.
     */
    private void stopComputerTurn() {
        computerTurn = false;
        if (computerTimer != null) {
            computerTimer.stop();
            computerTimer = null;
        }
    }

    /**
     * de: Zeigt das Endbildschirm-Dialogfenster mit Titel und Nachricht an.
     * en: Shows the end screen dialog with a title and message.
     */
    private void showEndScreen(String title, String message) {
    Object[] options = {"Hauptmenü"};

    JOptionPane.showOptionDialog(
            null,
            message,
            title,
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]
    );

    backToMainMenuAction.run();
}

    /**
     * de: Richtet die Netzwerkereignisse für den Spielverlauf (Schuss, Antwort, Pass) ein.
     * en: Sets up the network event handlers for the game flow (shot, answer, pass).
     */
    private void setupNetworkHandlers() {
        com.setListener(new Com.Listener() {
            /**
             * de: Die Methode onCoin.
             * en: The method onCoin.
             *
             * @param coin de: Parameter coin. en: Parameter coin.
             */
            @Override
            public void onCoin(int coin) {
                // not used here
            }

            /**
             * de: Die Methode onShot.
             * en: The method onShot.
             *
             * @param col de: Parameter col. en: Parameter col.
             * @param row de: Parameter row. en: Parameter row.
             */
            @Override
            public void onShot(int col, int row) {
                int answer = model.evaluateIncomingShot(col, row);
                frame.setOwnBoard(model.getOwnBoard());
                try {
                    com.sendAnswer(answer);
                    if (answer == 0) {
                        myTurnNetwork = false;
                        frame.setStatus(
                                "Gegner hat bei " + (char) ('A' + col) + (row + 1) + " daneben. Warte auf pass.");
                    } else if (answer == 1) {
                        frame.setStatus("Gegner hat bei " + (char) ('A' + col) + (row + 1) + " getroffen.");
                    } else if (answer == 2) {
                        frame.setStatus("Gegner hat dich versenkt. Spielende.");
                        showEndScreen("Spiel beendet", "Gegner hat gewonnen!");
                    }
                } catch (Exception e) {
                    frame.setStatus("Fehler beim Senden der Antwort: " + e.getMessage());
                }
            }

            /**
             * de: Die Methode onAnswer.
             * en: The method onAnswer.
             *
             * @param answer de: Parameter answer. en: Parameter answer.
             */
            @Override
            public void onAnswer(int answer) {
                if (pendingShotCol >= 0 && pendingShotRow >= 0) {
                    CellState[][] enemy = model.getEnemyBoard();
                    CellState current = enemy[pendingShotCol][pendingShotRow];
                    // Reject if the cell was already shot locally
                    if (current == CellState.HIT || current == CellState.MISS) {
                        frame.setStatus("Ungültige Antwort: Feld bereits beschrieben.");
                    } else {
                        if (answer == 0) {
                            enemy[pendingShotCol][pendingShotRow] = CellState.MISS;
                            frame.setEnemyBoard(model.getEnemyBoard());
                            try {
                                com.sendPass();
                            } catch (Exception e) {
                                frame.setStatus("Fehler beim Senden von pass: " + e.getMessage());
                            }
                            myTurnNetwork = false;
                            frame.setStatus("Daneben. Gegner ist dran.");
                        } else if (answer == 1) {
                            enemy[pendingShotCol][pendingShotRow] = CellState.HIT;
                            frame.setEnemyBoard(model.getEnemyBoard());
                            frame.setStatus("Treffer! Du darfst weiter schießen.");
                        } else if (answer == 2) {
                            enemy[pendingShotCol][pendingShotRow] = CellState.HIT;
                            frame.setEnemyBoard(model.getEnemyBoard());
                            frame.setStatus("Treffer. Alle gegnerischen Schiffe versenkt. Du hast gewonnen!");
                            showEndScreen("Spiel beendet", "Du hast gewonnen!");
                        }
                    }
                    pendingShotCol = -1;
                    pendingShotRow = -1;
                }
            }

            /**
             * de: Die Methode onPass.
             * en: The method onPass.
             *
             */
            @Override
            public void onPass() {
                myTurnNetwork = true;
                frame.setStatus("Gegner hat nicht getroffen. Dein Zug.");
            }

            /**
             * de: Die Methode onConnected.
             * en: The method onConnected.
             *
             */
            @Override
            public void onConnected() {
                // ignore
            }

            /**
             * de: Die Methode onDisconnected.
             * en: The method onDisconnected.
             *
             */
            @Override
            public void onDisconnected() {
                frame.setStatus("Verbindung getrennt.");
            }
        });
    }
}