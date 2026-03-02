package org.games.xlspaceship.impl.game.ships;

import org.games.xlspaceship.impl.game.Cell;

public class Angle extends Spaceship {

    private static final int WIDTH = 3;
    private static final int HEIGHT = 4;

    public Angle(int form) {
        super(form, WIDTH, HEIGHT);
    }

    protected Cell[][] formA() {
        ship[0] = string2cells("*..");
        ship[1] = string2cells("*..");
        ship[2] = string2cells("*..");
        ship[3] = string2cells("***");
        return ship;
    }

    protected Cell[][] formB() {
        ship[0] = string2cells("...*");
        ship[1] = string2cells("...*");
        ship[2] = string2cells("****");
        return ship;
    }

    protected Cell[][] formC() {
        ship[0] = string2cells("***");
        ship[1] = string2cells("..*");
        ship[2] = string2cells("..*");
        ship[3] = string2cells("..*");
        return ship;
    }

    protected Cell[][] formD() {
        ship[0] = string2cells("****");
        ship[1] = string2cells("*...");
        ship[2] = string2cells("*...");
        return ship;
    }

}
