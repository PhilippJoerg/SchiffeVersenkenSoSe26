/*
 * Datei: view/BoardPanel.java
 * Swing-Panel zur Darstellung eines 10x10-Spielfelds inklusive Beschriftung, Zelleninhalt
 * und Klick-/Zeichenlogik.
 */
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

    // Spielfeldgröße: 10x10
    public static final int GRID_SIZE = 10;

    // Standardgröße einer Zelle
    public static final int CELL_SIZE = 32;

    // Platz für Beschriftung oben und links
    public static final int LABEL_SPACE = 28;

    // Grenzen für skalierende Zellgröße
    private static final int MIN_CELL_SIZE = 26;
    private static final int MAX_CELL_SIZE = 56;

    // Farben der Spielfeld-Elemente
    private static final Color WATER_COLOR = new Color(185, 220, 240);
    private static final Color GRID_COLOR = new Color(90, 135, 165);
    private static final Color SHIP_COLOR = new Color(145, 150, 160);
    private static final Color SHIP_BORDER_COLOR = new Color(90, 95, 105);
    private static final Color MISS_COLOR = new Color(55, 70, 85);
    private static final Color HIT_COLOR = new Color(205, 45, 45);

    // true, wenn dieses Board das Gegnerfeld ist
    private final boolean enemyBoard;

    // Zustand aller Felder: cells[col][row]
    private final CellState[][] cells;

    // Reaktion auf Klicks ins Spielfeld
    private BoardClickListener clickListener;

    public BoardPanel(boolean enemyBoard) {
        this.enemyBoard = enemyBoard;
        this.cells = createEmptyBoard();

        setOpaque(false);

        // Kleinste erlaubte Panelgröße
        setMinimumSize(new Dimension(
                LABEL_SPACE + GRID_SIZE * MIN_CELL_SIZE + 1,
                LABEL_SPACE + GRID_SIZE * MIN_CELL_SIZE + 1
        ));

        // Startgröße für das Layout
        setPreferredSize(new Dimension(
                LABEL_SPACE + GRID_SIZE * CELL_SIZE + 1,
                LABEL_SPACE + GRID_SIZE * CELL_SIZE + 1
        ));

        // Wandelt Mausklicks in Spielfeldkoordinaten um
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

    // Setzt die Klick-Aktion für gültige Felder
    public void setBoardClickListener(BoardClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /*
     * Aktualisiert das Board.
     * Das Array wird kopiert, damit spätere Änderungen am übergebenen Array
     * das Panel nicht unkontrolliert verändern.
     */
    public void setCells(CellState[][] newCells) {
        validateBoard(newCells);

        for (int row = 0; row < GRID_SIZE; row++) {
            System.arraycopy(newCells[row], 0, cells[row], 0, GRID_SIZE);
        }

        repaint();
    }

    // Gibt zurück, ob es das Gegnerfeld ist
    public boolean isEnemyBoard() {
        return enemyBoard;
    }

    /*
     * Zeichnet das komplette Panel neu.
     * Wird automatisch von Swing aufgerufen, z. B. nach repaint()
     * oder beim Ändern der Fenstergröße.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        // Glättet Rundungen und Kreise
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
        g2.setColor(Color.WHITE);

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

    // Zeichnet alle Zellen inklusive Inhalt
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

    /*
     * Zeichnet den Inhalt einer Zelle.
     * Gegnerische Schiffe werden nicht angezeigt.
     */
    private void drawCellContent(Graphics2D g2, int x, int y, int cellSize, CellState state) {
        switch (state) {
            case SHIP:
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
                
                break;
        }
    }

    // Zeichnet ein Schiff als abgerundetes Rechteck
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

    // Zeichnet einen mittigen Kreis für Treffer oder Fehlschuss
    private void drawCenteredCircle(Graphics2D g2, int x, int y, int cellSize, Color color, int size) {
        int circleX = x + (cellSize - size) / 2;
        int circleY = y + (cellSize - size) / 2;

        g2.setColor(color);
        g2.fillOval(circleX, circleY, size, size);
    }

    // Zeichnet das Raster über die Zellen
    private void drawGrid(Graphics2D g2, BoardGeometry geometry) {
        g2.setColor(GRID_COLOR);

        for (int i = 0; i <= GRID_SIZE; i++) {
            int x = geometry.startX + i * geometry.cellSize;
            int y = geometry.startY + i * geometry.cellSize;

            // Horizontale Linie
            g2.drawLine(
                    geometry.startX,
                    y,
                    geometry.startX + geometry.boardSize,
                    y
            );

            // Vertikale Linie
            g2.drawLine(
                    x,
                    geometry.startY,
                    x,
                    geometry.startY + geometry.boardSize
            );
        }
    }

    /*
     * Wandelt eine Pixelposition in Board-Koordinaten um.
     * Gibt null zurück, wenn der Punkt außerhalb des Spielfelds liegt.
     */
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

    /*
     * Berechnet Position und Größe des Boards.
     * Die Zellgröße passt sich an das Panel an, bleibt aber begrenzt.
     */
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

    // Erstellt ein leeres Board
    private CellState[][] createEmptyBoard() {
        CellState[][] board = new CellState[GRID_SIZE][GRID_SIZE];

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                board[col][row] = CellState.EMPTY;
            }
        }

        return board;
    }

    // Prüft, ob das Board 10x10 groß ist
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

    // Bündelt die berechneten Zeichenwerte des Boards
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