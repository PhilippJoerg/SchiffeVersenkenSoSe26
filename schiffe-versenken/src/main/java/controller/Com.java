/*
 * Datei: controller/Com.java
 * Kommunikation und Protokoll-Handling: Senden/Empfangen von Nachrichten zwischen Host und Client.
 * Enthält Hilfsfunktionen zum Aufbau von Server-/Client-Verbindungen und einen Listener-Callback.
 */
package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * de: Die Klasse Com.
 * en: The class Com.
 */
public class Com {
    /**
     * de: Das Enum State.
     * en: The enum State.
     */
    public enum State {
        START,
        WAITING_FOR_SIZE_DONE,
        WAITING_DONE_SHIPS,
        WAITING_READY,
        MY_TURN,
        ENEMY_TURN,
        WAITING_PASS,
        WAITING_ANSWER,
        WAITING_OK_SAVE,
        WAITING_OK_LOAD,
        GAME_OVER
    }

    private State currentState = State.START;
    private State previousState = null;
    private int[] shipLengths = null;
    private int numRows = 0;
    private int numCols = 0;

    /**
     * de: Die Schnittstelle Listener.
     * en: The interface Listener.
     */
    public interface Listener {
        /**
         * de: Wird aufgerufen, wenn eine Münze empfangen wird.
         * en: Called when a coin is received.
         *
         * @param coin de: Parameter coin. en: Parameter coin.
         */
        default void onCoin(int coin) {
        }

        /**
         * de: Wird aufgerufen, wenn die Größe des Spielfelds empfangen wird.
         * en: Called when the size of the game board is received.
         *
         * @param rows de: Parameter rows. en: Parameter rows.
         * @param cols de: Parameter cols. en: Parameter cols.
         */
        default void onSize(int rows, int cols) {
        }

        /**
         * de: Wird aufgerufen, wenn die Schiffe empfangen werden.
         * en: Called when the ships are received.
         *
         * @param lengths de: Parameter lengths. en: Parameter lengths.
         */
        default void onShips(int[] lengths) {
        }

        /**
         * de: Wird aufgerufen, wenn die Platzierung abgeschlossen ist.
         * en: Called when the placement is done.
         *
         */
        default void onDone() {
        }

        /**
         * de: Wird aufgerufen, wenn das Spiel bereit ist.
         * en: Called when the game is ready.
         *
         */
        default void onReady() {
        }

        /**
         * de: Wird aufgerufen, wenn ein Schuss abgegeben wird.
         * en: Called when a shot is fired.
         *
         * @param col de: Parameter col. en: Parameter col.
         * @param row de: Parameter row. en: Parameter row.
         */
        default void onShot(int col, int row) {
        }

        /**
         * de: Wird aufgerufen, wenn eine Antwort empfangen wird.
         * en: Called when an answer is received.
         *
         * @param answer de: Parameter answer. en: Parameter answer.
         */
        default void onAnswer(int answer) {
        }

        /**
         * de: Wird aufgerufen, wenn ein Zug übergeben wird.
         * en: Called when a turn is passed.
         *
         */
        default void onPass() {
        }

        /**
         * de: Wird aufgerufen, wenn ein Spiel gespeichert wird.
         * en: Called when a game is saved.
         *
         * @param id de: Parameter id. en: Parameter id.
         */
        default void onSave(long id) {
        }

        /**
         * de: Wird aufgerufen, wenn ein Spiel geladen wird.
         * en: Called when a game is loaded.
         *
         * @param id de: Parameter id. en: Parameter id.
         */
        default void onLoad(long id) {
        }

        /**
         * de: Wird aufgerufen, wenn eine Bestätigung empfangen wird.
         * en: Called when an acknowledgment is received.
         *
         */
        default void onOk() {
        }

        /**
         * de: Wird aufgerufen, wenn eine Verbindung hergestellt wird.
         * en: Called when a connection is established.
         *
         */
        default void onConnected() {
        }

        /**
         * de: Wird aufgerufen, wenn die Verbindung getrennt wird.
         * en: Called when the connection is disconnected.
         *
         */
        default void onDisconnected() {
        }
    }

    private BufferedReader in;
    private Writer out;
    private Listener listener;
    private Socket socket;

    /**
     * de: Erzeugt ein Com-Objekt mit einem Listener für eingehende Netzwerkereignisse.
     * en: Creates a Com object with a listener for incoming network events.
     */
    public Com(Listener listener) {
        this.listener = listener;
    }

    /**
     * de: Setzt den Listener für eingehende Nachrichten.
     * en: Sets the listener for incoming messages.
     */
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * de: Sendet eine rohe Textnachricht über den verbundenen Socket.
     * en: Sends a raw text message over the connected socket.
     */
    private void sendRaw(String msg) throws IOException {
        out.write(msg + "\n");
        out.flush();
    }

