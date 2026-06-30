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

/**
 * de: Kapselt die Schuss-Logik für den Computergegner.
 * en: Encapsulates the shooting logic for the computer opponent.
 */
public class ShootController {

    private final GameModel gameModel;
    private final Random random;
    private final List<int[]> availableCells;
    private final List<int[]> targetQueue;
    private final GameDifficulty difficulty;

    /**
     * de: Konstruktor für ShootController.
     * en: Constructor for ShootController.
     *
     * @param gameModel de: Parameter gameModel. en: Parameter gameModel.
     */
    public ShootController(GameModel gameModel) {
        this.gameModel = gameModel;
        this.random = new Random();
        this.availableCells = new ArrayList<>();
        this.targetQueue = new ArrayList<>();
        this.difficulty = gameModel.getDifficulty();
        initAvailableCells();
    }

    /**
     * de: Initialisiert die Liste der verfügbaren Zellen für den Computergegner.
     * en: Initializes the list of available cells for the computer opponent.
     *
     */
    private void initAvailableCells() {
        int boardSize = gameModel.getOwnBoard().length;

        for (int col = 0; col < boardSize; col++) {
            for (int row = 0; row < boardSize; row++) {
                availableCells.add(new int[] { col, row });
            }
        }
    }

    /**
     * de: Wertet einen Schuss auf das gegnerische Board aus und markiert Treffer oder Fehlschuss.
     * en: Evaluates a shot on the enemy board and marks a hit or miss.
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
     * de: Überprüft, ob auf dem angegebenen Board noch Schiffe vorhanden sind.
     * en: Checks if there are any ships remaining on the given board.
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
     * de: Bestimmt den nächsten Schuss des Computers abhängig von der Schwierigkeit.
     * en: Determines the next shot of the computer based on the difficulty.
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
     * de: Wertet einen eingehenden Schuss vom Netzwerkgegner aus und liefert das Ergebnis zurück. 0 = Wasser, 1 = Treffer, 2 = Versenkt
     * en: Evaluates an incoming shot from the network opponent and returns the result. 0 = water, 1 = hit, 2 = sunk
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
     * de: Schwere KI-Strategie mit Wahrscheinlichkeitsdichte.
     * en: Hard AI strategy with probability density.
     */
    private int[] shootProbabilityDensity() {
        int[] huntResult = pollNextTargetCell();
        if (huntResult != null) {
            int col = huntResult[0];
            int row = huntResult[1];
            removeAvailableCell(col, row);
            return shootAndEvaluate(col, row);
        }

        CellState[][] ownBoard = gameModel.getOwnBoard();
        int boardSize = ownBoard.length;
        double[][] probabilityMap = new double[boardSize][boardSize];

        for (int col = 0; col < boardSize; col++) {
            for (int row = 0; row < boardSize; row++) {
                if (isCellAvailable(col, row)) {
                    probabilityMap[col][row] = calculateCellProbability(col, row, ownBoard);
                } else {
                    probabilityMap[col][row] = -1;
                }
            }
        }

        double maxProbability = -1;
        int[] bestCell = null;
        for (int col = 0; col < boardSize; col++) {
            for (int row = 0; row < boardSize; row++) {
                if (probabilityMap[col][row] > maxProbability) {
                    maxProbability = probabilityMap[col][row];
                    bestCell = new int[] { col, row };
                }
            }
        }

        if (bestCell == null) {
            return null;
        }

        removeAvailableCell(bestCell[0], bestCell[1]);
        return shootAndEvaluate(bestCell[0], bestCell[1]);
    }

