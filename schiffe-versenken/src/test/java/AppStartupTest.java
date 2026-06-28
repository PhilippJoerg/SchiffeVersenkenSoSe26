import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * de: Testet die Klasse AppStartup.
 * en: Tests the AppStartup class.
 */
class AppStartupTest {

    /**
     * de: Testet, ob die main-Methode existiert.
     * en: Tests if the main method exists.
     *
     * @throws Exception de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
    @Test
    void testMainMethodExists() throws Exception {
        assertNotNull(AppStartup.class.getMethod("main", String[].class));
    }
}
