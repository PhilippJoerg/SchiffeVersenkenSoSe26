package models;

import java.util.EnumMap;
import java.util.Map;

/**
 * de: Die Klasse ShipLimitRules enthält die Regeln für die maximale Anzahl von Schiffen.
 * en: The class ShipLimitRules contains the rules for the maximum number of ships.
 */
public final class ShipLimitRules {
    private ShipLimitRules() {}

    /**
     * de: Liefert die maximalen Schiffszahlen für eine gegebene Brettgröße.
     * en: Returns the maximum ship counts for a given board size.
     *
     * @param boardSize de: Parameter boardSize. en: Parameter boardSize.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
    public static Map<ShipType, Integer> getLimits(int boardSize) {
        EnumMap<ShipType, Integer> limits = new EnumMap<>(ShipType.class);

        switch (boardSize) {
            case 8 -> {
                limits.put(ShipType.BATTLESHIP, 1);
                limits.put(ShipType.CRUISER, 1);
                limits.put(ShipType.DESTROYER, 2);
                limits.put(ShipType.SUBMARINE, 3);
            }
            case 12 -> {
                limits.put(ShipType.BATTLESHIP, 2);
                limits.put(ShipType.CRUISER, 3);
                limits.put(ShipType.DESTROYER, 4);
                limits.put(ShipType.SUBMARINE, 5);
            }
            default -> {
                limits.put(ShipType.BATTLESHIP, 1);
                limits.put(ShipType.CRUISER, 2);
                limits.put(ShipType.DESTROYER, 3);
                limits.put(ShipType.SUBMARINE, 4);
            }
        }

        return limits;
    }

    /**
     * de: Liefert die maximale Anzahl eines bestimmten Schiffstyps für eine gegebene Brettgröße.
     * en: Returns the maximum count of a specific ship type for a given board size.
     *
     * @param boardSize de: Parameter boardSize. en: Parameter boardSize.
     * @param shipType de: Parameter shipType. en: Parameter shipType.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
    public static int getMaxCount(int boardSize, ShipType shipType) {
        return getLimits(boardSize).getOrDefault(shipType, 0);
    }
}