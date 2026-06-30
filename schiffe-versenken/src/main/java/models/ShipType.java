/*
 * Datei: models/ShipType.java
 * Definition der Schiffstypen mit Anzeige-Name, Anzahl und Größe.
 */
package models;

/**
 * de: Enum ShipType definiert die verschiedenen Schiffstypen im Spiel.
 * en: Enum ShipType defines the different types of ships in the game.
 */
public enum ShipType {
    BATTLESHIP("Schlachtschiff", 1, 5),
    CRUISER("Kreuzer", 2, 4),
    DESTROYER("Zerstörer", 3, 3),
    SUBMARINE("U-Boot", 4, 2);

    private final String displayName;
    private final int amount;
    private final int size;

    /**
     * de: Erzeugt einen Schiffstyp mit Anzeigename, Anzahl und Groesse.
     * en: Creates a ship type with display name, amount, and size.
     *
     * @param displayName de: Anzeigename des Schiffstyps. en: Display name of the ship type.
     * @param amount de: Anzahl dieses Schiffstyps. en: Number of ships of this type.
     * @param size de: Laenge des Schiffs in Feldern. en: Ship length in cells.
     */
    ShipType(String displayName, int amount, int size) {
        this.displayName = displayName;
        this.amount = amount;
        this.size = size;
    }

    /**
     * de: Liefert den deutschen Anzeigenamen des Schiffstyps.
     * en: Returns the display name of the ship type.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * de: Gibt die Anzahl der Schiffe dieses Typs im Spiel an.
     * en: Returns the number of ships of this type in the game.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * de: Liefert die Schiffslänge in Feldern.
     * en: Returns the length of the ship in cells.
     */
    public int getSize() {
        return size;
    }
} 