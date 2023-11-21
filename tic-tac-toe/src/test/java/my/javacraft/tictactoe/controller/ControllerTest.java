package my.javacraft.tictactoe.controller;

import my.javacraft.tictactoe.model.GameSettings;
import my.javacraft.tictactoe.view.GUI;
import my.javacraft.tictactoe.view.Options;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * User: Lipatov Nikita
 */
public class ControllerTest {

    @Test
    public void testControllerSingleGame() {
        GUI gui = Mockito.mock(GUI.class);

        Controller controller = new Controller();
        controller.setView(gui);
        gui.setController(controller);

        Options options = mockOptions();

        // default values
        GameSettings.getInstance().setOddGame(false);
        GameSettings.getInstance().setComputer(true);
        GameSettings.getInstance().setFirstGamerMove(true);

        controller.newGame(options);
        Assertions.assertTrue(GameSettings.getInstance().isFirstGamerMove());
        controller.action(1); // first player
        controller.action(5); // second player
        controller.action(9); // first player
        controller.action(3); // second player
        controller.action(7); // first player
        controller.action(8); // second player
        controller.action(4); // first player

        Mockito.verify(gui).showWinner("Player 1");
    }

    @Test
    public void testControllerTwoGames() {
        GUI gui = Mockito.mock(GUI.class);

        Controller controller = new Controller();
        controller.setView(gui);
        gui.setController(controller);

        Options options = mockOptions();

        // default values
        GameSettings.getInstance().setOddGame(false);
        GameSettings.getInstance().setComputer(true);
        GameSettings.getInstance().setFirstGamerMove(true);

        controller.newGame(options);
        Assertions.assertTrue(GameSettings.getInstance().isFirstGamerMove());
        controller.action(4); // first player
        controller.action(1); // second player
        controller.action(2); // first player
        controller.action(5); // second player
        controller.action(6); // first player
        controller.action(9); // second player
        Mockito.verify(gui).showWinner("Player 2");

        controller.newGame(options);
        Assertions.assertFalse(GameSettings.getInstance().isFirstGamerMove());
        controller.action(5); // second player
        controller.action(7); // first player
        controller.action(1); // second player
        controller.action(9); // first player
        controller.action(8); // second player
        controller.action(3); // first player
        controller.action(4); // second player
        controller.action(6); // first player
        Mockito.verify(gui).showWinner("Player 1");
    }

    private Options mockOptions() {
        Options options = Mockito.mock(Options.class);
        Mockito.when(options.isSecondPlayerComputer()).thenReturn(false); /* reference attr group */
        Mockito.when(options.getNamePlayerOne()).thenReturn("Player 1"); /* reference attr group */
        Mockito.when(options.getNamePlayerTwo()).thenReturn("Player 2"); /* reference attr group */
        Mockito.when(options.getNameComputer()).thenReturn("Computer"); /* reference attr group */
        Mockito.when(options.isFirstMoveAlwaysPlayerOne()).thenReturn(false); /* reference attr group */
        Mockito.when(options.isFirstMoveAlwaysPlayerTwo()).thenReturn(false); /* reference attr group */
        return options;
    }
}
