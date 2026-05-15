package dev.nklip.javacraft.xlspaceship.engine.game.ships;

import dev.nklip.javacraft.xlspaceship.engine.game.Cell;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;

/**
 * Closed ship hierarchy for the built-in XL-Spaceship fleet.
 *
 * <p>Each concrete ship supplies one immutable shape template per orientation,
 * and the selected {@link ShipOrientation} determines the active width, height,
 * and cell layout.
 */
public abstract sealed class Spaceship permits AClass, Angle, BClass, SClass, Winger {

    @Getter
    private final ShipOrientation orientation;
    @Getter
    private final int width;
    @Getter
    private final int height;

    private final Map<ShipOrientation, List<String>> shapeTemplates;
    private final List<Cell> cells = new ArrayList<>();

    protected Spaceship(ShipOrientation orientation, Map<ShipOrientation, List<String>> shapeTemplates) {
        this.orientation = Objects.requireNonNull(orientation, "Ship orientation must not be null");
        this.shapeTemplates = copyTemplates(shapeTemplates);

        List<String> activeRows = this.shapeTemplates.get(orientation);
        this.height = activeRows.size();
        this.width = activeRows.getFirst().length();
    }

    public void addCell(Cell cell) {
        if ("*".equals(cell.getValue())) {
            cells.add(cell);
        }
    }

    public void clearCells() {
        cells.clear();
    }

    public int getHealth() {
        int health = 0;
        for (Cell cell : cells) {
            if ("*".equals(cell.getValue())) {
                health++;
            }
        }
        return health;
    }

    public List<Cell> shape() {
        List<String> activeRows = shapeTemplates.get(orientation);
        List<Cell> cellList = new ArrayList<>(width * height);
        for (String row : activeRows) {
            cellList.addAll(Arrays.asList(toCells(row)));
        }
        return cellList;
    }

    protected static Cell[] toCells(String value) {
        Cell[] cells = new Cell[value.length()];
        for (int i = 0; i < value.length(); i++) {
            cells[i] = new Cell(Character.toString(value.charAt(i)));
        }
        return cells;
    }

    private static Map<ShipOrientation, List<String>> copyTemplates(Map<ShipOrientation, List<String>> shapeTemplates) {
        Objects.requireNonNull(shapeTemplates, "Ship shape templates must not be null");

        java.util.EnumMap<ShipOrientation, List<String>> copiedTemplates = new java.util.EnumMap<>(ShipOrientation.class);
        for (ShipOrientation orientation : ShipOrientation.values()) {
            List<String> rows = shapeTemplates.get(orientation);
            if (rows == null || rows.isEmpty()) {
                throw new IllegalArgumentException("Missing rows for ship orientation " + orientation);
            }
            validateRows(orientation, rows);
            copiedTemplates.put(orientation, List.copyOf(rows));
        }
        return Map.copyOf(copiedTemplates);
    }

    private static void validateRows(ShipOrientation orientation, List<String> rows) {
        int expectedWidth = rows.getFirst().length();
        for (String row : rows) {
            if (row.length() != expectedWidth) {
                throw new IllegalArgumentException(
                        "Inconsistent row width for orientation " + orientation + ": " + rows
                );
            }
        }
    }
}
