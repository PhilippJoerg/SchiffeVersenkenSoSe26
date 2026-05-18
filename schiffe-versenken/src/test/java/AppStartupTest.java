import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AppStartupTest {

    @Test
    void testMainMethodExists() throws Exception {
        assertNotNull(AppStartup.class.getMethod("main", String[].class));
    }
}
