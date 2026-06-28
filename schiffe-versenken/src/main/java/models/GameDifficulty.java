/*
 * Datei: models/GameDifficulty.java
 * Definiert die Schwierigkeitsstufen des Computerspielers mit deutschen Anzeigenamen.
 */
package models;

/**
 * de: Das Enum GameDifficulty repräsentiert die möglichen Schwierigkeitsstufen des Computerspielers.
 * en: The enum GameDifficulty represents the possible difficulty levels of the computer player.
 */
public enum GameDifficulty {
    EASY("Einfach"),
    MEDIUM("Mittel"),
    HARD("Schwer");

    private final String displayName;

    /**
     * de: Erzeugt eine Schwierigkeitsstufe mit dem zugehoerigen Anzeigenamen.
     * en: Creates a difficulty level with its display name.
     *
     * @param displayName de: Anzeigename der Schwierigkeit. en: Display name of the difficulty.
     */
    GameDifficulty(String displayName) {
        this.displayName = displayName;
    }

    /**
     * de: Liefert den Anzeigenamen der Schwierigkeitsstufe.
     * en: Returns the display name of the difficulty level.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * de: Gibt den Anzeigenamen zurück, damit die Enum als Text dargestellt werden kann.
     * en: Returns the display name, allowing the enum to be represented as text.
     */
    @Override
    public String toString() {
        return displayName;
    }
} 