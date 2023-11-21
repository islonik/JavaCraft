package my.javacraft.tictactoe.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * User: Lipatov Nikita
 */
public class GameFieldTest {
    @Test
    public void testGameField_testCase01() {
        GameField gameField = new GameField();
        Assertions.assertNull(gameField.getGameField()[0][0]);
        Assertions.assertNull(gameField.getGameField()[0][1]);
        Assertions.assertNull(gameField.getGameField()[0][2]);
        Assertions.assertNull(gameField.getGameField()[1][0]);
        Assertions.assertNull(gameField.getGameField()[1][1]);
        Assertions.assertNull(gameField.getGameField()[1][2]);
        Assertions.assertNull(gameField.getGameField()[2][0]);
        Assertions.assertNull(gameField.getGameField()[2][1]);
        Assertions.assertNull(gameField.getGameField()[2][2]);
    }

    @Test
    public void testGameField_testCase02() {
        Player one = new Player();
        Player two = new Player();

        GameField gameField = new GameField();
        gameField.setPlayer(one, 1);
        Assertions.assertEquals(one, gameField.getGameField()[0][0]);

        gameField.setPlayer(two, 2);
        Assertions.assertEquals(two, gameField.getGameField()[0][1]);

        gameField.setPlayer(one, 3);
        Assertions.assertEquals(one, gameField.getGameField()[0][2]);

        gameField.setPlayer(two, 4);
        Assertions.assertEquals(two, gameField.getGameField()[1][0]);

        gameField.setPlayer(one, 5);
        Assertions.assertEquals(one, gameField.getGameField()[1][1]);

        gameField.setPlayer(two, 6);
        Assertions.assertEquals(two, gameField.getGameField()[1][2]);

        gameField.setPlayer(two, 7);
        Assertions.assertEquals(two, gameField.getGameField()[2][0]);

        gameField.setPlayer(one, 8);
        Assertions.assertEquals(one, gameField.getGameField()[2][1]);

        gameField.setPlayer(two, 9);
        Assertions.assertEquals(two, gameField.getGameField()[2][2]);
    }

    @Test
    public void testGameField_testCase03() {
        Player one = new Player();
        Player two = new Player();

        GameField gameField = new GameField();
        gameField.setPlayer(one, 1);
        Assertions.assertEquals(one, gameField.getPlayer(1));

        gameField.setPlayer(two, 2);
        Assertions.assertEquals(two, gameField.getPlayer(2));

        gameField.setPlayer(one, 3);
        Assertions.assertEquals(one, gameField.getPlayer(3));

        gameField.setPlayer(two, 4);
        Assertions.assertEquals(two, gameField.getPlayer(4));

        gameField.setPlayer(one, 5);
        Assertions.assertEquals(one, gameField.getPlayer(5));

        gameField.setPlayer(two, 6);
        Assertions.assertEquals(two, gameField.getPlayer(6));

        gameField.setPlayer(two, 7);
        Assertions.assertEquals(two, gameField.getPlayer(7));

        gameField.setPlayer(one, 8);
        Assertions.assertEquals(one, gameField.getPlayer(8));

        gameField.setPlayer(two, 9);
        Assertions.assertEquals(two, gameField.getPlayer(9));
    }

    @Test
    public void testGameField_testCase04() {
        // rows
        testGetWinner(1, 2, 3, 4, 5);
        testGetWinner(4, 5, 6, 1, 2);
        testGetWinner(7, 8, 9, 1, 2);

        // columns
        testGetWinner(1, 4, 7, 5, 9);
        testGetWinner(2, 5, 8, 4, 6);
        testGetWinner(3, 6, 9, 2, 8);

        // diagonals
        testGetWinner(1, 5, 9, 3, 7);
        testGetWinner(3, 5, 7, 1, 9);
    }

    public void testGetWinner(int c1, int c2, int c3, int c4, int c5) {
        Player one = new Player();
        Player two = new Player();

        GameField gameField = new GameField();
        gameField.setPlayer(one, c1);
        gameField.setPlayer(one, c2);
        gameField.setPlayer(one, c3);
        gameField.setPlayer(two, c4);
        gameField.setPlayer(two, c5);

        Assertions.assertEquals(one, gameField.getWinner());
    }
}
