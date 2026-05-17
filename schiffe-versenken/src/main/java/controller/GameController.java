package controller;

import javax.swing.Timer;

import models.CellState;
import models.GameModel;
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
    private int pendingShotCol = -1;
    private int pendingShotRow = -1;

    public GameController(GameView frame, GameModel model) {
        this(frame, model, null, false);
    }

    /**
     * Multiplayer constructor: provide a connected `Com` instance and initial turn flag.
     */
    public GameController(GameView frame, GameModel model, Com com, boolean iStart) {
        this.frame = frame;
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
    }

    private void handleShoot(int col, int row) {
        if (model.isGameOver() || computerTurn) {
            return;
        }
        if (networkMode) {
            if (!myTurnNetwork) {
                frame.setStatus("Nicht dein Zug (Netzwerk).");
                return;
            }
            try {
                com.fireShot(row, col);
                pendingShotCol = col;
                pendingShotRow = row;
                frame.setStatus("Schuss gesendet: " + (char) ('A' + col) + (row + 1) + " — warte auf Antwort...");
            } catch (Exception e) {
                frame.setStatus("Fehler beim Senden des Schusses: " + e.getMessage());
            }
            return;
        }

        boolean shot = model.shoot(col, row);
        if (!shot) {
            frame.setStatus("Ungültiger Schuss.");
            return;
        }

        frame.setEnemyBoard(model.getEnemyBoard());
        CellState result = model.getEnemyBoard()[col][row];

        if (result == CellState.HIT) {
            if (model.isGameOver()) {
                frame.setStatus("Du hast gewonnen!");
            } else {
                frame.setStatus("Treffer! Schieße weiter.");
            }
            return;
        }

        if (result == CellState.MISS) {
            frame.setStatus("Daneben. Computer denkt...");
            startComputerShootLoop();
        }
    }

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
            boolean hit = compShot[2] == 1;

            if (hit) {
                if (model.isGameOver()) {
                    frame.setStatus("Computer hat bei " + coord + " getroffen und gewonnen!");
                    stopComputerTurn();
                } else {
                    frame.setStatus("Computer hat bei " + coord + " getroffen. Computer denkt...");
                }
            } else {
                frame.setStatus("Computer hat bei " + coord + " daneben. Dein Zug.");
                stopComputerTurn();
            }
        });
        computerTimer.setRepeats(true);
        computerTimer.start();
    }

    private void stopComputerTurn() {
        computerTurn = false;
        if (computerTimer != null) {
            computerTimer.stop();
            computerTimer = null;
        }
    }

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
                        // Opponent missed; wait for pass before taking the turn.
                        myTurnNetwork = false;
                        frame.setStatus("Gegner hat bei " + (char) ('A' + col) + (row + 1) + " daneben. Warte auf pass.");
                    } else if (answer == 1) {
                        frame.setStatus("Gegner hat bei " + (char) ('A' + col) + (row + 1) + " getroffen.");
                    } else if (answer == 2) {
                        frame.setStatus("Gegner hat dich versenkt. Spielende.");
                    }
                } catch (Exception e) {
                    frame.setStatus("Fehler beim Senden der Antwort: " + e.getMessage());
                }
            }

            @Override
            public void onAnswer(int answer) {
                if (pendingShotCol >= 0 && pendingShotRow >= 0) {
                    if (answer == 0) {
                        model.getEnemyBoard()[pendingShotCol][pendingShotRow] = CellState.MISS;
                        frame.setEnemyBoard(model.getEnemyBoard());
                        try {
                            com.sendPass();
                        } catch (Exception e) {
                            frame.setStatus("Fehler beim Senden von pass: " + e.getMessage());
                        }
                        myTurnNetwork = false;
                        frame.setStatus("Daneben. Gegner ist dran.");
                    } else if (answer == 1) {
                        model.getEnemyBoard()[pendingShotCol][pendingShotRow] = CellState.HIT;
                        frame.setEnemyBoard(model.getEnemyBoard());
                        frame.setStatus("Treffer! Du darfst weiter schießen.");
                    } else if (answer == 2) {
                        model.getEnemyBoard()[pendingShotCol][pendingShotRow] = CellState.HIT;
                        frame.setEnemyBoard(model.getEnemyBoard());
                        frame.setStatus("Treffer. Alle gegnerischen Schiffe versenkt. Du hast gewonnen!");
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