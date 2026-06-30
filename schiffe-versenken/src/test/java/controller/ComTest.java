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

/**
 * de: Testet die Klasse Com.
 * en: Tests the Com class.
 */
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

        /**
         * de: Reagiert auf das Ereignis "Coin".
         * en: Responds to the "Coin" event.
         *
         * @param coin de: Parameter coin. en: Parameter coin.
         */
        @Override
        public void onCoin(int coin) {
            this.coin = coin;
        }

        /**
         * de: Reagiert auf das Ereignis "Size".
         * en: Responds to the "Size" event.
         *
         * @param rows de: Parameter rows. en: Parameter rows.
         * @param cols de: Parameter cols. en: Parameter cols.
         */
        @Override
        public void onSize(int rows, int cols) {
            this.sizeRows = rows;
            this.sizeCols = cols;
        }

        /**
         * de: Reagiert auf das Ereignis "Ships".
         * en: Responds to the "Ships" event.
         *
         * @param lengths de: Parameter lengths. en: Parameter lengths.
         */
        @Override
        public void onShips(int[] lengths) {
            this.ships = lengths;
        }

        /**
         * de: Reagiert auf das Ereignis "Done".
         * en: Responds to the "Done" event.
         *
         */
        @Override
        public void onDone() {
            done = true;
        }

        /**
         * de: Reagiert auf das Ereignis "Ready".
         * en: Responds to the "Ready" event.
         *
         */
        @Override
        public void onReady() {
            ready = true;
        }

        /**
         * de: Reagiert auf das Ereignis "Pass".
         * en: Responds to the "Pass" event.
         *
         */
        @Override
        public void onPass() {
            pass = true;
        }

        /**
         * de: Reagiert auf das Ereignis "Ok".
         * en: Responds to the "Ok" event.
         *
         */
        @Override
        public void onOk() {
            ok = true;
        }
    }

    /**
     * de: Reagiert auf das Ereignis "SetUp".
     * en: Responds to the "SetUp" event.
     *
     * @throws Exception de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
    @BeforeEach
    void setUp() throws Exception {
        listener = new TestListener();
        com = new Com(listener);
        writer = new StringWriter();
        Field outField = Com.class.getDeclaredField("out");
        outField.setAccessible(true);
        outField.set(com, writer);
    }

    /**
     * de: Testet, ob die Methode sendCommands die erwarteten Protokollzeilen schreibt.
     * en: Tests if the sendCommands method writes the expected protocol lines.
     *
     * @throws IOException de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
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

    /**
     * de: Testet, ob die Methode handleLine eingehende Protokollzeilen korrekt parst.
     * en: Tests if the handleLine method correctly parses incoming protocol lines.
     *
     * @throws Exception de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
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

    /**
     * de: Testet, ob die Methode saveGame und fireShot den Steuerzustand korrekt aktualisieren.
     * en: Tests if the saveGame and fireShot methods correctly update the control state.
     *
     * @throws Exception de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
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
