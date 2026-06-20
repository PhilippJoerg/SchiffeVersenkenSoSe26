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
     * Erstellt einen lokalen Spiel-Controller für ein Einzelspiel.
     */
    public GameController(GameView frame, GameModel model, Runnable backToMainMenuAction) {
    this(frame, model, null, false, backToMainMenuAction);
}

    /**
     * Erstellt einen Spiel-Controller für ein Netzwerkspiel mit Verbindungsobjekt und Startreihenfolge.
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
     * Verarbeitet einen Schuss auf das Gegnerfeld und aktualisiert den Spielstatus.
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
                // Spieler über den gesendeten Schuss informieren und auf Rückmeldung vertrösten
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
                // Letztes Schiff versenkt → Spieler hat gewonnen
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
        // Daneben: kein Schiff getroffen → Zug geht an den Computer
        if (result == CellState.MISS) {
            frame.setStatus("Daneben. Computer denkt...");
            startComputerShootLoop(); // Computerlogik asynchron starten
        }
    }

    /**
     * Startet die Schusschleife für den Computergegner, damit dieser schießt bis er nicht getroffen hat.
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
     * Beendet den Zug des Computers und stoppt den zugehörigen Timer.
     */
    private void stopComputerTurn() {
        computerTurn = false;
        if (computerTimer != null) {
            computerTimer.stop();
            computerTimer = null;
        }
    }

    /**
     * Zeigt das Endbildschirm-Dialogfenster mit Titel und Nachricht an.
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
     * Richtet die Netzwerkereignisse für den Spielverlauf (Schuss, Antwort, Pass) ein.
     */
    private void setupNetworkHandlers() {
        com.setListener(new Com.Listener() {
            @Override
            public void onCoin(int coin) {
                // not used here
            }

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

            @Override
            public void onPass() {
                myTurnNetwork = true;
                frame.setStatus("Gegner hat nicht getroffen. Dein Zug.");
            }

            @Override
            public void onConnected() {
                // ignore
            }

            @Override
            public void onDisconnected() {
                frame.setStatus("Verbindung getrennt.");
            }
        });
    }
}