    /**
     * de: Sendet einen Schussbefehl an den Gegner.
     * en: Sends a shot command to the opponent.
     */
    public void sendShot(int col, int row) throws IOException {
        sendRaw("shot " + col + " " + row);
    }

    /**
     * de: Sendet die Antwort auf einen Schuss (0 = Wasser / 1 = Treffer/ 2 = Treffer-versenkt ).
     * en: Sends the response to a shot (0 = water / 1 = hit / 2 = hit-sunk).
     */
    public void sendAnswer(int answer) throws IOException {
        sendRaw("answer " + answer);
    }

    /**
     * de: Sendet eine Pass-Nachricht, wenn der Gegner an der Reihe ist.
     * en: Sends a pass message when it's the opponent's turn.
     */
    public void sendPass() throws IOException {
        sendRaw("pass");
    }

    /**
     * de: Sendet ein Zufallszahl-Token für den Startspielervergleich.
     * en: Sends a random number token for the starting player comparison.
     */
    public void sendCoin(int coin) throws IOException {
        sendRaw("COIN " + coin);
    }

    /**
     * de: Sendet die Größe des Spielfeldes an den Gegner.
     * en: Sends the size of the game board to the opponent.
     */
    public void sendSize(int size) throws IOException {
        sendRaw("size " + size);
    }

    /**
     * de: Sendet die Schiffslängen zur Synchronisation des Handshake-Protokolls.
     * en: Sends the ship lengths for handshake protocol synchronization.
     */
    public void sendShips(int... lengths) throws IOException {
        StringBuilder builder = new StringBuilder("ships");
        for (int length : lengths) {
            builder.append(' ').append(length);
        }
        sendRaw(builder.toString());
    }

    /**
     * de: Signalisiert dem Gegner, dass die Schiffplatzierung abgeschlossen ist.
     * en: Signals the opponent that the ship placement is complete.
     */
    public void sendDone() throws IOException {
        sendRaw("done");
    }

    /**
     * de: Signalisiert dem Gegner, dass beide Parteien bereit sind.
     * en: Signals the opponent that both parties are ready.
     */
    public void sendReady() throws IOException {
        sendRaw("ready");
    }

    /**
     * de: Bestätigt eine vorherige Nachricht mit OK.
     * en: Confirms a previous message with OK.
     */
    public void sendOk() throws IOException {
        sendRaw("ok");
    }

    /**
     * de: Sendet einen Save-Befehl mit einer gespeicherten Spiel-ID.
     * en: Sends a save command with a stored game ID.
     */
    public void sendSave(long id) throws IOException {
        sendRaw("save " + id);
    }

    /**
     * de: Sendet einen Load-Befehl für eine gespeicherte Spiel-ID.
     * en: Sends a load command for a stored game ID.
     */
    public void sendLoad(long id) throws IOException {
        sendRaw("load " + id);
    }

