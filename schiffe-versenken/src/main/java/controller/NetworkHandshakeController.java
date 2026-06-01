package controller;

/*
 * Datei: controller/NetworkHandshakeController.java
 * Verantwortlich für den Verbindungsaufbau und das Handshake-Protokoll zwischen Host und Client.
 * Nutzt `Com` um Nachrichten auszutauschen und ruft den Callback auf, sobald der Handshake abgeschlossen ist.
 */
import javax.swing.SwingUtilities;
import models.ShipType;
import view.ConnectionView;

public class NetworkHandshakeController {

    public interface ReadyCallback {
        void onReady(Com com, boolean iStart);
        void onError(String message, Exception e);
    }

    /**
     * Startet einen Host, akzeptiert einen Client und führt den Handshake aus.
     */
    public static void startHost(ConnectionView frame, int port, ReadyCallback callback) {
        frame.setConnectionStatus("Host gestartet — warte auf Client-Verbindung...");
        frame.setLocalIpAddress(getLocalIpText("Host-IP: "));
        frame.setLoading(true);

        final int[] handshakeStage = new int[] {0};
        final boolean[] iStart = new boolean[] {false};
        final Com[] comRef = new Com[1];

        Com com = new Com(new Com.Listener() {
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
     * Verbindet als Client zu einem Host und führt den Handshake aus.
     */
    public static void startClient(ConnectionView frame, String host, int port, ReadyCallback callback) {
        frame.setConnectionStatus("Verbinde mit Host " + host + "...");
        frame.setLocalIpAddress(getLocalIpText("Deine IP: "));

        final boolean[] gotCoin = new boolean[] {false};
        final boolean[] iStart = new boolean[] {false};
        final Com[] comRef = new Com[1];

        Com com = new Com(new Com.Listener() {
            @Override
            public void onConnected() {
                SwingUtilities.invokeLater(() -> frame.setConnectionStatus("Verbunden mit Host " + host + ". Warte auf Handshake..."));
            }

            @Override
            public void onCoin(int coin) {
                iStart[0] = coin == 0;
                gotCoin[0] = true;
            }

            @Override
            public void onSize(int rows, int cols) {
                try {
                    comRef[0].sendDone();
                } catch (Exception e) {
                    notifyError(frame, "Fehler im Handshake", e, callback);
                }
            }

            @Override
            public void onShips(int[] lengths) {
                try {
                    comRef[0].sendDone();
                } catch (Exception e) {
                    notifyError(frame, "Fehler im Handshake", e, callback);
                }
            }

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
     * Benachrichtigt den Callback bei einem Handshake-Fehler im Event-Thread.
     */
    private static void notifyError(ConnectionView frame, String message, Exception e, ReadyCallback callback) {
        SwingUtilities.invokeLater(() -> {
            frame.setLoading(false);
            callback.onError(message, e);
        });
    }

    /**
     * Erzeugt die Schiffslängen für den Handshake aus der Schiffstyp-Definition.
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
     * Formatiert die lokale IP-Adresse für die Anzeige im Verbindungsdialog.
     */
    private static String getLocalIpText(String prefix) {
        String localIp = Com.getLocalIpAddresses();
        return localIp.isEmpty() ? "Lokale IP nicht verfügbar" : prefix + localIp;
    }
}
