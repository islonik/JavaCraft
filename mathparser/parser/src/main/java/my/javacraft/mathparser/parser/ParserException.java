package my.javacraft.mathparser.parser;

import java.util.Map;

/**
 * @author Lipatov Nikita
 * @see Exception
 **/
public class ParserException extends Exception {

    public static final int EXPRESSION_MAX_LENGTH = 1024;
    public static final int IDENTIFIER_MAX_LENGTH = 32;

    private static final Map<Error, String> errorMessages = Map.of(
            Error.SYNTAX, "Syntax error",
            Error.UNBAL_PARENTS, "Unbalanced brackets",
            Error.NO_EXPRESSION, "Expression wasn't found",
            Error.DIVISION_BY_ZERO, "Division by zero",
            Error.UNKNOWN_EXPRESSION, "Unknown expression",
            Error.UNKNOWN_FUNCTION, "Unknown function",
            Error.UNKNOWN_VARIABLE, "Unknown variable",
            Error.TOO_BIG, "Expression is too big (max '%s' characters)".formatted(EXPRESSION_MAX_LENGTH),
            Error.IDENTIFIER_TOO_LONG, "Identifier is too long (max '%s' characters)".formatted(IDENTIFIER_MAX_LENGTH),
            Error.NON_NEGATIVE_INTEGERS, "Factorial requires non-negative integers."
    );

    private final Error typeError;

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
        UNKNOWN_FUNCTION,
        UNKNOWN_VARIABLE,
        TOO_BIG,
        IDENTIFIER_TOO_LONG,
        NON_NEGATIVE_INTEGERS
    }

}
