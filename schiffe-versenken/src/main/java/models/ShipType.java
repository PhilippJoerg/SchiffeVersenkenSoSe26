package models;

public enum ShipType {
    BATTLESHIP("Schlachtschiff", 1, 5),
    CRUISER("Kreuzer", 2, 4),
    DESTROYER("Zerstörer", 3, 3),
    SUBMARINE("U-Boot", 4, 2);

    private final String displayName;
    private final int amount;
    private final int size;

    ShipType(String displayName, int amount, int size) {
        this.displayName = displayName;
        this.amount = amount;
        this.size = size;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAmount() {
        return amount;
    }

    public int getSize() {
        return size;
    }
}