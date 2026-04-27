package ui;

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

public class BoardPanel extends JPanel {
    // Größe des Spielfelds: 10x10
    public static final int GRID_SIZE = 10;

    // Größe einer einzelnen Zelle in Pixeln
    public static final int CELL_SIZE = 32;

    // Platz für Beschriftung links und oben
    public static final int LABEL_SPACE = 28;

    // Merkt, ob dieses Panel das gegnerische Feld ist
    private final boolean enemyBoard;

    // Enthält den aktuellen Zustand aller Felder
    private final CellState[][] cells;

    // Listener für Klicks auf das Spielfeld
    private BoardClickListener clickListener;

    public BoardPanel(boolean enemyBoard) {
        this.enemyBoard = enemyBoard;
        this.cells = createEmptyBoard();

        setBackground(Color.WHITE);

        // Gesamtgröße des Panels inklusive Beschriftung
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

        // Zeichenreihenfolge: Beschriftung, Inhalte, Gitter
        drawCoordinates(g2);
        drawCells(g2);
        drawGrid(g2);

        g2.dispose();
    }

    // Zeichnet A-J oben und 1-10 links
    private void drawCoordinates(Graphics2D g2) {
        FontMetrics fm = g2.getFontMetrics();

        g2.setColor(Color.BLACK);

        for (int col = 0; col < GRID_SIZE; col++) {
            String label = String.valueOf((char) ('A' + col));
            int x = LABEL_SPACE + col * CELL_SIZE + (CELL_SIZE - fm.stringWidth(label)) / 2;
            int y = LABEL_SPACE - 8;

            g2.drawString(label, x, y);
        }

        for (int row = 0; row < GRID_SIZE; row++) {
            String label = String.valueOf(row + 1);
            int x = LABEL_SPACE - 8 - fm.stringWidth(label);
            int y = LABEL_SPACE + row * CELL_SIZE + (CELL_SIZE + fm.getAscent()) / 2 - 3;

            g2.drawString(label, x, y);
        }
    }

    // Geht alle Felder durch und zeichnet ihren Inhalt
    private void drawCells(Graphics2D g2) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                CellState state = cells[row][col];

                int x = LABEL_SPACE + col * CELL_SIZE;
                int y = LABEL_SPACE + row * CELL_SIZE;

                drawCellContent(g2, x, y, state);
            }
        }
    }

    // Zeichnet je nach Zustand Schiff, Treffer oder Fehlschuss
    private void drawCellContent(Graphics2D g2, int x, int y, CellState state) {
        switch (state) {
            case SHIP:
                // Schiffe nur auf dem eigenen Feld sichtbar machen
                if (!enemyBoard) {
                    g2.setColor(new Color(160, 160, 160));
                    g2.fillRect(x + 1, y + 1, CELL_SIZE - 1, CELL_SIZE - 1);
                }
                break;

            case MISS:
                // Fehlschuss als kleiner dunkler Punkt
                g2.setColor(Color.DARK_GRAY);
                g2.fillOval(
                        x + CELL_SIZE / 2 - 4,
                        y + CELL_SIZE / 2 - 4,
                        8,
                        8
                );
                break;

            case HIT:
                // Treffer als roter Punkt
                g2.setColor(Color.RED);
                g2.fillOval(
                        x + CELL_SIZE / 2 - 5,
                        y + CELL_SIZE / 2 - 5,
                        10,
                        10
                );
                break;

            case EMPTY:
            default:
                // Leere Felder werden nicht extra gefüllt
                break;
        }
    }

    // Zeichnet das Raster des Spielfelds
    private void drawGrid(Graphics2D g2) {
        g2.setColor(Color.GRAY);

        int startX = LABEL_SPACE;
        int startY = LABEL_SPACE;
        int boardSize = GRID_SIZE * CELL_SIZE;

        for (int i = 0; i <= GRID_SIZE; i++) {
            int x = startX + i * CELL_SIZE;
            int y = startY + i * CELL_SIZE;

            g2.drawLine(startX, y, startX + boardSize, y);
            g2.drawLine(x, startY, x, startY + boardSize);
        }
    }

    // Wandelt eine Pixelposition der Maus in Spielfeldkoordinaten um
    public Point cellAt(Point point) {
        int x = point.x - LABEL_SPACE;
        int y = point.y - LABEL_SPACE;

        if (x < 0 || y < 0) {
            return null;
        }

        int col = x / CELL_SIZE;
        int row = y / CELL_SIZE;

        if (col >= GRID_SIZE || row >= GRID_SIZE) {
            return null;
        }

        return new Point(col, row);
    }

    // Erzeugt ein leeres 10x10-Board
    private CellState[][] createEmptyBoard() {
        CellState[][] board = new CellState[GRID_SIZE][GRID_SIZE];

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                board[row][col] = CellState.EMPTY;
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
}