package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import models.CellState;

public class BoardPanel extends JPanel {

    // Größe des Spielfelds: 10x10
    public static final int GRID_SIZE = 10;

    // Beibehalten als bevorzugte Zellgröße, damit bestehender Code nicht bricht
    public static final int CELL_SIZE = 32;

    // Platz für Beschriftung links und oben
    public static final int LABEL_SPACE = 28;

    private static final int MIN_CELL_SIZE = 26;
    private static final int MAX_CELL_SIZE = 56;

    private static final Color BACKGROUND_COLOR = new Color(238, 238, 238);
    private static final Color WATER_COLOR = new Color(185, 220, 240);
    private static final Color GRID_COLOR = new Color(90, 135, 165);
    private static final Color SHIP_COLOR = new Color(145, 150, 160);
    private static final Color SHIP_BORDER_COLOR = new Color(90, 95, 105);
    private static final Color MISS_COLOR = new Color(55, 70, 85);
    private static final Color HIT_COLOR = new Color(205, 45, 45);

    // Merkt, ob dieses Panel das gegnerische Feld ist
    private final boolean enemyBoard;

    // Enthält den aktuellen Zustand aller Felder
    private final CellState[][] cells;

    // Listener für Klicks auf das Spielfeld
    private BoardClickListener clickListener;

