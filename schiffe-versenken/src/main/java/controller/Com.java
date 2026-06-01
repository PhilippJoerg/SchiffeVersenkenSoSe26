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

public class Com {
    // Optional internal state machine similar to the original CLI Com.java
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

    public interface Listener {
        default void onCoin(int coin) {
        }

        default void onSize(int rows, int cols) {
        }

        default void onShips(int[] lengths) {
        }

        default void onDone() {
        }

        default void onReady() {
        }

        default void onShot(int col, int row) {
        }

        default void onAnswer(int answer) {
        }

        default void onPass() {
        }

        default void onSave(long id) {
        }

        default void onLoad(long id) {
        }

        default void onOk() {
        }

        default void onConnected() {
        }

        default void onDisconnected() {
        }
    }

    private BufferedReader in;
    private Writer out;
    private Listener listener;
    private Socket socket;

    /**
     * Erzeugt ein Com-Objekt mit einem Listener für eingehende Netzwerkereignisse.
     */
    public Com(Listener listener) {
        this.listener = listener;
    }

    /**
     * Setzt den Listener für eingehende Nachrichten.
     */
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * Sendet eine rohe Textnachricht über den verbundenen Socket.
     */
    private void sendRaw(String msg) throws IOException {
        out.write(msg + "\n");
        out.flush();
    }

    /**
     * Sendet einen Schussbefehl an den Gegner.
     */
    public void sendShot(int col, int row) throws IOException {
        sendRaw("shot " + col + " " + row);
    }

    /**
     * Sendet die Antwort auf einen Schuss (0 = Wasser / 1 = Treffer/ 2 = Treffer-versenkt ).
     */
    public void sendAnswer(int answer) throws IOException {
        sendRaw("answer " + answer);
    }

    /**
     * Sendet eine Pass-Nachricht, wenn der Gegner an der Reihe ist.
     */
    public void sendPass() throws IOException {
        sendRaw("pass");
    }

    /**
     * Sendet ein Zufallszahl-Token für den Startspielervergleich.
     */
    public void sendCoin(int coin) throws IOException {
        sendRaw("COIN " + coin);
    }

    /**
     * Sendet die Größe des Spielfeldes an den Gegner.
     */
    public void sendSize(int size) throws IOException {
        sendRaw("size " + size);
    }

    /**
     * Sendet die Schiffslängen zur Synchronisation des Handshake-Protokolls.
     */
    public void sendShips(int... lengths) throws IOException {
        StringBuilder builder = new StringBuilder("ships");
        for (int length : lengths) {
            builder.append(' ').append(length);
        }
        sendRaw(builder.toString());
    }

    /**
     * Signalisiert dem Gegner, dass die Schiffplatzierung abgeschlossen ist.
     */
    public void sendDone() throws IOException {
        sendRaw("done");
    }

    /**
     * Signalisiert dem Gegner, dass beide Parteien bereit sind.
     */
    public void sendReady() throws IOException {
        sendRaw("ready");
    }

    /**
     * Bestätigt eine vorherige Nachricht mit OK.
     */
    public void sendOk() throws IOException {
        sendRaw("ok");
    }

    /**
     * Sendet einen Save-Befehl mit einer gespeicherten Spiel-ID.
     */
    public void sendSave(long id) throws IOException {
        sendRaw("save " + id);
    }

    /**
     * Sendet einen Load-Befehl für eine gespeicherte Spiel-ID.
     */
    public void sendLoad(long id) throws IOException {
        sendRaw("load " + id);
    }

    /**
     * Ermittelt verfügbare lokale IPv4-Adressen zur Anzeige im Verbindungsdialog.
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
     * Startet einen Server-Socket und wartet auf die Verbindung eines Clients.
     */
    public void connectAsServer(int port) throws IOException {
        // print local IPs to console for convenience
        System.out.print("My IP address(es):");
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        while (nis.hasMoreElements()) {
            NetworkInterface ni = nis.nextElement();
            Enumeration<InetAddress> ias = ni.getInetAddresses();
            while (ias.hasMoreElements()) {
                InetAddress ia = ias.nextElement();
                if (!ia.isLoopbackAddress()) {
                    System.out.print(" " + ia.getHostAddress());
                }
            }
        }
        System.out.println();

        try (ServerSocket ss = new ServerSocket(port)) {
            socket = ss.accept();
        }
        setupStreamsAndReader();
        if (listener != null)
            listener.onConnected();
    }

    /**
     * Verbindet als Client zu einem entfernten Host.
     */
    public void connectAsClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        setupStreamsAndReader();
        if (listener != null)
            listener.onConnected();
    }

    /**
     * Initialisiert die Ein- und Ausgabeströme und startet den Hintergrundleser.
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
     * Verarbeitet eine eingehende Protokollzeile und ruft entsprechende Listener-Methoden auf.
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
                        // store locally for compatibility with original Com
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
                        // original behavior: acknowledge immediately
                        try { sendOk(); } catch (IOException ex) { }
                    }
                    break;
                case "load":
                    if (parts.length > 1 && listener != null) {
                        long id = Long.parseLong(parts[1]);
                        listener.onLoad(id);
                        // original behavior: acknowledge immediately
                        try { sendOk(); } catch (IOException ex) { }
                    }
                    break;
                case "ok":
                    if (listener != null) {
                        listener.onOk();
                    }
                    // restore state on OK like original
                    if (currentState == State.WAITING_OK_SAVE && previousState != null) {
                        currentState = previousState;
                        previousState = null;
                    } else if (currentState == State.WAITING_OK_LOAD) {
                        currentState = State.MY_TURN;
                    }
                    break;
                default:
                    // ignore unknown
            }
        } catch (Exception e) {
            // ignore parse errors
        }
    }

    /**
     * Schließt die Netzwerkverbindung und den zugehörigen Socket.
     */
    public void close() {
        try {
            if (socket != null)
                socket.close();
        } catch (Exception e) {
        }
    }

    /**
     * Sendet eine Save-Anfrage und wechselt in den entsprechenden Wartezustand.
     */
    public void saveGame() throws IOException {
        previousState = currentState;
        long id = System.currentTimeMillis();
        sendSave(id);
        currentState = State.WAITING_OK_SAVE;
    }

    /**
     * Sendet einen Schuss, wenn der interne Zustand anzeigt, dass der Zug erlaubt ist.
     */
    public void fireShot(int row, int col) throws IOException {
        if (currentState != State.MY_TURN) {
            return;
        }
        sendShot(row, col);
        currentState = State.WAITING_ANSWER;
    }

    // getters/setters for compatibility / inspection
    public State getCurrentState() { return currentState; }
    public void setCurrentState(State s) { this.currentState = s; }
    public int getNumRows() { return numRows; }
    public int getNumCols() { return numCols; }
    public int[] getShipLengths() { return shipLengths; }
}
