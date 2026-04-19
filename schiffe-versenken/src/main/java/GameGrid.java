import java.util.Random;

public class GameGrid {
    // Eigenschaften
    private static int FIELD_SIZE = 10;
    private Field[][] field;
    private String name;
    private static final Random random = new Random();

    // Konstruktor
    public GameGrid(String n) {
        name = n;
        field = new Field[FIELD_SIZE][FIELD_SIZE];
        // Anfangswerte setzen 
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                field[j][i] = new Field();
            }
        }
    }

    // Methoden

    // Funktion zum automatischen Platzieren eines Schiffes bestimmter Länge
    public void placeShipRND(int length) {
        boolean placed = false;
        while (!placed) {
            int x = random.nextInt(FIELD_SIZE);
            int y = random.nextInt(FIELD_SIZE);
            boolean horizontal = random.nextBoolean();

            if (canPlaceShip(x, y, length, horizontal)) {
                for (int i = 0; i < length; i++) {
                    if (!horizontal) {
                        field[x][y + i].setShip();
                    } else {
                        field[x + i][y].setShip();
                    }
                }
                placed = true;
            }
        }
    }

    // Prüft, ob das Schiff platzierbar ist (Spielfeldgröße + Abstand)
    private boolean canPlaceShip(int x, int y, int length, boolean horizontal) {
        if (horizontal) {
            if (x + length > FIELD_SIZE)
                return false;
            for (int i = 0; i < length; i++) {
                if (!isCellFree(x + i, y))
                    return false;
            }
        } else {
            if (y + length > FIELD_SIZE)
                return false;
            for (int i = 0; i < length; i++) {
                if (!isCellFree(x, y + i))
                    return false;
            }
        }
        return true;
    }

    // Prüft Zelle und Nachbarn auf freie Position
    private boolean isCellFree(int x, int y) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int checkX = x + i;
                int checkY = y + j;
                if (checkX >= 0 && checkX < FIELD_SIZE && checkY >= 0 && checkY < FIELD_SIZE) {
                    if (field[checkX][checkY].isShip())
                        return false;
                }
            }
        }
        return true;
    }

    public boolean placeShip(int x, int y) {
        if (field[x][y].isShip()) {
            return false;
        } else {
            field[x][y].setShip();
            return true;
        }
    }

    public boolean shoot(int x, int y) {
        if (field[x][y].shootField())
            return true;
        else
            return false;
    }

    public void showGameGridInConsole() {
        System.out.println("\nSpielfeld: " + name); // Leerzeile + Name
        System.out.println("  0123456789"); // Beschriftung
        for (int i = 0; i < 10; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 10; j++) {
                if (field[j][i].isFieldWasShot())
                    System.out.print("*");
                else {
                    if (field[j][i].isShip())
                        System.out.print("X");
                    else
                        System.out.print("-");
                }
            }
            System.out.println();
        }
    }

    public String getName() {
        return name;
    }
}