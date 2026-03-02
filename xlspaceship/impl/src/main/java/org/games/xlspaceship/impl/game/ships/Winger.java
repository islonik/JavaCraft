package org.games.xlspaceship.impl.game.ships;

import org.games.xlspaceship.impl.game.Cell;

public class Winger extends Spaceship {

    private static final int WIDTH = 3;
    private static final int HEIGHT = 5;

    public Winger(int form) {
        super(form, WIDTH, HEIGHT);
    }

    protected Cell[][] formA() {
        ship[0] = string2cells("*.*");
        ship[1] = string2cells("*.*");
        ship[2] = string2cells(".*.");
        ship[3] = string2cells("*.*");
        ship[4] = string2cells("*.*");
        return ship;
    }

    protected Cell[][] formB() {
        ship[0] = string2cells("**.**");
        ship[1] = string2cells("..*..");
        ship[2] = string2cells("**.**");
        return ship;
    }

    protected Cell[][] formC() {
        return formA();
    }

    protected Cell[][] formD() {
        return formB();
    }
}
