package models;

import java.util.EnumMap;
import java.util.Map;

public final class ShipLimitRules {
    private ShipLimitRules() {}

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

    public static int getMaxCount(int boardSize, ShipType shipType) {
        return getLimits(boardSize).getOrDefault(shipType, 0);
    }
}