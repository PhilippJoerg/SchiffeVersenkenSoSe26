package models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GameDifficultyTest {

    @Test
    void testGetDisplayNameAndToString() {
        assertEquals("Einfach", GameDifficulty.EASY.getDisplayName());
        assertEquals("Einfach", GameDifficulty.EASY.toString());
        assertEquals("Mittel", GameDifficulty.MEDIUM.getDisplayName());
        assertEquals("Mittel", GameDifficulty.MEDIUM.toString());
        assertEquals("Schwer (not implemented)", GameDifficulty.HARD.getDisplayName());
    }

    @Test
    void testValuesContainThreeDifficulties() {
        assertEquals(3, GameDifficulty.values().length);
    }
}