    /**
     * de: Berechnet die Wahrscheinlichkeit, dass ein Schiff auf einem Feld platziert ist.
     * en: Calculates the probability that a ship is placed on a cell.
     */
    private double calculateCellProbability(int col, int row, CellState[][] board) {
        double probability = 0.0;
        int boardSize = board.length;

        double positionWeight = 1.0
                - (Math.abs(col - boardSize / 2.0) + Math.abs(row - boardSize / 2.0)) / (boardSize * 1.5);

        // Horizontal
        for (int shipLen = 2; shipLen <= 4; shipLen++) {
            for (int startCol = Math.max(0, col - shipLen + 1); startCol <= Math.min(boardSize - shipLen,
                    col); startCol++) {
                if (canPlaceShip(startCol, row, startCol + shipLen - 1, row, board)) {
                    probability += 1.0 / shipLen;
                }
            }
        }

        // Vertikal
        for (int shipLen = 2; shipLen <= 4; shipLen++) {
            for (int startRow = Math.max(0, row - shipLen + 1); startRow <= Math.min(boardSize - shipLen,
                    row); startRow++) {
                if (canPlaceShip(col, startRow, col, startRow + shipLen - 1, board)) {
                    probability += 1.0 / shipLen;
                }
            }
        }

        probability *= Math.max(0.5, positionWeight);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nearCol = col + dx;
                int nearRow = row + dy;
                if (BoardUtils.isInsideBoard(board, nearCol, nearRow) && board[nearCol][nearRow] == CellState.HIT) {
                    probability *= 3.0;
                }
            }
        }

        return probability;
    }

    /**
     * de: Prüft, ob ein Schiff auf dem Board platziert werden kann.
     * en: Checks if a ship can be placed on the board.
     */
    private boolean canPlaceShip(int col1, int row1, int col2, int row2, CellState[][] board) {
        if (col1 == col2) {
            // Vertikal
            for (int r = Math.min(row1, row2); r <= Math.max(row1, row2); r++) {
                if (board[col1][r] == CellState.MISS) {
                    return false;
                }
            }
        } else if (row1 == row2) {
            // Horizontal
            for (int c = Math.min(col1, col2); c <= Math.max(col1, col2); c++) {
                if (board[c][row1] == CellState.MISS) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * de: Führt einen Schuss aus und evaluiert das Ergebnis.
     * en: Executes a shot and evaluates the result.
     */
    private int[] shootAndEvaluate(int col, int row) {
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
        return new int[] { col, row, result };
    }

    /**
     * de: Mittlere KI-Strategie mit Schachbrett- und Jagd-Modus.
     * en: Medium AI strategy with checkerboard and hunt mode.
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

        return new int[] { col, row, result };
    }

    /**
     * de: Gibt die nächste Zielzelle aus der Zielwarteschlange zurück.
     * en: Returns the next target cell from the target queue.
     *
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
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
                    return new int[] { c, r };
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

    /**
     * de: Enum für die Orientierung.
     * en: Enum for the orientation.
     */
    private enum Orientation {
        HORIZONTAL, VERTICAL, UNKNOWN
    }

    /**
     * de: Gibt die aktiven Treffercluster zurück.
     * en: Returns the active hit clusters.
     *
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
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
                    stack.add(new int[] { col, row });
                    visited[col][row] = true;
                    while (!stack.isEmpty()) {
                        int[] cur = stack.remove(stack.size() - 1);
                        cluster.add(cur);
                        int cx = cur[0];
                        int cy = cur[1];
                        int[][] nbrs = { { cx - 1, cy }, { cx + 1, cy }, { cx, cy - 1 }, { cx, cy + 1 } };
                        for (int[] n : nbrs) {
                            int nx = n[0];
                            int ny = n[1];
                            if (BoardUtils.isInsideBoard(ownBoard, nx, ny) && !visited[nx][ny]
                                    && ownBoard[nx][ny] == CellState.HIT) {
                                visited[nx][ny] = true;
                                stack.add(new int[] { nx, ny });
                            }
                        }
                    }
                    clusters.add(cluster);
                }
            }
        }
        return clusters;
    }

    /**
     * de: Schätzt die Orientierung eines Trefferclusters.
     * en: Infers the orientation of a hit cluster.
     *
     * @param cluster de: Parameter cluster. en: Parameter cluster.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
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

    /**
     * de: Gibt die möglichen Zielzellen für ein Treffercluster zurück.
     * en: Returns the possible target cells for a hit cluster.
     *
     * @param cluster de: Parameter cluster. en: Parameter cluster.
     * @param ori de: Parameter ori. en: Parameter ori.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
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
            candidates.add(new int[] { minCol - 1, row });
            candidates.add(new int[] { maxCol + 1, row });
            return candidates;
        } else if (ori == Orientation.VERTICAL) {
            int col = cluster.get(0)[0];
            int minRow = Integer.MAX_VALUE;
            int maxRow = Integer.MIN_VALUE;
            for (int[] c : cluster) {
                minRow = Math.min(minRow, c[1]);
                maxRow = Math.max(maxRow, c[1]);
            }
            candidates.add(new int[] { col, minRow - 1 });
            candidates.add(new int[] { col, maxRow + 1 });
            return candidates;
        }
        int col = cluster.get(0)[0];
        int row = cluster.get(0)[1];
        int[][] offsets = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };
        for (int[] off : offsets) {
            candidates.add(new int[] { col + off[0], row + off[1] });
        }
        return candidates;
    }

    /**
     * de: Wählt eine Zelle basierend auf dem Paritätsmuster aus.
     * en: Picks a cell based on the parity pattern.
     *
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
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

    /**
     * de: Überprüft, ob eine Zelle verfügbar ist.
     * en: Checks if a cell is available.
     *
     * @param col de: Parameter col. en: Parameter col.
     * @param row de: Parameter row. en: Parameter row.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
    private boolean isCellAvailable(int col, int row) {
        for (int[] cell : availableCells) {
            if (cell[0] == col && cell[1] == row) {
                return true;
            }
        }
        return false;
    }

    /**
     * de: Entfernt eine Zelle aus der Liste der verfügbaren Zellen.
     * en: Removes a cell from the list of available cells.
     *
     * @param col de: Parameter col. en: Parameter col.
     * @param row de: Parameter row. en: Parameter row.
     */
    private void removeAvailableCell(int col, int row) {
        for (int i = 0; i < availableCells.size(); i++) {
            int[] cell = availableCells.get(i);
            if (cell[0] == col && cell[1] == row) {
                availableCells.remove(i);
                return;
            }
        }
    }

    /**
     * de: Überprüft, ob eine Zelle in der Zielwarteschlange enthalten ist.
     * en: Checks if a cell is in the target queue.
     *
     * @param col de: Parameter col. en: Parameter col.
     * @param row de: Parameter row. en: Parameter row.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
    private boolean containsTargetCell(int col, int row) {
        for (int[] cell : targetQueue) {
            if (cell[0] == col && cell[1] == row) {
                return true;
            }
        }
        return false;
    }

    /**
     * de: Fügt die benachbarten Zellen eines getroffenen Schiffs zur Zielwarteschlange hinzu.
     * en: Adds the neighboring cells of a hit ship to the target queue.
     *
     * @param col de: Parameter col. en: Parameter col.
     * @param row de: Parameter row. en: Parameter row.
     */
    private void enqueueTargetCells(int col, int row) {
        CellState[][] ownBoard = gameModel.getOwnBoard();
        int[][] offsets = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };

        for (int[] offset : offsets) {
            int nextCol = col + offset[0];
            int nextRow = row + offset[1];

            if (BoardUtils.isInsideBoard(ownBoard, nextCol, nextRow) && isCellAvailable(nextCol, nextRow)
                    && !containsTargetCell(nextCol, nextRow)) {
                targetQueue.add(new int[] { nextCol, nextRow });
            }
        }
    }

    /**
     * de: Entfernt die Zellen eines versenkten Schiffs aus der Zielwarteschlange.
     * en: Removes the cells of a sunk ship from the target queue.
     *
     * @param col de: Parameter col. en: Parameter col.
     * @param row de: Parameter row. en: Parameter row.
     */
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

    /**
     * de: Entfernt eine Zelle aus der Zielwarteschlange.
     * en: Removes a cell from the target queue.
     *
     * @param col de: Parameter col. en: Parameter col.
     * @param row de: Parameter row. en: Parameter row.
     */
    private void removeTargetQueueCell(int col, int row) {
        for (int i = targetQueue.size() - 1; i >= 0; i--) {
            int[] cell = targetQueue.get(i);
            if (cell[0] == col && cell[1] == row) {
                targetQueue.remove(i);
            }
        }
    }

    /**
     * de: Überprüft, ob ein Schiff versenkt ist.
     * en: Checks if a ship is sunk.
     *
     * @param col de: Parameter col. en: Parameter col.
     * @param row de: Parameter row. en: Parameter row.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
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

    /**
     * de: Liefert die Zellen eines Schiffs basierend auf einer gegebenen Zelle zurück.
     * en: Returns the cells of a ship based on a given cell.
     *
     * @param col de: Parameter col. en: Parameter col.
     * @param row de: Parameter row. en: Parameter row.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
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
                shipCells.add(new int[] { current, row });
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
            shipCells.add(new int[] { col, current });
        }
        return shipCells;
    }

    /**
     * de: Überprüft, ob eine Zelle ein Schiffsegment ist.
     * en: Checks if a cell is a ship segment.
     *
     * @param state de: Parameter state. en: Parameter state.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
    private boolean isShipSegment(CellState state) {
        return state == CellState.HIT || state == CellState.SHIP;
    }

    /**
     * de: Einfache KI-Strategie, die einen zufälligen Schuss ausführt und das Ergebnis markiert.
     * en: Simple AI strategy that executes a random shot and marks the result.
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
        return new int[] { col, row, result };
    }
}
