package my.javacraft.linker.datamanager.service;

import java.util.Random;

public class SymbolGeneratorServices {

    private static final String ALL_AVAILABLE_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTYVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static char generateSymbol() {
        Random random = new Random();
        return ALL_AVAILABLE_SYMBOLS.charAt(random.nextInt(ALL_AVAILABLE_SYMBOLS.length()));
    }

    public static String generateShortText() {
        StringBuilder shortBuilder = new StringBuilder(6);
        for(int i = 0; i < 6; i++) {
            shortBuilder.append(SymbolGeneratorServices.generateSymbol());
        }
        return shortBuilder.toString();
    }

}
