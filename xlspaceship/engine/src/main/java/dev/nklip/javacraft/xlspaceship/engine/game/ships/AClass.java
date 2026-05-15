package dev.nklip.javacraft.xlspaceship.engine.game.ships;

import java.util.List;
import java.util.Map;

public final class AClass extends Spaceship {

    private static final Map<ShipOrientation, List<String>> SHAPE_TEMPLATES = Map.of(
            ShipOrientation.NORTH, List.of(
                    ".*.",
                    "*.*",
                    "***",
                    "*.*"
            ),
            ShipOrientation.EAST, List.of(
                    ".***",
                    "*.*.",
                    ".***"
            ),
            ShipOrientation.SOUTH, List.of(
                    "*.*",
                    "***",
                    "*.*",
                    ".*."
            ),
            ShipOrientation.WEST, List.of(
                    "***.",
                    ".*.*",
                    "***."
            )
    );

    public AClass(ShipOrientation orientation) {
        super(orientation, SHAPE_TEMPLATES);
    }
}
