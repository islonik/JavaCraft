package my.javacraft.mathparser.gui.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HelpMessageLoaderTest {

    @Test
    void testLoadHelpMessage() {
        String message = HelpMessageLoader.loadHelpMessage();

        Assertions.assertNotNull(message);
        Assertions.assertFalse(message.isBlank());
        Assertions.assertTrue(message.contains("Functions with one variable:"));
        Assertions.assertTrue(message.contains("Functions with two variables:"));
        Assertions.assertTrue(message.contains("Functions with many variables:"));
    }
}
