package controller;

/*
 * Datei: controller/NetworkHandshakeController.java
 * Verantwortlich für den Verbindungsaufbau und das Handshake-Protokoll zwischen Host und Client.
 * Nutzt `Com` um Nachrichten auszutauschen und ruft den Callback auf, sobald der Handshake abgeschlossen ist.
 */
import javax.swing.SwingUtilities;
import models.ShipType;
import view.ConnectionView;

/**
 * de: Die Klasse NetworkHandshakeController.
 * en: The class NetworkHandshakeController.
 */
public class NetworkHandshakeController {

    /**
     * de: Die Schnittstelle ReadyCallback.
     * en: The interface ReadyCallback.
     */
    public interface ReadyCallback {
        /**
         * de: Die Methode onReady.
         * en: The method onReady.
         *
         * @param com de: Parameter com. en: Parameter com.
         * @param iStart de: Parameter iStart. en: Parameter iStart.
         */
        void onReady(Com com, boolean iStart);
        /**
         * de: Die Methode onError.
         * en: The method onError.
         *
         * @param message de: Parameter message. en: Parameter message.
         * @param e de: Parameter e. en: Parameter e.
         */
        void onError(String message, Exception e);
    }

    /**
     * de: Startet einen Host, akzeptiert einen Client und führt den Handshake aus.
     * en: Starts a host, accepts a client, and performs the handshake.
     *
     * @param frame de: Parameter frame. en: Parameter frame.
     * @param port de: Parameter port. en: Parameter port.
     * @param callback de: Parameter callback. en: Parameter callback.
     */
    public static void startHost(ConnectionView frame, int port, ReadyCallback callback) {
        frame.setConnectionStatus("Host gestartet — warte auf Client-Verbindung...");
        frame.setLocalIpAddress(getLocalIpText("Host-IP: "));
        frame.setLoading(true);

        final int[] handshakeStage = new int[] {0};
        final boolean[] iStart = new boolean[] {false};
        final Com[] comRef = new Com[1];

        Com com = new Com(new Com.Listener() {
            /**
             * de: Überschreibt die Methode onConnected.
             * en: Overrides the method onConnected.
             *
             */
            @Override
            public void onConnected() {
                try {
                    Com c = comRef[0];
                    int coin = new java.util.Random().nextBoolean() ? 1 : 0;
                    c.sendCoin(coin);
                    iStart[0] = coin == 1;
                    c.sendSize(10);
                    handshakeStage[0] = 1;
                    SwingUtilities.invokeLater(() -> frame.setConnectionStatus("Client verbunden. Protokoll wird ausgehandelt..."));
                } catch (Exception e) {
                    notifyError(frame, "Fehler beim Starten des Hosts", e, callback);
                }
            }

            /**
             * de: Überschreibt die Methode onDone.
             * en: Overrides the method onDone.
             *
             */
            @Override
            public void onDone() {
                try {
                    Com c = comRef[0];
                    if (handshakeStage[0] == 1) {
                        c.sendShips(getShipLengths());
                        handshakeStage[0] = 2;
                    } else if (handshakeStage[0] == 2) {
                        c.sendReady();
                        handshakeStage[0] = 3;
                    }
                } catch (Exception e) {
                    notifyError(frame, "Fehler im Handshake", e, callback);
                }
            }

            /**
             * de: Überschreibt die Methode onReady.
             * en: Overrides the method onReady.
             *
             */
            @Override
            public void onReady() {
                if (handshakeStage[0] == 3) {
                    SwingUtilities.invokeLater(() -> {
                        frame.setLoading(false);
                        frame.setConnectionStatus("Handschlag abgeschlossen. Platziere deine Schiffe.");
                        callback.onReady(comRef[0], iStart[0]);
                    });
                }
            }

            /**
             * de: Überschreibt die Methode onDisconnected.
             * en: Overrides the method onDisconnected.
             *
             */
            @Override
            public void onDisconnected() {
                SwingUtilities.invokeLater(() -> {
                    frame.setLoading(false);
                    frame.setConnectionStatus("Client getrennt.");
                });
            }
        });
        comRef[0] = com;

        new Thread(() -> {
            try {
                com.connectAsServer(port);
            } catch (Exception e) {
                notifyError(frame, "Fehler beim Starten des Hosts", e, callback);
            }
        }, "Net-Host").start();
    }

