package controller;

/*
 * Datei: controller/ShootController.java
 * Kapselt die Schuss-Logik für den Computergegner: verschiedene Schwierigkeitsstufen
 * und Auswertung eingehender Schüsse aus dem Netzwerk.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import models.BoardUtils;
import models.CellState;
import models.GameDifficulty;
import models.GameModel;

public class ShootController {

    private final GameModel gameModel;
    private final Random random;
    private List<int[]> availableCells;
    private final List<int[]> targetQueue;
    private final GameDifficulty difficulty;

    public ShootController(GameModel gameModel) {
        this.gameModel = gameModel;
        this.random = new Random();
        this.targetQueue = new ArrayList<>();
        this.difficulty = gameModel.getDifficulty();
        initAvailableCells();
    }

    private void initAvailableCells() {
        int boardSize = gameModel.getOwnBoard().length;

        for (int col = 0; col < boardSize; col++) {
            for (int row = 0; row < boardSize; row++) {
                availableCells.add(new int[]{col, row});
            }
        }
    }

    /**
     * Wertet einen Schuss auf das gegnerische Board aus und markiert Treffer
     * oder Fehlschuss.
     */
    public boolean shoot(int col, int row) {
        CellState[][] enemyBoard = gameModel.getEnemyBoard();
        if (gameModel.isGameOver() || !BoardUtils.isInsideBoard(enemyBoard, col, row)
                || (enemyBoard[col][row] == CellState.HIT || enemyBoard[col][row] == CellState.MISS)) {
            return false;
        }
        if (enemyBoard[col][row] == CellState.SHIP) {
            enemyBoard[col][row] = CellState.HIT;
            if (isBoardSunk(enemyBoard)) {
                gameModel.setGameOver(true);
                gameModel.setPlayerWon(true);
            }
        } else {
            enemyBoard[col][row] = CellState.MISS;
        }
        return true;
    }

    /**
     * Überprüft, ob auf dem angegebenen Board noch Schiffe vorhanden sind.
     */
    private boolean isBoardSunk(CellState[][] board) {
        int boardSize = board.length;

        for (int col = 0; col < boardSize; col++) {
            for (int row = 0; row < boardSize; row++) {
                if (board[col][row] == CellState.SHIP) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Bestimmt den nächsten Schuss des Computers abhängig von der
     * Schwierigkeit.
     */
    public int[] computerShoot() {
        switch (difficulty) {
            case GameDifficulty.EASY:
                return shootRandom();

            case GameDifficulty.MEDIUM:
                return shootCheckerboardAndHunt();

            case GameDifficulty.HARD:
                return shootProbabilityDensity();

            default:
                return null;
        }
    }

    /**
     * Wertet einen eingehenden Schuss vom Netzwerkgegner aus und liefert das
     * Ergebnis zurück. 0 = Wasser, 1 = Treffer, 2 = Versenkt
     */
    public int evaluateIncomingShot(int col, int row) {
        CellState[][] enemyBoard = gameModel.getEnemyBoard();
        if (gameModel.isGameOver() || !BoardUtils.isInsideBoard(enemyBoard, col, row)) {
            return 0;
        }

        int result = 0;
        CellState[][] ownBoard = gameModel.getOwnBoard();
        if (ownBoard[col][row] == CellState.SHIP) {
            ownBoard[col][row] = CellState.HIT;
            result = 1;
            if (isBoardSunk(ownBoard)) {
                gameModel.setGameOver(true);
                gameModel.setPlayerWon(false);
                result = 2;
            }
        } else {
            ownBoard[col][row] = CellState.MISS;
            result = 0;
        }
        return result;
    }

    /**
     * Platzhalter für eine schwere KI-Strategie auf Basis von
     * Wahrscheinlichkeitsdichte.
     */
    private int[] shootProbabilityDensity() {
        // TODO: implement shootProbabilityDensity()
        // 3. Schwer: Wahrscheinlichkeitsdichte-Algorithmus (Probability Density Function)
        // Dies ist die stärkste Strategie, die oft von Computer-KIs genutzt wird. 
        // - Vorgehensweise: Der Algorithmus berechnet für jedes Feld auf dem Gitter, wie viele Möglichkeiten es gibt, die noch übrigen Schiffe dort zu 
        //   platzieren.Felder in der Mitte haben anfangs eine höhere Wahrscheinlichkeit, da dort Schiffe in mehr Ausrichtungen (horizontal/vertikal) hinpassen 
        //   als in den Ecken. Nach jedem Schuss (Treffer oder Fehlschuss) wird die „Heatmap“ neu berechnet. Ein Fehlschuss senkt die Wahrscheinlichkeit der 
        //   umliegenden Felder drastisch, während ein Treffer sie massiv erhöht. 
        // - Effizienz: Extrem hoch. Erfahrene Algorithmen benötigen oft nur 30 bis 40 Schüsse, um die gesamte Flotte zu versenken.
        throw new UnsupportedOperationException("Unimplemented method 'shootProbabilityDensity'");
    }

    /**
     * Platzhalter für eine mittlere KI-Strategie mit Schachbrett- und
     * Jagd-Modus. TODO: add a sunk ship tracking to avoid shooting around
     * already sunk ships
     */
    private int[] shootCheckerboardAndHunt() {
        if (availableCells.isEmpty()) {
            return null;
        }

        int[] cell = pollNextTargetCell();
        if (cell == null) {
            cell = pickParityCell();
        }

        if (cell == null) {
            return null;
        }

        int col = cell[0];
        int row = cell[1];
        removeAvailableCell(col, row);

        CellState[][] ownBoard = gameModel.getOwnBoard();
        int result = 0;
        if (ownBoard[col][row] == CellState.SHIP) {
            ownBoard[col][row] = CellState.HIT;
            result = 1;
            enqueueTargetCells(col, row);
            if (isShipSunk(col, row)) {
                cleanupTargetQueueForSunkShip(col, row);
                result = 2;
            }
            if (isBoardSunk(ownBoard)) {
                gameModel.setGameOver(true);
                gameModel.setPlayerWon(false);
            }
        } else {
            ownBoard[col][row] = CellState.MISS;
        }

        return new int[]{col, row, result};
    }

    private int[] pollNextTargetCell() {
        List<List<int[]>> clusters = getActiveHitClusters();
        clusters.sort((a, b) -> Integer.compare(b.size(), a.size()));
        for (List<int[]> cluster : clusters) {
            Orientation ori = inferOrientation(cluster);
            List<int[]> candidates = getClusterCandidates(cluster, ori);
            for (int[] cand : candidates) {
                int c = cand[0];
                int r = cand[1];
                if (BoardUtils.isInsideBoard(gameModel.getOwnBoard(), c, r) && isCellAvailable(c, r)) {
                    removeTargetQueueCell(c, r);
                    return new int[]{c, r};
                }
            }
        }

        while (!targetQueue.isEmpty()) {
            int[] next = targetQueue.remove(0);
            if (isCellAvailable(next[0], next[1])) {
                return next;
            }
        }
        return null;
    }

    private enum Orientation {
        HORIZONTAL, VERTICAL, UNKNOWN
    }

    private List<List<int[]>> getActiveHitClusters() {
        List<List<int[]>> clusters = new ArrayList<>();
        CellState[][] ownBoard = gameModel.getOwnBoard();
        int boardSize = ownBoard.length;
        boolean[][] visited = new boolean[boardSize][boardSize];

        for (int col = 0; col < boardSize; col++) {
            for (int row = 0; row < boardSize; row++) {
                if (!visited[col][row] && ownBoard[col][row] == CellState.HIT) {
                    List<int[]> cluster = new ArrayList<>();
                    List<int[]> stack = new ArrayList<>();
                    stack.add(new int[]{col, row});
                    visited[col][row] = true;
                    while (!stack.isEmpty()) {
                        int[] cur = stack.remove(stack.size() - 1);
                        cluster.add(cur);
                        int cx = cur[0];
                        int cy = cur[1];
                        int[][] nbrs = {{cx - 1, cy}, {cx + 1, cy}, {cx, cy - 1}, {cx, cy + 1}};
                        for (int[] n : nbrs) {
                            int nx = n[0];
                            int ny = n[1];
                            if (BoardUtils.isInsideBoard(ownBoard, nx, ny) && !visited[nx][ny] && ownBoard[nx][ny] == CellState.HIT) {
                                visited[nx][ny] = true;
                                stack.add(new int[]{nx, ny});
                            }
                        }
                    }
                    clusters.add(cluster);
                }
            }
        }
        return clusters;
    }

    private Orientation inferOrientation(List<int[]> cluster) {
        if (cluster.size() < 2) {
            return Orientation.UNKNOWN;
        }
        boolean sameRow = true;
        boolean sameCol = true;
        int firstCol = cluster.get(0)[0];
        int firstRow = cluster.get(0)[1];
        for (int[] c : cluster) {
            if (c[1] != firstRow) {
                sameRow = false;
            }
            if (c[0] != firstCol) {
                sameCol = false;
            }
        }
        if (sameRow) {
            return Orientation.HORIZONTAL;
        }
        if (sameCol) {
            return Orientation.VERTICAL;
        }
        return Orientation.UNKNOWN;
    }

    private List<int[]> getClusterCandidates(List<int[]> cluster, Orientation ori) {
        List<int[]> candidates = new ArrayList<>();
        if (cluster.isEmpty()) {
            return candidates;
        }
        if (ori == Orientation.HORIZONTAL) {
            int row = cluster.get(0)[1];
            int minCol = Integer.MAX_VALUE;
            int maxCol = Integer.MIN_VALUE;
            for (int[] c : cluster) {
                minCol = Math.min(minCol, c[0]);
                maxCol = Math.max(maxCol, c[0]);
            }
            candidates.add(new int[]{minCol - 1, row});
            candidates.add(new int[]{maxCol + 1, row});
            return candidates;
        } else if (ori == Orientation.VERTICAL) {
            int col = cluster.get(0)[0];
            int minRow = Integer.MAX_VALUE;
            int maxRow = Integer.MIN_VALUE;
            for (int[] c : cluster) {
                minRow = Math.min(minRow, c[1]);
                maxRow = Math.max(maxRow, c[1]);
            }
            candidates.add(new int[]{col, minRow - 1});
            candidates.add(new int[]{col, maxRow + 1});
            return candidates;
        }
        int col = cluster.get(0)[0];
        int row = cluster.get(0)[1];
        int[][] offsets = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] off : offsets) {
            candidates.add(new int[]{col + off[0], row + off[1]});
        }
        return candidates;
    }

    private int[] pickParityCell() {
        List<int[]> parityCells = new ArrayList<>();
        for (int[] cell : availableCells) {
            if ((cell[0] + cell[1]) % 2 == 0) {
                parityCells.add(cell);
            }
        }
        if (!parityCells.isEmpty()) {
            return parityCells.get(random.nextInt(parityCells.size()));
        }
        return availableCells.get(random.nextInt(availableCells.size()));
    }

    private boolean isCellAvailable(int col, int row) {
        for (int[] cell : availableCells) {
            if (cell[0] == col && cell[1] == row) {
                return true;
            }
        }
        return false;
    }

    private void removeAvailableCell(int col, int row) {
        for (int i = 0; i < availableCells.size(); i++) {
            int[] cell = availableCells.get(i);
            if (cell[0] == col && cell[1] == row) {
                availableCells.remove(i);
                return;
            }
        }
    }

    private boolean containsTargetCell(int col, int row) {
        for (int[] cell : targetQueue) {
            if (cell[0] == col && cell[1] == row) {
                return true;
            }
        }
        return false;
    }

    private void enqueueTargetCells(int col, int row) {
        int[][] offsets = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] offset : offsets) {
            int nextCol = col + offset[0];
            int nextRow = row + offset[1];
            if (BoardUtils.isInsideBoard(nextCol, nextRow) && isCellAvailable(nextCol, nextRow)
                    && !containsTargetCell(nextCol, nextRow)) {
                targetQueue.add(new int[]{nextCol, nextRow});
            }
        }
    }

    private void cleanupTargetQueueForSunkShip(int col, int row) {
        CellState[][] ownBoard = gameModel.getOwnBoard();
        for (int[] shipCell : getShipCells(col, row)) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nextCol = shipCell[0] + dx;
                    int nextRow = shipCell[1] + dy;
                    if (!BoardUtils.isInsideBoard(ownBoard, nextCol, nextRow)) {
                        continue;
                    }
                    removeAvailableCell(nextCol, nextRow);
                    removeTargetQueueCell(nextCol, nextRow);
                }
            }
        }
    }

    private void removeTargetQueueCell(int col, int row) {
        for (int i = targetQueue.size() - 1; i >= 0; i--) {
            int[] cell = targetQueue.get(i);
            if (cell[0] == col && cell[1] == row) {
                targetQueue.remove(i);
            }
        }
    }

    private boolean isShipSunk(int col, int row) {
        List<int[]> shipCells = getShipCells(col, row);
        if (shipCells.isEmpty()) {
            return false;
        }
        CellState[][] ownBoard = gameModel.getOwnBoard();
        for (int[] cell : shipCells) {
            if (ownBoard[cell[0]][cell[1]] == CellState.SHIP) {
                return false;
            }
        }
        return true;
    }

    private List<int[]> getShipCells(int col, int row) {
        List<int[]> shipCells = new ArrayList<>();
        CellState[][] ownBoard = gameModel.getOwnBoard();
        if (!BoardUtils.isInsideBoard(ownBoard, col, row)) {
            return shipCells;
        }

        int boardSize = ownBoard.length;
        CellState startState = ownBoard[col][row];
        if (startState != CellState.HIT && startState != CellState.SHIP) {
            return shipCells;
        }

        int left = col;
        while (left - 1 >= 0 && isShipSegment(ownBoard[left - 1][row])) {
            left--;
        }
        int right = col;
        while (right + 1 < boardSize && isShipSegment(ownBoard[right + 1][row])) {
            right++;
        }
        if (right > left) {
            for (int current = left; current <= right; current++) {
                shipCells.add(new int[]{current, row});
            }
            return shipCells;
        }

        int up = row;
        while (up - 1 >= 0 && isShipSegment(ownBoard[col][up - 1])) {
            up--;
        }
        int down = row;
        while (down + 1 < boardSize && isShipSegment(ownBoard[col][down + 1])) {
            down++;
        }
        for (int current = up; current <= down; current++) {
            shipCells.add(new int[]{col, current});
        }
        return shipCells;
    }

    private boolean isShipSegment(CellState state) {
        return state == CellState.HIT || state == CellState.SHIP;
    }

    /**
     * Führt einen zufälligen Schuss des Computers aus und markiert das
     * Ergebnis.
     */
    private int[] shootRandom() {
        if (availableCells.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int[] cell = availableCells.get(random.nextInt(availableCells.size()));
        availableCells.remove(cell);
        int col = cell[0];
        int row = cell[1];
        CellState[][] ownBoard = gameModel.getOwnBoard();
        int result = 0;
        if (ownBoard[col][row] == CellState.SHIP) {
            ownBoard[col][row] = CellState.HIT;
            result = 1;
            if (isShipSunk(col, row)) {
                result = 2;
            }
            if (isBoardSunk(ownBoard)) {
                gameModel.setGameOver(true);
                gameModel.setPlayerWon(false);
            }
        } else {
            ownBoard[col][row] = CellState.MISS;
        }
        return new int[]{col, row, result};
    }
}
