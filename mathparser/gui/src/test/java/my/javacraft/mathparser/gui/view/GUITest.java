package my.javacraft.mathparser.gui.view;

import java.awt.event.*;
import my.javacraft.mathparser.parser.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * User: Lipatov Nikita
 */
public class GUITest {

    @Test
    public void testCase() {
        Parser mathParser = new Parser();
        GUI instance = new GUI();
        instance.setMathParser(mathParser);

        ActionEvent event1 = Mockito.mock(ActionEvent.class);
        Mockito.when(event1.getActionCommand()).thenReturn("1");
        instance.actionPerformed(event1);

        ActionEvent event2 = Mockito.mock(ActionEvent.class);
        Mockito.when(event2.getActionCommand()).thenReturn("0");
        instance.actionPerformed(event2);

        ActionEvent event3 = Mockito.mock(ActionEvent.class);
        Mockito.when(event3.getActionCommand()).thenReturn("+");
        instance.actionPerformed(event3);

        ActionEvent event4 = Mockito.mock(ActionEvent.class);
        Mockito.when(event4.getActionCommand()).thenReturn("9");
        instance.actionPerformed(event4);

        instance.calculateButton();

        Assertions.assertEquals("19.0", instance.getOutputText());
    }
}
