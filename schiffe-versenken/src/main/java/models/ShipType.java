/*
 * Datei: models/ShipType.java
 * Definition der Schiffstypen mit Anzeige-Name, Anzahl und Größe.
 */
package models;

public enum ShipType {
    BATTLESHIP("Schlachtschiff", 1, 5),
    CRUISER("Kreuzer", 2, 4),
    DESTROYER("Zerstörer", 3, 3),
    SUBMARINE("U-Boot", 4, 2);

    private final String displayName;
    private final int amount;
    private final int size;

    /**
     * Erzeugt einen Schiffstyp mit Anzeige-Name, Anzahl und Größe.
     */
    ShipType(String displayName, int amount, int size) {
        this.displayName = displayName;
        this.amount = amount;
        this.size = size;
    }

    /**
     * Liefert den deutschen Anzeigenamen des Schiffstyps.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gibt die Anzahl der Schiffe dieses Typs im Spiel an.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Liefert die Schiffslänge in Feldern.
     */
    public int getSize() {
        return size;
    }
} 