package view;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import models.BoardUtils;

class MainFrameTest {

    @Test
    void testSettersAndActionsDoNotThrow() {
        MainFrame frame = new MainFrame();
        frame.setStatus("Test Status");
        frame.setConnectionStatus("Connected");
        frame.setLocalIpAddress("127.0.0.1");
        frame.setOwnBoard(BoardUtils.createEmptyCellBoard());
        frame.setEnemyBoard(BoardUtils.createEmptyCellBoard());

        frame.setRotateAction(() -> frame.setStatus("Rotated"));
        frame.setAutoPlaceAction(() -> frame.setStatus("Auto"));
        frame.setShootAction(() -> frame.setStatus("Shoot"));

        assertNotNull(frame.getOwnBoard());
        assertNotNull(frame.getEnemyBoard());
        assertNotNull(frame.getShipPalettePanel());
    }
}
