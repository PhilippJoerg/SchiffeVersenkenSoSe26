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

/**
 * de: Die Klasse BoardPanel stellt ein 10x10-Spielfeld dar, inklusive Beschriftung, Zelleninhalt
 * und Klick-/Zeichenlogik.
 * en: The BoardPanel class represents a 10x10 game board, including labels, cell content,
 * and click/draw logic.
 */
public class BoardPanel extends JPanel {

    // Spielfeldgröße: 10x10
    /**
     * de: Das Feld GRID_SIZE gibt die Größe des Spielfelds an.
     * en: The field GRID_SIZE specifies the size of the game board.
     */
    public static final int GRID_SIZE = 10;

    // Standardgröße einer Zelle
    /**
     * de: Das Feld CELL_SIZE gibt die Standardgröße einer Zelle an.
     * en: The field CELL_SIZE specifies the default size of a cell.
     */
    public static final int CELL_SIZE = 32;

    // Platz für Beschriftung oben und links
    /**
     * de: Das Feld LABEL_SPACE gibt den Platz für die Beschriftung oben und links an.
     * en: The field LABEL_SPACE specifies the space for labels at the top and left.
     */
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
    private CellState[][] cells;
    private int gridSize;

    // Reaktion auf Klicks ins Spielfeld
    private BoardClickListener clickListener;

