/*
 * Datei: view/ConnectionView.java
 * Interface für Views, die Verbindungsstatus und lokale IP anzeigen können.
 */
package view;

/**
 * de: Schnittstelle für Views, die Verbindungsstatus und lokale IP anzeigen können.
 * en: Interface for views that can display connection status and local IP.
 */
public interface ConnectionView {
    /**
     * de: Setzt den Verbindungsstatus.
     * en: Sets the connection status.
     *
     * @param text de: Der Verbindungsstatus. en: The connection status.
     */
    void setConnectionStatus(String text);
    /**
     * de: Setzt die lokale IP-Adresse.
     * en: Sets the local IP address.
     *
     * @param text de: Die lokale IP-Adresse. en: The local IP address.
     */
    void setLocalIpAddress(String text);
    /**
     * de: Setzt den Ladezustand.
     * en: Sets the loading state.
     *
     * @param loading de: Der Ladezustand. en: The loading state.
     */
    void setLoading(boolean loading);
}
