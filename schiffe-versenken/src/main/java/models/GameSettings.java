package models;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class GameSettings {

    // Feld für die Spielfeldgröße
    private final int boardSize;

    // Feld für die Anzahl je Schiffstyp
    private final EnumMap<ShipType, Integer> shipCounts;

    public GameSettings(int boardSize, Map<ShipType, Integer> shipCounts) {
        if (boardSize <= 0) {
            throw new IllegalArgumentException("boardSize must be greater than 0");
        }
        this.boardSize = boardSize;
        this.shipCounts = new EnumMap<>(ShipType.class);
        this.shipCounts.putAll(Objects.requireNonNull(shipCounts, "shipCounts must not be null"));
    }

    public int getBoardSize() {
        return boardSize;
    }

    public int getShipCount(ShipType shipType) {
        return shipCounts.getOrDefault(shipType, 0);
    }

    public Map<ShipType, Integer> getShipCounts() {
        return new EnumMap<>(shipCounts);
    }

    public static GameSettings defaultSettings() {
        EnumMap<ShipType, Integer> defaultShipCounts = new EnumMap<>(ShipType.class);

        for (ShipType type : ShipType.values()) {
            defaultShipCounts.put(type, type.getAmount());
        }

        return new GameSettings(10, defaultShipCounts);
    }
}
