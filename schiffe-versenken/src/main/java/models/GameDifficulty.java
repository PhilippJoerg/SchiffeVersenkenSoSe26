package models;

public enum GameDifficulty {
    EASY("Einfach"),
    MEDIUM("Mittel (not implemented)"),
    HARD("Schwer (not implemented)");

    private final String displayName;

    GameDifficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}