package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComTest {

    private Com com;
    private StringWriter writer;
    private TestListener listener;

    static class TestListener implements Com.Listener {
        int coin = -1;
        int sizeRows = -1;
        int sizeCols = -1;
        int[] ships;
        boolean done;
        boolean ready;
        boolean pass;
        boolean ok;

        @Override
        public void onCoin(int coin) {
            this.coin = coin;
        }

        @Override
        public void onSize(int rows, int cols) {
            this.sizeRows = rows;
            this.sizeCols = cols;
        }

        @Override
        public void onShips(int[] lengths) {
            this.ships = lengths;
        }

        @Override
        public void onDone() {
            done = true;
        }

        @Override
        public void onReady() {
            ready = true;
        }

        @Override
        public void onPass() {
            pass = true;
        }

        @Override
        public void onOk() {
            ok = true;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        listener = new TestListener();
        com = new Com(listener);
        writer = new StringWriter();
        Field outField = Com.class.getDeclaredField("out");
        outField.setAccessible(true);
        outField.set(com, writer);
    }

    @Test
    void testSendCommandsWriteExpectedProtocolLines() throws IOException {
        com.sendShot(1, 2);
        com.sendAnswer(0);
        com.sendPass();
        com.sendCoin(1);
        com.sendSize(10);
        com.sendShips(5, 4, 3);
        com.sendDone();
        com.sendReady();
        com.sendOk();
        com.sendSave(42);
        com.sendLoad(13);

        String output = writer.toString();
        assertNotNull(output);
        assertTrue(output.contains("shot 1 2\n"));
        assertTrue(output.contains("answer 0\n"));
        assertTrue(output.contains("pass\n"));
        assertTrue(output.contains("COIN 1\n"));
        assertTrue(output.contains("size 10\n"));
        assertTrue(output.contains("ships 5 4 3\n"));
        assertTrue(output.contains("done\n"));
        assertTrue(output.contains("ready\n"));
        assertTrue(output.contains("ok\n"));
        assertTrue(output.contains("save 42\n") || output.contains("load 13\n"));
    }

    @Test
    void testHandleLineParsesIncomingProtocol() throws Exception {
        Method method = Com.class.getDeclaredMethod("handleLine", String.class);
        method.setAccessible(true);

        method.invoke(com, "COIN 1");
        method.invoke(com, "shot 2 3");
        method.invoke(com, "answer 1");
        method.invoke(com, "done");
        method.invoke(com, "ready");
        method.invoke(com, "size 5 5");
        method.invoke(com, "ships 5 4 3");
        method.invoke(com, "pass");
        method.invoke(com, "ok");

        assertEquals(1, listener.coin);
        assertEquals(5, listener.sizeRows);
        assertEquals(5, listener.sizeCols);
        assertNotNull(listener.ships);
        assertEquals(true, listener.done);
        assertEquals(true, listener.ready);
        assertEquals(true, listener.pass);
        assertEquals(true, listener.ok);
    }

    @Test
    void testSaveGameAndFireShotControlState() throws Exception {
        com.setCurrentState(Com.State.MY_TURN);
        com.fireShot(3, 4);
        assertEquals(Com.State.WAITING_ANSWER, com.getCurrentState());

        com.setCurrentState(Com.State.START);
        com.saveGame();
        assertEquals(Com.State.WAITING_OK_SAVE, com.getCurrentState());
        assertTrue(writer.toString().startsWith("shot 3 4\nsave "));
    }
}
