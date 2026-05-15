package dev.nklip.javacraft.xlspaceship.engine.game.ships;

/**
 * Named rotation/orientation values for ship layouts.
 *
 * <p>This replaces the old raw {@code 1..4} form ids with self-documenting
 * directions that make constructor call sites easier to read and harder to mix
 * up.
 */
public enum ShipOrientation {
    NORTH,
    EAST,
    SOUTH,
    WEST
}
