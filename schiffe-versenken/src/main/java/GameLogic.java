import java.util.Scanner;

/*
* TODO: 
* Erkennen das ein Schiff versenkt wurde (alle Felder eines Schiffes getroffen wurden)
* Besseres Trefferfeedback (Weiterer zustand: Feld beschossen aber kein Treffer/ und Treffer)
*/

/**
 * BattleShip!
 */
public class GameLogic {
    // So viele felder müssen belegt sein
    public static int quota = 30;
    public static int player1, player2 = 0;
    public static boolean testing = true;

    public static void main(String[] args) {
        /* 
        * Ablauf:
        * 1. Begrüßung & Regeln 
        * 2. Spielfeld erstellen (10x10 für den anfang)
        * 3. Schiffe setzen (30% sollen belegt sein) 
        *    Schiffstyp     Anzahl	Größe (Kästchen)
        *    Schlachtschiff	1	    5
        *    Kreuzer	    2	    4
        *    Zerstörer	    3	    3
        *    U-Boote	    4	    2
        * 4. Solange das Spiel nicht vorbei ist:
        *    a) Spieler nach Koordinaten fragen
        *    b) Schuss ausführen
        *    c) Ergebnis anzeigen
        * 5. Spiel beenden
        */
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to BattleShip!");
        System.out.println("Gespielt wird auf einem 10x10 Spielfeld.");

        System.out.println("Spieler 1 wie ist dein Name?");
        GameGrid player1Grid;
        if (testing) {
            player1Grid = new GameGrid("Phil");
        } else {
            player1Grid = new GameGrid(scanner.nextLine());
        }

        System.out.println("Spieler 2 wie ist dein Name?");
        GameGrid player2Grid;
        if (testing) {
            player2Grid = new GameGrid("Dodo");
        } else {
            player2Grid = new GameGrid(scanner.nextLine());
        }

        System.out.println("\nEs müssen insgesamt 30 Felder mit Schiffen belegt werden.");
        System.out.println("Folgende Schiffe müssen plaziert werden, die Schiffe fürden sich nicht berühren.");
        System.out.println("Schlachtschiff Anzahl: 1 Größe: 5");
        System.out.println("Kreuzer Anzahl: 2 Größe: 4");
        System.out.println("Zerstörer Anzahl: 3 Größe: 3");
        System.out.println("U-Boote Anzahl: 4 Größe: 2");

        // Spieler 1 setzt seine Schiffe
        System.out.println(
                "\nSpieler 1 wo möchtest du die Schiffe platzieren? Bitte mache deine Eingabe im Format \"x y\".");
        if (testing) {
            player1Grid.placeShipRND(5);
            player1Grid.placeShipRND(4);
            player1Grid.placeShipRND(4);
            player1Grid.placeShipRND(3);
            player1Grid.placeShipRND(3);
            player1Grid.placeShipRND(3);
            player1Grid.placeShipRND(2);
            player1Grid.placeShipRND(2);
            player1Grid.placeShipRND(2);
            player1Grid.placeShipRND(2);
            player1 = quota;
            player1Grid.showGameGridInConsole();
        } else {
            player1Grid.showGameGridInConsole();
            while (player1 <= quota) {
                if (scanner.hasNextInt()) {
                    int x = scanner.nextInt();
                    int y = scanner.nextInt();
                    if (x >= 0 && x < 10 && y >= 0 && y < 10 && player1Grid.placeShip(x, y)) {
                        player1++;
                        player1Grid.showGameGridInConsole();
                    } else {
                        System.out.println("Das hat nicht geklappt.");
                    }
                }
            }
        }

        // Spieler 2 setzt seine Schiffe
        System.out.println(
                "\nSpieler 2 wo möchtest du die Schiffe platzieren? Bitte mache deine Eingabe im Format \"x y\".");
        if (testing) {
            player2Grid.placeShipRND(5);
            player2Grid.placeShipRND(4);
            player2Grid.placeShipRND(4);
            player2Grid.placeShipRND(3);
            player2Grid.placeShipRND(3);
            player2Grid.placeShipRND(3);
            player2Grid.placeShipRND(2);
            player2Grid.placeShipRND(2);
            player2Grid.placeShipRND(2);
            player2Grid.placeShipRND(2);
            player2 = quota;
            player2Grid.showGameGridInConsole();
        } else {
            player2Grid.showGameGridInConsole();
            while (player2 <= quota) {
                if (scanner.hasNextInt()) {
                    int x = scanner.nextInt();
                    int y = scanner.nextInt();
                    if (x >= 0 && x < 10 && y >= 0 && y < 10 && player1Grid.placeShip(x, y)) {
                        player2++;
                        player2Grid.showGameGridInConsole();
                    } else {
                        System.out.println("Das hat nicht geklappt.");
                    }
                }
            }
        }

        // Schüsse schiesen bis ein Spieler keine Schiffe mehr hat.
        System.out.println(player1Grid.getName()
                + " beginnt, wohin willst du schiesen? Bitte mache deine Eingabe im Format \"x y\".");
        do {
            while (true) {
                System.out.print(player1Grid.getName() + ": ");
                if (scanner.hasNextInt()) {
                    int x = scanner.nextInt();
                    int y = scanner.nextInt();
                    if (x >= 0 && x < 10 && y >= 0 && y < 10) {
                        if (player2Grid.shoot(x, y)) {
                            System.out.println("Teffer!");
                            if (player2 == 0) {
                                break;
                            }
                            player2--;
                            player2Grid.showGameGridInConsole();
                        } else {
                            System.out.println("Daneben!");
                            break;
                        }
                    } else {
                        System.out.println("Das hat nicht geklappt.");
                    }
                }
            }

            while (true) {
                System.out.print(player2Grid.getName() + ": ");
                if (scanner.hasNextInt()) {
                    int x = scanner.nextInt();
                    int y = scanner.nextInt();
                    if (x >= 0 && x < 10 && y >= 0 && y < 10) {
                        if (player1Grid.shoot(x, y)) {
                            System.out.println("Teffer!");
                            if (player1 == 0) {
                                break;
                            }
                            player1--;
                            player1Grid.showGameGridInConsole();
                        } else {
                            System.out.println("Daneben!");
                            break;
                        }
                    } else {
                        System.out.println("Das hat nicht geklappt.");
                    }
                }
            }

        } while (player1 > 0 && player2 > 0);

        if (player1 == 0) {
            System.out.println(player2Grid.getName() + " hat gewonnen!");
        } else {
            System.out.println(player1Grid.getName() + " hat gewonnen!");
        }

        scanner.close();
    }
}
