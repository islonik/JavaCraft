package dev.nklip.javacraft.xlspaceship.engine.service;

import dev.nklip.javacraft.xlspaceship.engine.game.ships.ShipOrientation;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class RandomProvider {

    private static final Random greatRandom = new Random(); // should be thread safe
    private static final ShipOrientation[] ORIENTATIONS = ShipOrientation.values();

    public int generateAI() {
        return greatRandom.nextInt(2000) + 1;
    }

    public ShipOrientation generateOrientation() {
        return ORIENTATIONS[greatRandom.nextInt(ORIENTATIONS.length)];
    }

    public int generatePlayer() {
        return greatRandom.nextInt(2) + 1;
    }

    public int generateCell(int size) {
        return greatRandom.nextInt(size);
    }

    public int generateUp10() {
        return greatRandom.nextInt(10) + 1;
    }

    public int generateUp16() {
        return greatRandom.nextInt(16);
    }


}