    /**
     * de: Verbindet als Client zu einem Host und führt den Handshake aus.
     * en: Connects to a host as a client and performs the handshake.
     */
    public static void startClient(ConnectionView frame, String host, int port, ReadyCallback callback) {
        frame.setConnectionStatus("Verbinde mit Host " + host + "...");
        frame.setLocalIpAddress(getLocalIpText("Deine IP: "));

        final boolean[] gotCoin = new boolean[] {false};
        final boolean[] iStart = new boolean[] {false};
        final Com[] comRef = new Com[1];

        Com com = new Com(new Com.Listener() {
            /**
             * de: Überschreibt die Methode onConnected.
             * en: Overrides the method onConnected.
             *
             */
            @Override
            public void onConnected() {
                SwingUtilities.invokeLater(() -> frame.setConnectionStatus("Verbunden mit Host " + host + ". Warte auf Handshake..."));
            }

            /**
             * de: Überschreibt die Methode onCoin.
             * en: Overrides the method onCoin.
             *
             * @param coin de: Parameter coin. en: Parameter coin.
             */
            @Override
            public void onCoin(int coin) {
                iStart[0] = coin == 0;
                gotCoin[0] = true;
            }

            /**
             * de: Überschreibt die Methode onSize.
             * en: Overrides the method onSize.
             *
             * @param rows de: Parameter rows. en: Parameter rows.
             * @param cols de: Parameter cols. en: Parameter cols.
             */
            @Override
            public void onSize(int rows, int cols) {
                try {
                    comRef[0].sendDone();
                } catch (Exception e) {
                    notifyError(frame, "Fehler im Handshake", e, callback);
                }
            }

            /**
             * de: Überschreibt die Methode onShips.
             * en: Overrides the method onShips.
             *
             * @param lengths de: Parameter lengths. en: Parameter lengths.
             */
            @Override
            public void onShips(int[] lengths) {
                try {
                    comRef[0].sendDone();
                } catch (Exception e) {
                    notifyError(frame, "Fehler im Handshake", e, callback);
                }
            }

            /**
             * de: Überschreibt die Methode onReady.
             * en: Overrides the method onReady.
             *
             */
            @Override
            public void onReady() {
                if (!gotCoin[0]) {
                    return;
                }
                try {
                    comRef[0].sendReady();
                } catch (Exception e) {
                    notifyError(frame, "Fehler im Handshake", e, callback);
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    frame.setConnectionStatus("Handschlag abgeschlossen. Platziere deine Schiffe.");
                    callback.onReady(comRef[0], iStart[0]);
                });
            }

            /**
             * de: Überschreibt die Methode onDisconnected.
             * en: Overrides the method onDisconnected.
             *
             */
            @Override
            public void onDisconnected() {
                SwingUtilities.invokeLater(() -> frame.setConnectionStatus("Verbindung zum Host verloren."));
            }
        });
        comRef[0] = com;

        new Thread(() -> {
            try {
                com.connectAsClient(host, port);
            } catch (Exception e) {
                notifyError(frame, "Fehler beim Verbinden", e, callback);
            }
        }, "Net-Client").start();
    }

    /**
     * de: Benachrichtigt den Callback bei einem Handshake-Fehler im Event-Thread.
     * en: Notifies the callback of a handshake error in the event thread.
     */
    private static void notifyError(ConnectionView frame, String message, Exception e, ReadyCallback callback) {
        SwingUtilities.invokeLater(() -> {
            frame.setLoading(false);
            callback.onError(message, e);
        });
    }

    /**
     * de: Erzeugt die Schiffslängen für den Handshake aus der Schiffstyp-Definition.
     * en: Generates the ship lengths for the handshake from the ship type definition.
     */
    private static int[] getShipLengths() {
        java.util.List<Integer> lengths = new java.util.ArrayList<>();
        for (ShipType type : ShipType.values()) {
            for (int i = 0; i < type.getAmount(); i++) {
                lengths.add(type.getSize());
            }
        }
        return lengths.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * de: Formatiert die lokale IP-Adresse für die Anzeige im Verbindungsdialog.
     * en: Formats the local IP address for display in the connection dialog.
     */
    private static String getLocalIpText(String prefix) {
        String localIp = Com.getLocalIpAddresses();
        return localIp.isEmpty() ? "Lokale IP nicht verfügbar" : prefix + localIp;
    }
}
