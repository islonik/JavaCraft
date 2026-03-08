package my.javacraft.mathparser.gui.view;

import java.awt.GraphicsEnvironment;
import java.awt.event.*;
import my.javacraft.mathparser.parser.Parser;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * User: Lipatov Nikita
 */
public class GUITest {

    @Test
    public void testCase() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Requires graphics environment");
        Parser mathParser = new Parser();
        GUI instance = new GUI();
        try {
            instance.setMathParser(mathParser);

            ActionEvent event1 = new ActionEvent(instance, ActionEvent.ACTION_PERFORMED, "1");
            instance.actionPerformed(event1);

            ActionEvent event2 = new ActionEvent(instance, ActionEvent.ACTION_PERFORMED, "0");
            instance.actionPerformed(event2);

            ActionEvent event3 = new ActionEvent(instance, ActionEvent.ACTION_PERFORMED, "+");
            instance.actionPerformed(event3);

            ActionEvent event4 = new ActionEvent(instance, ActionEvent.ACTION_PERFORMED, "9");
            instance.actionPerformed(event4);

            instance.calculateButton();

            Assertions.assertEquals("19.0", instance.getOutputText());
        } finally {
            instance.dispose();
        }
    }
}
