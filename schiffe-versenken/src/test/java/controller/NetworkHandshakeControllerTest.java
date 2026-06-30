package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import models.ShipType;

/**
 * de: Testet die Klasse NetworkHandshakeController.
 * en: Tests the NetworkHandshakeController class.
 */
class NetworkHandshakeControllerTest {

    /**
     * de: Reagiert auf das Ereignis "TestGetShipLengthsFromShipTypeDefinitions".
     * en: Responds to the "TestGetShipLengthsFromShipTypeDefinitions" event.
     *
     * @throws Exception de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
    @Test
    void testGetShipLengthsFromShipTypeDefinitions() throws Exception {
        Method method = NetworkHandshakeController.class.getDeclaredMethod("getShipLengths");
        method.setAccessible(true);
        int[] lengths = (int[]) method.invoke(null);

        int expectedCount = 0;
        for (ShipType type : ShipType.values()) {
            expectedCount += type.getAmount();
        }

        assertEquals(expectedCount, lengths.length);
        assertEquals(5, lengths[0]);
    }

    /**
     * de: Reagiert auf das Ereignis "TestGetLocalIpTextReturnsFallbackForUnavailableIp".
     * en: Responds to the "TestGetLocalIpTextReturnsFallbackForUnavailableIp" event.
     *
     * @throws Exception de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
    @Test
    void testGetLocalIpTextReturnsFallbackForUnavailableIp() throws Exception {
        Method method = NetworkHandshakeController.class.getDeclaredMethod("getLocalIpText", String.class);
        method.setAccessible(true);
        String text = (String) method.invoke(null, "Host-IP: ");
        assertEquals("Host-IP: " + Com.getLocalIpAddresses(), text);
    }
}
