package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import models.ShipType;

class NetworkHandshakeControllerTest {

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

    @Test
    void testGetLocalIpTextReturnsFallbackForUnavailableIp() throws Exception {
        Method method = NetworkHandshakeController.class.getDeclaredMethod("getLocalIpText", String.class);
        method.setAccessible(true);
        String text = (String) method.invoke(null, "Host-IP: ");
        assertEquals("Host-IP: " + Com.getLocalIpAddresses(), text);
    }
}
