package my.javacraft.linker.datamanager.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SymbolGeneratorServicesTest {

    @Test
    public void testSpeedGeneration() {
        for (int i = 0; i < 100000; i++) {
            char symbol = SymbolGeneratorServices.generateSymbol();
            Assertions.assertNotNull(symbol);
        }
    }

}
