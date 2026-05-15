package dev.nklip.javacraft.xlspaceship.engine.game.ships;

import java.util.List;
import java.util.Map;

public final class Winger extends Spaceship {

    private static final List<String> NORTH_SOUTH_SHAPE = List.of(
            "*.*",
            "*.*",
            ".*.",
            "*.*",
            "*.*"
    );
    private static final List<String> EAST_WEST_SHAPE = List.of(
            "**.**",
            "..*..",
            "**.**"
    );
    private static final Map<ShipOrientation, List<String>> SHAPE_TEMPLATES = Map.of(
            ShipOrientation.NORTH, NORTH_SOUTH_SHAPE,
            ShipOrientation.EAST, EAST_WEST_SHAPE,
            ShipOrientation.SOUTH, NORTH_SOUTH_SHAPE,
            ShipOrientation.WEST, EAST_WEST_SHAPE
    );

    public Winger(ShipOrientation orientation) {
        super(orientation, SHAPE_TEMPLATES);
    }
}
