package my.javacraft.mathparser.gui;

import my.javacraft.mathparser.gui.view.GUI;
import my.javacraft.mathparser.parser.Parser;

/**
 * Entry point of MathParser.
 *
 * @author Lipatov Nikita
 **/
public class Main {
    /**
     * Main function.
     *
     * @param args It's not used.
     **/
    public static void main(String[] args) {
        Parser mathParser = new Parser();
        GUI instance = new GUI();
        instance.setMathParser(mathParser);
    }
}