    public BoardPanel(boolean enemyBoard) {
        this.enemyBoard = enemyBoard;
        this.cells = createEmptyBoard();

        setOpaque(true);
        setBackground(BACKGROUND_COLOR);

        setMinimumSize(new Dimension(
                LABEL_SPACE + GRID_SIZE * MIN_CELL_SIZE + 1,
                LABEL_SPACE + GRID_SIZE * MIN_CELL_SIZE + 1
        ));

        setPreferredSize(new Dimension(
                LABEL_SPACE + GRID_SIZE * CELL_SIZE + 1,
                LABEL_SPACE + GRID_SIZE * CELL_SIZE + 1
        ));

        // Reagiert auf Mausklicks und wandelt Pixel in Feldkoordinaten um
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point cell = cellAt(e.getPoint());
                if (cell != null && clickListener != null) {
                    clickListener.onCellClicked(cell.x, cell.y);
                }
            }
        });
    }

    // Setzt die Aktion, die bei einem Klick aufgerufen wird
    public void setBoardClickListener(BoardClickListener clickListener) {
        this.clickListener = clickListener;
    }

    // Übernimmt einen neuen Board-Zustand und zeichnet das Panel neu
    public void setCells(CellState[][] newCells) {
        validateBoard(newCells);

        for (int row = 0; row < GRID_SIZE; row++) {
            System.arraycopy(newCells[row], 0, cells[row], 0, GRID_SIZE);
        }

        repaint();
    }

    public boolean isEnemyBoard() {
        return enemyBoard;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        // Sorgt für glattere Darstellung
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        BoardGeometry geometry = calculateGeometry();

        drawCoordinates(g2, geometry);
        drawCells(g2, geometry);
        drawGrid(g2, geometry);

        g2.dispose();
    }

    // Zeichnet A-J oben und 1-10 links
    private void drawCoordinates(Graphics2D g2, BoardGeometry geometry) {
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.BLACK);

        for (int col = 0; col < GRID_SIZE; col++) {
            String label = String.valueOf((char) ('A' + col));
            int x = geometry.startX + col * geometry.cellSize
                    + (geometry.cellSize - fm.stringWidth(label)) / 2;
            int y = geometry.startY - 8;

            g2.drawString(label, x, y);
        }

        for (int row = 0; row < GRID_SIZE; row++) {
            String label = String.valueOf(row + 1);
            int x = geometry.startX - 8 - fm.stringWidth(label);
            int y = geometry.startY + row * geometry.cellSize
                    + (geometry.cellSize + fm.getAscent()) / 2 - 3;

            g2.drawString(label, x, y);
        }
    }

    // Geht alle Felder durch und zeichnet ihren Inhalt
    private void drawCells(Graphics2D g2, BoardGeometry geometry) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                CellState state = cells[col][row];

                int x = geometry.startX + col * geometry.cellSize;
                int y = geometry.startY + row * geometry.cellSize;

                g2.setColor(WATER_COLOR);
                g2.fillRect(x, y, geometry.cellSize, geometry.cellSize);

                drawCellContent(g2, x, y, geometry.cellSize, state);
            }
        }
    }

    // Zeichnet je nach Zustand Schiff, Treffer oder Fehlschuss
    private void drawCellContent(Graphics2D g2, int x, int y, int cellSize, CellState state) {
        switch (state) {
            case SHIP:
                // Schiffe nur auf dem eigenen Feld sichtbar machen
                if (!enemyBoard) {
                    drawShip(g2, x, y, cellSize);
                }
                break;

            case MISS:
                drawCenteredCircle(g2, x, y, cellSize, MISS_COLOR, Math.max(6, cellSize / 5));
                break;

            case HIT:
                if (!enemyBoard) {
                    drawShip(g2, x, y, cellSize);
                }
                drawCenteredCircle(g2, x, y, cellSize, HIT_COLOR, Math.max(9, cellSize / 4));
                break;

            case EMPTY:
            default:
                // Leere Felder werden nur als Wasserfläche gezeichnet
                break;
        }
    }

    private void drawShip(Graphics2D g2, int x, int y, int cellSize) {
        int padding = Math.max(2, cellSize / 10);
        int arc = Math.max(8, cellSize / 3);

        int shipX = x + padding;
        int shipY = y + padding;
        int shipSize = cellSize - padding * 2;

        g2.setColor(SHIP_COLOR);
        g2.fillRoundRect(shipX, shipY, shipSize, shipSize, arc, arc);

        g2.setColor(SHIP_BORDER_COLOR);
        g2.drawRoundRect(shipX, shipY, shipSize, shipSize, arc, arc);
    }

    private void drawCenteredCircle(Graphics2D g2, int x, int y, int cellSize, Color color, int size) {
        int circleX = x + (cellSize - size) / 2;
        int circleY = y + (cellSize - size) / 2;

        g2.setColor(color);
        g2.fillOval(circleX, circleY, size, size);
    }

    // Zeichnet das Raster des Spielfelds
    private void drawGrid(Graphics2D g2, BoardGeometry geometry) {
        g2.setColor(GRID_COLOR);

        for (int i = 0; i <= GRID_SIZE; i++) {
            int x = geometry.startX + i * geometry.cellSize;
            int y = geometry.startY + i * geometry.cellSize;

            g2.drawLine(
                    geometry.startX,
                    y,
                    geometry.startX + geometry.boardSize,
                    y
            );

            g2.drawLine(
                    x,
                    geometry.startY,
                    x,
                    geometry.startY + geometry.boardSize
            );
        }
    }

    // Wandelt eine Pixelposition der Maus in Spielfeldkoordinaten um
    public Point cellAt(Point point) {
        BoardGeometry geometry = calculateGeometry();

        int x = point.x - geometry.startX;
        int y = point.y - geometry.startY;

        if (x < 0 || y < 0) {
            return null;
        }

        int col = x / geometry.cellSize;
        int row = y / geometry.cellSize;

        if (col >= GRID_SIZE || row >= GRID_SIZE) {
            return null;
        }

        return new Point(col, row);
    }

    private BoardGeometry calculateGeometry() {
        int availableWidth = Math.max(1, getWidth() - LABEL_SPACE - 1);
        int availableHeight = Math.max(1, getHeight() - LABEL_SPACE - 1);

        int dynamicCellSize = Math.min(availableWidth, availableHeight) / GRID_SIZE;
        int cellSize = Math.max(MIN_CELL_SIZE, Math.min(MAX_CELL_SIZE, dynamicCellSize));

        int boardSize = GRID_SIZE * cellSize;

        int startX = LABEL_SPACE + Math.max(0, (getWidth() - LABEL_SPACE - boardSize) / 2);
        int startY = LABEL_SPACE + Math.max(0, (getHeight() - LABEL_SPACE - boardSize) / 2);

        return new BoardGeometry(startX, startY, cellSize, boardSize);
    }

    // Erzeugt ein leeres 10x10-Board
    private CellState[][] createEmptyBoard() {
        CellState[][] board = new CellState[GRID_SIZE][GRID_SIZE];

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                board[col][row] = CellState.EMPTY;
            }
        }

        return board;
    }

    // Prüft, ob das übergebene Board die richtige Größe hat
    private void validateBoard(CellState[][] board) {
        if (board == null || board.length != GRID_SIZE) {
            throw new IllegalArgumentException("Board must have 10 rows.");
        }

        for (CellState[] row : board) {
            if (row == null || row.length != GRID_SIZE) {
                throw new IllegalArgumentException("Each board row must have 10 columns.");
            }
        }
    }

    private static final class BoardGeometry {
        private final int startX;
        private final int startY;
        private final int cellSize;
        private final int boardSize;

        private BoardGeometry(int startX, int startY, int cellSize, int boardSize) {
            this.startX = startX;
            this.startY = startY;
            this.cellSize = cellSize;
            this.boardSize = boardSize;
        }
    }
}