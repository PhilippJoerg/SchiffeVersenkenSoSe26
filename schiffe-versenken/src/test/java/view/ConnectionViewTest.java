package view;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ConnectionViewTest {

    @Test
    void testAnonymousImplementationExists() {
        ConnectionView view = new ConnectionView() {
            @Override
            public void setConnectionStatus(String text) {
            }

            @Override
            public void setLocalIpAddress(String text) {
            }

            @Override
            public void setLoading(boolean loading) {
            }
        };

        assertNotNull(view);
    }
}