    /**
     * de: Ermittelt verfügbare lokale IPv4-Adressen zur Anzeige im Verbindungsdialog.
     * en: Retrieves available local IPv4 addresses for display in the connection dialog.
     */
    public static String getLocalIpAddresses() {
        try {
            List<String> addresses = new ArrayList<>();
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    InetAddress ia = ias.nextElement();
                    if (!ia.isLoopbackAddress() && ia instanceof Inet4Address) {
                        addresses.add(ia.getHostAddress());
                    }
                }
            }
            return String.join(", ", addresses);
        } catch (SocketException e) {
            return "";
        }
    }

    /**
     * de: Startet einen Server-Socket und wartet auf die Verbindung eines Clients.
     * en: Starts a server socket and waits for a client connection.
     */
    public void connectAsServer(int port) throws IOException {
        try (ServerSocket ss = new ServerSocket(port)) {
            socket = ss.accept();
        }
        setupStreamsAndReader();
        if (listener != null)
            listener.onConnected();
    }

    /**
     * de: Verbindet als Client zu einem entfernten Host.
     * en: Connects as a client to a remote host.
     */
    public void connectAsClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        setupStreamsAndReader();
        if (listener != null)
            listener.onConnected();
    }

    /**
     * de: Initialisiert die Ein- und Ausgabeströme und startet den Hintergrundleser.
     * en: Initializes the input and output streams and starts the background reader.
     */
    private void setupStreamsAndReader() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new OutputStreamWriter(socket.getOutputStream());

        Thread reader = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    handleLine(line.trim());
                }
            } catch (IOException e) {
                // connection lost
            } finally {
                try {
                    socket.shutdownOutput();
                    socket.close();
                } catch (Exception ex) {
                }
                if (listener != null)
                    listener.onDisconnected();
            }
        }, "Com-Reader");
        reader.setDaemon(true);
        reader.start();
    }

    /**
     * de: Verarbeitet eine eingehende Protokollzeile und ruft entsprechende Listener-Methoden auf.
     * en: Processes an incoming protocol line and calls the corresponding listener methods.
     */
    private void handleLine(String line) {
        if (line.isEmpty())
            return;
        String[] parts = line.split(" ");
        String cmd = parts[0];
        try {
            switch (cmd) {
                case "COIN":
                    if (parts.length > 1 && listener != null) {
                        listener.onCoin(Integer.parseInt(parts[1]));
                    }
                    break;
                case "shot":
                    if (parts.length > 2 && listener != null) {
                        int r = Integer.parseInt(parts[1]);
                        int c = Integer.parseInt(parts[2]);
                        listener.onShot(r, c);
                    }
                    break;
                case "answer":
                    if (parts.length > 1 && listener != null) {
                        listener.onAnswer(Integer.parseInt(parts[1]));
                    }
                    break;
                case "done":
                    if (listener != null)
                        listener.onDone();
                    break;
                case "ready":
                    if (listener != null)
                        listener.onReady();
                    break;
                case "size":
                    if (parts.length > 1 && listener != null) {
                        int rows = Integer.parseInt(parts[1]);
                        int cols = parts.length > 2 ? Integer.parseInt(parts[2]) : rows;
                        numRows = rows;
                        numCols = cols;
                        listener.onSize(rows, cols);
                    }
                    break;
                case "ships":
                    if (parts.length > 1 && listener != null) {
                        int[] lengths = new int[parts.length - 1];
                        for (int i = 1; i < parts.length; i++) {
                            lengths[i - 1] = Integer.parseInt(parts[i]);
                        }
                        shipLengths = lengths;
                        listener.onShips(lengths);
                    }
                    break;
                case "pass":
                    if (listener != null)
                        listener.onPass();
                    break;
                case "save":
                    if (parts.length > 1 && listener != null) {
                        long id = Long.parseLong(parts[1]);
                        listener.onSave(id);
                        try { sendOk(); } catch (IOException ex) { }
                    }
                    break;
                case "load":
                    if (parts.length > 1 && listener != null) {
                        long id = Long.parseLong(parts[1]);
                        listener.onLoad(id);
                        try { sendOk(); } catch (IOException ex) { }
                    }
                    break;
                case "ok":
                    if (listener != null) {
                        listener.onOk();
                    }
                    if (currentState == State.WAITING_OK_SAVE && previousState != null) {
                        currentState = previousState;
                        previousState = null;
                    } else if (currentState == State.WAITING_OK_LOAD) {
                        currentState = State.MY_TURN;
                    }
                    break;
                default:
            }
        } catch (Exception e) {
            // ignore parse errors
        }
    }

    /**
     * de: Schließt die Netzwerkverbindung und den zugehörigen Socket.
     * en: Closes the network connection and the associated socket.
     */
    public void close() {
        try {
            if (socket != null)
                socket.close();
        } catch (Exception e) {
        }
    }

    /**
     * de: Sendet eine Save-Anfrage und wechselt in den entsprechenden Wartezustand.
     * en: Sends a save request and switches to the corresponding waiting state.
     */
    public void saveGame() throws IOException {
        previousState = currentState;
        long id = System.currentTimeMillis();
        sendSave(id);
        currentState = State.WAITING_OK_SAVE;
    }

    /**
     * de: Sendet einen Schuss, wenn der interne Zustand anzeigt, dass der Zug erlaubt ist.
     * en: Sends a shot if the internal state indicates that the turn is allowed.
     */
    public void fireShot(int row, int col) throws IOException {
        if (currentState != State.MY_TURN) {
            return;
        }
        sendShot(row, col);
        currentState = State.WAITING_ANSWER;
    }

    // 
    /**
     * de: Ruft das Feld currentState ab.
     * en: Gets the field currentState.
     */
    public State getCurrentState() { return currentState; }

    /**
     * de: Setzt das Feld currentState.
     * en: Sets the field currentState.
     */
    public void setCurrentState(State s) { this.currentState = s; }
    /**
     * de: Ruft das Feld numRows ab.
     * en: Gets the field numRows.
     */

    public int getNumRows() { return numRows; }
    /**
     * de: Ruft das Feld numCols ab.
     * en: Gets the field numCols.
     */

    public int getNumCols() { return numCols; }
    /**
     * de: Ruft das Feld shipLengths ab.
     * en: Gets the field shipLengths.
     */
    public int[] getShipLengths() { return shipLengths; }
}
