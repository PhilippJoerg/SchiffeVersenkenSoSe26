package models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * de: Testet die Enum-Werte von GameDifficulty.
 * en: Tests the enum values of GameDifficulty.
 */
class GameDifficultyTest {

    /**
     * de: Reagiert auf das Ereignis "TestGetDisplayNameAndToString".
     * en: Responds to the "TestGetDisplayNameAndToString" event.
     *
     */
    @Test
    void testGetDisplayNameAndToString() {
        assertEquals("Einfach", GameDifficulty.EASY.getDisplayName());
        assertEquals("Einfach", GameDifficulty.EASY.toString());
        assertEquals("Mittel", GameDifficulty.MEDIUM.getDisplayName());
        assertEquals("Mittel", GameDifficulty.MEDIUM.toString());
        assertEquals("Schwer (not implemented)", GameDifficulty.HARD.getDisplayName());
        assertEquals("Schwer (not implemented)", GameDifficulty.HARD.toString());
    }

    /**
     * de: Reagiert auf das Ereignis "TestValuesContainThreeDifficulties".
     * en: Responds to the "TestValuesContainThreeDifficulties" event.
     *
     */
    @Test
    void testValuesContainThreeDifficulties() {
        assertEquals(3, GameDifficulty.values().length);
    }
}
