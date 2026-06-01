package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.GraphicsEnvironment;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import models.GameDifficulty;

class StartScreenPanelSmokeTest {

    @Test
    void smokeDefaults() throws Exception {
        Assumptions.assumeFalse(
                GraphicsEnvironment.isHeadless(),
                "Skipping GUI test: no display available (headless environment)");

        final StartScreenPanel[] panelHolder = new StartScreenPanel[1];
        SwingUtilities.invokeAndWait(() -> panelHolder[0] = new StartScreenPanel());

        StartScreenPanel panel = panelHolder[0];
        assertNotNull(panel);
        assertEquals("", panel.getTextFieldValue());
        assertEquals(10, panel.getBoardSize());
        assertEquals("COMPUTER", panel.getOpponentSelection());
        assertEquals(GameDifficulty.EASY, panel.getSelectedDifficulty());
    }
}
