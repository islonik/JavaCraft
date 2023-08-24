package my.javacraft.mathparser.parser;

import java.util.HashMap;

/**
 * @author Lipatov Nikita
 * @see Exception
 **/
public class ParserException extends Exception {

    private static final HashMap<Error, String> errorMessages = new HashMap<Error, String>();
    private final Error typeError;

    {
        errorMessages.put(Error.SYNTAX, "Syntax error");
        errorMessages.put(Error.UNBAL_PARENTS, "Unbalanced brackets");
        errorMessages.put(Error.NO_EXPRESSION, "Expression wasn't found");
        errorMessages.put(Error.DIVISION_BY_ZERO, "Division by zero");
        errorMessages.put(Error.UNKNOWN_EXPRESSION, "Unknown expression");
        errorMessages.put(Error.UNKNOWN_VARIABLE, "Unknown variable");
        errorMessages.put(Error.TOO_BIG, "Expression is too big");
    }

    public ParserException(Error typeError) {
        this.typeError = typeError;
    }

    @Override
    public String toString() {
        return errorMessages.get(typeError);
    }

    public enum Error {
        SYNTAX,
        UNBAL_PARENTS,
        NO_EXPRESSION,
        DIVISION_BY_ZERO,
        UNKNOWN_EXPRESSION,
        UNKNOWN_VARIABLE,
        TOO_BIG
    }

}
