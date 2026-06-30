package models;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * de: Die Klasse GameSettings enthält die Einstellungen für ein Spiel, einschließlich Spielfeldgröße und Schiffszahlen.
 * en: The GameSettings class contains the settings for a game, including board size and ship counts.
 */
public class GameSettings {

    // Feld für die Spielfeldgröße
    private final int boardSize;

    // Feld für die Anzahl je Schiffstyp
    private final EnumMap<ShipType, Integer> shipCounts;

    /**
     * de: Konstruktor für GameSettings.
     * en: Constructor for GameSettings.
     *
     * @param boardSize de: Parameter boardSize. en: Parameter boardSize.
     * @param shipCounts de: Parameter shipCounts. en: Parameter shipCounts.
     */
    public GameSettings(int boardSize, Map<ShipType, Integer> shipCounts) {
        if (boardSize <= 0) {
            throw new IllegalArgumentException("boardSize must be greater than 0");
        }
        this.boardSize = boardSize;
        this.shipCounts = new EnumMap<>(ShipType.class);
        this.shipCounts.putAll(Objects.requireNonNull(shipCounts, "shipCounts must not be null"));
    }

    /**
     * de: Liefert die Größe des Spielfelds zurück.
     * en: Returns the size of the game board.
     *
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
    public int getBoardSize() {
        return boardSize;
    }

    /**
     * de: Liefert die Anzahl der Schiffe eines bestimmten Typs zurück.
     * en: Returns the count of ships of a specific type.
     *
     * @param shipType de: Parameter shipType. en: Parameter shipType.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
    public int getShipCount(ShipType shipType) {
        return shipCounts.getOrDefault(shipType, 0);
    }

    /**
     * de: Liefert die Anzahl aller Schiffe zurück.
     * en: Returns the count of all ships.
     *
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
    public Map<ShipType, Integer> getShipCounts() {
        return new EnumMap<>(shipCounts);
    }

    /**
     * de: Liefert die Standard-Spiel-Einstellungen zurück.
     * en: Returns the default game settings.
     *
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
    public static GameSettings defaultSettings() {
        EnumMap<ShipType, Integer> defaultShipCounts = new EnumMap<>(ShipType.class);

        for (ShipType type : ShipType.values()) {
            defaultShipCounts.put(type, type.getAmount());
        }

        return new GameSettings(10, defaultShipCounts);
    }
}