    /**
     * de: Konstruktor für BoardPanel.
     * en: Constructor for BoardPanel.
     *
     * @param enemyBoard de: Gibt an, ob dieses Board das Gegnerfeld ist. en: Indicates whether this board is the enemy board.
     */
    public BoardPanel(boolean enemyBoard) {
        this.enemyBoard = enemyBoard;
        this.gridSize = GRID_SIZE;
        this.cells = createEmptyBoard(gridSize);

        setOpaque(false);

        // Kleinste erlaubte Panelgröße
        setMinimumSize(new Dimension(
                LABEL_SPACE + gridSize * MIN_CELL_SIZE + 1,
                LABEL_SPACE + gridSize * MIN_CELL_SIZE + 1
        ));

        // Startgröße für das Layout
        setPreferredSize(new Dimension(
                LABEL_SPACE + GRID_SIZE * CELL_SIZE + 1,
                LABEL_SPACE + GRID_SIZE * CELL_SIZE + 1
        ));

        // Wandelt Mausklicks in Spielfeldkoordinaten um
        addMouseListener(new MouseAdapter() {
            /**
             * de: Überschreibt die Methode mouseClicked.
             * en: Overrides the method mouseClicked.
             *
             * @param e de: Parameter e. en: Parameter e.
             */
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
    /**
     * de: Setzt den Listener für Klicks auf das Board.
     * en: Sets the listener for clicks on the board.
     *
     * @param clickListener de: Parameter clickListener. en: Parameter clickListener.
     */
    public void setBoardClickListener(BoardClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * de: Aktualisiert das Board. Das Array wird kopiert, damit spätere Änderungen am übergebenen Array das Panel nicht unkontrolliert verändern.
     * en: Updates the board. The array is copied to prevent uncontrolled changes to the panel from later modifications to the passed array.
     *
     * @param newCells de: Neues Spielfeld mit Zellzustaenden. en: New board with cell states.
     */
    public void setCells(CellState[][] newCells) {
        validateBoard(newCells);

        this.gridSize = newCells.length;
        this.cells = createEmptyBoard(gridSize);

        for (int col = 0; col < gridSize; col++) {
            System.arraycopy(newCells[col], 0, cells[col], 0, gridSize);
        }

        setMinimumSize(new Dimension(
                LABEL_SPACE + gridSize * MIN_CELL_SIZE + 1,
                LABEL_SPACE + gridSize * MIN_CELL_SIZE + 1
        ));

        setPreferredSize(new Dimension(
                LABEL_SPACE + gridSize * CELL_SIZE + 1,
                LABEL_SPACE + gridSize * CELL_SIZE + 1
        ));

        revalidate();
        repaint();
    }

    // Gibt zurück, ob es das Gegnerfeld ist
    /**
     * de: Gibt zurück, ob dieses Board das Gegnerfeld ist.
     * en: Returns whether this board is the enemy board.
     *
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
    public boolean isEnemyBoard() {
        return enemyBoard;
    }

    /**
     * de: Zeichnet das komplette Panel inklusive Koordinaten, Zellen und Raster.
     * en: Paints the complete panel including coordinates, cells, and grid.
     *
     * @param g de: Zeichenkontext von Swing. en: Swing graphics context.
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
    /**
     * de: Zeichnet die Koordinatenbeschriftungen für das Board.
     * en: Draws the coordinate labels for the board.
     *
     * @param g2 de: Zeichenkontext von Swing. en: Swing graphics context.
     * @param geometry de: Geometrieinformationen des Boards. en: Geometry information of the board.
     */
    private void drawCoordinates(Graphics2D g2, BoardGeometry geometry) {
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);

        for (int col = 0; col < gridSize; col++) {
            String label = String.valueOf((char) ('A' + col));

            int x = geometry.startX + col * geometry.cellSize
                    + (geometry.cellSize - fm.stringWidth(label)) / 2;
            int y = geometry.startY - 8;

            g2.drawString(label, x, y);
        }

        for (int row = 0; row < gridSize; row++) {
            String label = String.valueOf(row + 1);

            int x = geometry.startX - 8 - fm.stringWidth(label);
            int y = geometry.startY + row * geometry.cellSize
                    + (geometry.cellSize + fm.getAscent()) / 2 - 3;

            g2.drawString(label, x, y);
        }
    }

    /**
     * de: Zeichnet alle Zellen des Boards einschließlich ihres Inhalts.
     * en: Draws all cells of the board, including their content.
     *
     * @param g2 de: Zeichenkontext von Swing. en: Swing graphics context.
     * @param geometry de: Geometrieinformationen des Boards. en: Geometry information of the board.
     */
    private void drawCells(Graphics2D g2, BoardGeometry geometry) {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
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
     * de: Zeichnet den Inhalt einer Zelle.
     * en: Draws the content of a cell.
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

    /**
     * de: Zeichnet ein Schiff als abgerundetes Rechteck.
     * en: Draws a ship as a rounded rectangle.
     *
     * @param g2 de: Zeichenkontext von Swing. en: Swing graphics context.
     * @param x de: X-Koordinate der Zelle. en: X-coordinate of the cell.
     * @param y de: Y-Koordinate der Zelle. en: Y-coordinate of the cell.
     * @param cellSize de: Größe der Zelle. en: Size of the cell.
     */
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


    /**
     * de: Zeichnet einen mittigen Kreis für Treffer oder Fehlschuss.
     * en: Draws a centered circle for hit or miss.
     *
     * @param g2 de: Zeichenkontext von Swing. en: Swing graphics context.
     * @param x de: X-Koordinate der Zelle. en: X-coordinate of the cell.
     * @param y de: Y-Koordinate der Zelle. en: Y-coordinate of the cell.
     * @param cellSize de: Größe der Zelle. en: Size of the cell.
     * @param color de: Farbe des Kreises. en: Color of the circle.
     * @param size de: Größe des Kreises. en: Size of the circle.
     */
    private void drawCenteredCircle(Graphics2D g2, int x, int y, int cellSize, Color color, int size) {
        int circleX = x + (cellSize - size) / 2;
        int circleY = y + (cellSize - size) / 2;

        g2.setColor(color);
        g2.fillOval(circleX, circleY, size, size);
    }


    /**
     * de: Zeichnet das Raster über die Zellen.
     * en: Draws the grid over the cells.
     *
     * @param g2 de: Zeichenkontext von Swing. en: Swing graphics context.
     * @param geometry de: Geometrie des Boards. en: Geometry of the board.
     */
    private void drawGrid(Graphics2D g2, BoardGeometry geometry) {
        g2.setColor(GRID_COLOR);

        for (int i = 0; i <= gridSize; i++) {
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

        if (col >= gridSize || row >= gridSize) {
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

        int dynamicCellSize = Math.min(availableWidth, availableHeight) / gridSize;
        int cellSize = Math.max(MIN_CELL_SIZE, Math.min(MAX_CELL_SIZE, dynamicCellSize));

        int boardSize = gridSize * cellSize;

        int startX = LABEL_SPACE + Math.max(0, (getWidth() - LABEL_SPACE - boardSize) / 2);
        int startY = LABEL_SPACE + Math.max(0, (getHeight() - LABEL_SPACE - boardSize) / 2);

        return new BoardGeometry(startX, startY, cellSize, boardSize);
    }

    /**
     * de: Erstellt ein leeres Board mit der angegebenen Größe.
     * en: Creates an empty board with the specified size.
     *
     * @param size de: Größe des Boards. en: Size of the board.
     * @return de: Ein leeres Board. en: An empty board.
     */
    private CellState[][] createEmptyBoard(int size) {
        CellState[][] board = new CellState[size][size];

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                board[col][row] = CellState.EMPTY;
            }
        }

        return board;
    }

    /**
     * de: Validiert das Board.
     * en: Validates the board.
     *
     * @param board de: Das zu validierende Board. en: The board to validate.
     */
    private void validateBoard(CellState[][] board) {
        if (board == null || board.length == 0) {
            throw new IllegalArgumentException("Board must not be null or empty.");
        }

        int size = board.length;

        for (CellState[] column : board) {
            if (column == null || column.length != size) {
                throw new IllegalArgumentException("Board must be square.");
            }
        }
    }

    // Bündelt die berechneten Zeichenwerte des Boards
    private static final class BoardGeometry {

        private final int startX;
        private final int startY;
        private final int cellSize;
        private final int boardSize;

        /**
         * de: Bündelt die berechneten Zeichenwerte des Boards.
         * en: Encapsulates the calculated drawing values of the board.
         *
         * @param startX de: Startposition X des Boards. en: Start position X of the board.
         * @param startY de: Startposition Y des Boards. en: Start position Y of the board.
         * @param cellSize de: Größe einer Zelle. en: Size of a cell.
         * @param boardSize de: Größe des Boards. en: Size of the board.
         * @return de: Rueckgabewert der Methode. en: Method return value.
         */
        private BoardGeometry(int startX, int startY, int cellSize, int boardSize) {
            this.startX = startX;
            this.startY = startY;
            this.cellSize = cellSize;
            this.boardSize = boardSize;
        }
    }
}
