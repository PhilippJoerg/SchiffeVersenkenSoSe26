package controller;

import javax.swing.Timer;

import models.GameModel;
import view.CellState;
import view.MainFrame;

public class GameController {
    private final MainFrame frame;
    private final GameModel model;
    private boolean computerTurn = false;
    private Timer computerTimer;

    public GameController(MainFrame frame, GameModel model) {
        this.frame = frame;
        this.model = model;

        frame.setOwnBoard(model.getOwnBoard());
        frame.setEnemyBoard(model.getEnemyBoard());

        frame.setEnemyBoardClickListener(this::handleShoot);
        frame.setShootButtonEnabled(false); // not needed, since clicking on board
        frame.setRotateButtonEnabled(false); // disable rotate during game
        frame.setStatus("Spiel gestartet. Klicke auf das gegnerische Feld, um zu schießen.");
    }

    private void handleShoot(int col, int row) {
        if (model.isGameOver() || computerTurn) {
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
}