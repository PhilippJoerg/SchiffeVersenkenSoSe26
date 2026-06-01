/*
 * Datei: view/ConnectionView.java
 * Interface für Views, die Verbindungsstatus und lokale IP anzeigen können.
 */
package view;

public interface ConnectionView {
    void setConnectionStatus(String text);
    void setLocalIpAddress(String text);
    void setLoading(boolean loading);
}
