/*
 * Datei: models/GameDifficulty.java
 * Definiert die Schwierigkeitsstufen des Computerspielers mit deutschen Anzeigenamen.
 */
package models;

public enum GameDifficulty {
    EASY("Einfach"),
    MEDIUM("Mittel"),
    HARD("Schwer (not implemented)");

    private final String displayName;

    /**
     * Erzeugt eine Schwierigkeitsstufe.
     */
    GameDifficulty(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Liefert den Anzeigenamen der Schwierigkeitsstufe.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gibt den Anzeigenamen zurück, damit die Enum als Text dargestellt werden kann.
     */
    @Override
    public String toString() {
        return displayName;
    }
} 