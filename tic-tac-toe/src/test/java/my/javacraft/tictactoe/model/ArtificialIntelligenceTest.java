package my.javacraft.tictactoe.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * User: Lipatov Nikita
 */
public class ArtificialIntelligenceTest
{
    private Player gamer    = new Player("Player 1", false);
    private Player computer = new Player("Computer", true);

    private GameField createTestGameField(int c1, int c2, int c3, int c4)
    {
        GameField gameField = new GameField();
        gameField.setPlayer(gamer, c1);
        gameField.setPlayer(gamer, c2);
        gameField.setPlayer(computer, c3);
        gameField.setPlayer(computer, c4);

        return gameField;
    }

    private GameField createTestGameField(int c1, int c2, int c3)
    {
        GameField gameField = new GameField();
        gameField.setPlayer(gamer, c1);
        gameField.setPlayer(gamer, c2);
        gameField.setPlayer(computer, c3);

        return gameField;
    }

    @Test
    public void testArtificialIntelligence_firstMove()
    {
        ArtificialIntelligence ai = new ArtificialIntelligence(gamer, computer, new GameField());
        ai.computerBrain();
        Assertions.assertNotSame(0, ai.getNextMove());
    }

    @Test
    public void testArtificialIntelligence_firstMoveNotInTheCentre()
    {
        GameField gameField = new GameField();
        gameField.setPlayer(gamer, 5);
        ArtificialIntelligence ai = new ArtificialIntelligence(gamer, computer, gameField);
        ai.computerBrain();
        Assertions.assertNotSame(0, ai.getNextMove());
    }

    @Test
    public void testArtificialIntelligence_horizontal_testCase01()
    {
        ArtificialIntelligence ai = new ArtificialIntelligence(gamer, computer, createTestGameField(1, 2, 4, 5));
        ai.makeMove(this.computer);
        Assertions.assertEquals(6, ai.getNextMove());
    }

    @Test
    public void testArtificialIntelligence_horizontal_testCase02()
    {
        ArtificialIntelligence ai = new ArtificialIntelligence(gamer, computer, createTestGameField(4, 5, 8, 9));
        ai.makeMove(this.computer);
        Assertions.assertEquals(7, ai.getNextMove());
    }

    @Test
    public void testArtificialIntelligence_vertical_testCase01()
    {
        ArtificialIntelligence ai = new ArtificialIntelligence(gamer, computer, createTestGameField(1, 4, 2, 5));
        ai.makeMove(this.computer);
        Assertions.assertEquals(8, ai.getNextMove());
    }

    @Test
    public void testArtificialIntelligence_vertical_testCase02()
    {
        ArtificialIntelligence ai = new ArtificialIntelligence(gamer, computer, createTestGameField(4, 7, 3, 6));
        ai.makeMove(this.computer);
        Assertions.assertEquals(9, ai.getNextMove());
    }

    @Test
    public void testArtificialIntelligence_diagonal_attack_testCase01()
    {
        ArtificialIntelligence ai = new ArtificialIntelligence(gamer, computer, createTestGameField(3, 7, 1, 9));
        ai.makeMove(this.computer);
        Assertions.assertEquals(5, ai.getNextMove());
    }

    @Test
    public void testArtificialIntelligence_diagonal_attack_testCase02()
    {
        ArtificialIntelligence ai = new ArtificialIntelligence(gamer, computer, createTestGameField(2, 8, 3, 5));
        ai.makeMove(this.computer);
        Assertions.assertEquals(7, ai.getNextMove());
    }

    @Test
    public void testArtificialIntelligence_diagonal_defense_testCase01()
    {
        ArtificialIntelligence ai = new ArtificialIntelligence(gamer, computer, createTestGameField(1, 5, 8));
        ai.computerBrain();
        Assertions.assertEquals(9, ai.getNextMove());
    }

    @Test
    public void testArtificialIntelligence_diagonal_defense_testCase02()
    {
        ArtificialIntelligence ai = new ArtificialIntelligence(gamer, computer, createTestGameField(3, 7, 2));
        ai.computerBrain();
        Assertions.assertEquals(5, ai.getNextMove());
    }




}
