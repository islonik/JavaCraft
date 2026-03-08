package my.javacraft.mathparser.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Top-down parser.
 *
 * @author Lipatov Nikita
 * @version 1.0.0
 **/
public class Parser {
    private static final Set<String> ONE_PARAMETER_FUNCTIONS = Set.of(
            "abs", "acos", "asin", "atan", "cos", "ln", "log10", "round", "sin", "sqrt", "tan"
    );
    private static final Set<String> TWO_PARAMETER_FUNCTIONS = Set.of("pow", "log");
    private static final Set<String> MULTI_PARAMETER_FUNCTIONS = Set.of("min", "max", "sum", "avg");

    private ParserType typeTangentUnit;    // unit of angle
    private int idString;           // pointer in string
    private String storString;      // full string
    private String storToken;       // current token
    private Types typeToken;        // type of current token
    // Storage of variables
    private final Map<String, Double> storVars = new HashMap<>();

    {
        typeToken = Types.NONE;
        idString = 0;
        storToken = "";
        storString = "";
    }

    public Parser() {
        typeTangentUnit = ParserType.DEGREE;
    }

    /**
     * @param unit unit of angle
     **/
    public Parser(ParserType unit) {
        typeTangentUnit = Objects.requireNonNull(unit, "ParserType unit cannot be null");
    }

    public void setTangentUnit(ParserType unit) {
        typeTangentUnit = Objects.requireNonNull(unit, "ParserType unit cannot be null");
    }

    /**
     * Method should transform input string into sequences of tokens, which should be calculated and return in output string.
     *
     * @param expression expression for parsing
     * @return String result of top-down parser or error message
     **/
    public synchronized String calculate(String expression) {
        try {
            if (expression == null) {
                throw new ParserException(ParserException.Error.NO_EXPRESSION);
            }
            expression = normalizeExpression(expression);
            if (expression.length() > ParserException.EXPRESSION_MAX_LENGTH) {
                throw new ParserException(ParserException.Error.TOO_BIG);
            }
            storString = expression.toLowerCase();
            idString = 0;
            getToken();
            if (storToken.isEmpty()) {
                throw new ParserException(ParserException.Error.NO_EXPRESSION);
            }
            Number temp = new Number();
            firstStepParsing(temp);
            if (!storToken.isEmpty()) {
                throw new ParserException(ParserException.Error.SYNTAX);
            }
            return Double.toString(temp.get());
        } catch (NumberFormatException exception) {
            return new ParserException(ParserException.Error.SYNTAX).toString();
        } catch (ParserException exception) {
            return exception.toString();
        }
    }

    /**
     * Removes all whitespace-like characters so parser tokenization stays consistent for tabs/new lines/non-breaking spaces.
     */
    private String normalizeExpression(String expression) {
        StringBuilder normalized = new StringBuilder(expression.length());
        expression.codePoints()
                .filter(codePoint -> !Character.isWhitespace(codePoint) && !Character.isSpaceChar(codePoint))
                .forEach(normalized::appendCodePoint);
        return normalized.toString();
    }

    /**
     * Method searches tokens of variable initialization
     *
     * @param result result of top-down parser.
     * @throws ParserException error type of top-down parser.
     **/
    private void firstStepParsing(Number result) throws ParserException {
        String token;
        Types tempType;
        if (typeToken == Types.VARIABLE) {
            token = storToken;
            tempType = Types.VARIABLE;
            boolean hasTemporaryDefaultValue = false;
            if (!storVars.containsKey(token)) {
                storVars.put(token, 0.0);
                hasTemporaryDefaultValue = true;
            }
            getToken();
            if (!storToken.equals("=")) {
                putBack();
                if (hasTemporaryDefaultValue) {
                    storVars.remove(token);
                }
                storToken = token;
                typeToken = tempType;
            } else {
                getToken();
                secondStepParsing(result);
                storVars.put(token, result.get());
                return;
            }
        }
        secondStepParsing(result);
    }

    /**
     * Method returns pointer to the start position
     **/
    private void putBack() {
        for (int i = 0; i < storToken.length(); i++) {
            idString--;
        }
    }

    /**
     * Method searches tokens of plus or minus.
     *
     * @param result result of top-down parser.
     * @throws ParserException error type of top-down parser.
     **/
    private void secondStepParsing(Number result) throws ParserException {
        thirdStepParsing(result);
        String token;
        while ((token = storToken).equals("+") || token.equals("-")) {
            getToken();
            Number temp = new Number();
            thirdStepParsing(temp);
            if (token.equals("-")) {
                result.set(result.get() - temp.get());
            } else { // token.equals("+") - this condition is always true
                result.set(result.get() + temp.get());
            }
        }
    }

    /**
     * Method searches tokens of multiplication or divide.
     *
     * @param result result of top-down parser.
     * @throws ParserException error type of top-down parser.
     **/
    private void thirdStepParsing(Number result) throws ParserException {
        fourthStepParsing(result);
        String token;
        while ((token = storToken).equals("*")
                || token.equals("/")
                || token.equals("%")
                || isImplicitMultiplicationToken()) {
            boolean implicitMultiplication = isImplicitMultiplicationToken();
            if (!implicitMultiplication) {
                getToken();
            }
            Number temp = new Number();
            fourthStepParsing(temp);
            switch (token) {
                case "/" -> {
                    if (temp.get() == 0.0) {
                        throw new ParserException(ParserException.Error.DIVISION_BY_ZERO);
                    }
                    result.set(result.get() / temp.get());
                }
                case "%" -> {
                    if (temp.get() == 0.0) {
                        throw new ParserException(ParserException.Error.DIVISION_BY_ZERO);
                    }
                    result.set(result.get() % temp.get());
                }
                case "*" -> result.set(result.get() * temp.get());
                default -> result.set(result.get() * temp.get());
            }
        }
    }

    /**
     * Method searches tokens of involution (math).
     *
     * @param result result of top-down parser.
     * @throws ParserException error type of top-down parser.
     **/
    private void fourthStepParsing(Number result) throws ParserException {
        fifthStepParsing(result);
        if (storToken.equals("^")) {
            getToken();
            Number temp = new Number(0.0);
            fourthStepParsing(temp);
            result.set(Math.pow(result.get(), temp.get()));
        }
    }

    /**
     * Method searches tokens of unary symbols.
     *
     * @param result result of top-down parser.
     * @throws ParserException error type of top-down parser.
     **/
    private void fifthStepParsing(Number result) throws ParserException {
        String str = "";
        if ((typeToken == Types.DELIMITER) && (storToken.equals("+") || storToken.equals("-"))) {
            str = storToken;
            getToken();
        }
        sixthStepParsing(result);
        if (str.equals("-")) {
            result.invertValue();
        }
    }

    /**
     * Method searches tokens of brackets.
     *
     * @param result result of top-down parser.
     * @throws ParserException error type of top-down parser.
     **/
    private void sixthStepParsing(Number result) throws ParserException {
        if (storToken.equals("(")) {
            getToken();
            firstStepParsing(result);
            if (!storToken.equals(")")) {
                throw new ParserException(ParserException.Error.UNBAL_PARENTS);
            }
            getToken();
        } else {
            seventhStepParsing(result);
        }
    }

    /**
     * Method searches tokens of constants.
     *
     * @param result result of top-down parser.
     * @throws ParserException error type of top-down parser.
     **/
    private void seventhStepParsing(Number result) throws ParserException {
        if (storToken.equals("e")) {
            result.set(Math.E);
            getToken();
        } else if (storToken.equals("pi")) {
            result.set(Math.PI);
            getToken();
        } else {
            atom(result);
        }
    }

    /**
     * Method returns value.
     *
     * @param result result of top-down parser.
     * @throws ParserException error type of top-down parser.
     **/
    private void atom(Number result) throws ParserException {
        switch (typeToken) {
            case NUMBER:
                result.set(Double.parseDouble(storToken));
                getToken();
                return;
            case FUNCTION:
                functions(result);
                return;
            case VARIABLE:
                result.set(findVar(storToken));
                getToken();
                return;
            default:
                result.set(0.0);
                throw new ParserException(ParserException.Error.SYNTAX);
        }
    }

    /* Method finds variable and return value of it.
     * @param vname Variable name.
     * @throws ParserException error type of top-down parser.
     **/
    private double findVar(String vname) throws ParserException {
        if (!storVars.containsKey(vname)) {
            throw new ParserException(ParserException.Error.UNKNOWN_VARIABLE);
        }
        return storVars.get(vname);
    }

    /**
     * This method finds which function should be used.
     *
     * @param result result of top-down parser.
     * @throws ParserException error type of top-down parser.
     **/
    private void functions(Number result) throws ParserException {
        String function = storToken;
        if (ONE_PARAMETER_FUNCTIONS.contains(function)) {
            oneParameterFunctions(result, function);
        } else if (TWO_PARAMETER_FUNCTIONS.contains(function)) {
            twoParameterFunctions(result, function);
        } else if (MULTI_PARAMETER_FUNCTIONS.contains(function)) {
            multiParameterFunctions(result, function);
        } else {
            throw new ParserException(ParserException.Error.UNKNOWN_FUNCTION);
        }
    }

    /**
     * Method defines the function with one input value.
     *
     * @param result   result of top-down parser.
     * @param function one name of function.
     * @throws ParserException error type of top-down parser.
     **/
    private void oneParameterFunctions(Number result, String function) throws ParserException {
        getToken();
        sixthStepParsing(result);
        switch (function) {
            case "abs" -> result.set(Math.abs(result.get()));
            case "ln" -> result.set(Math.log(result.get()));
            case "log10" -> result.set(Math.log10(result.get()));
            case "round" -> result.set(Math.round(result.get()));
            case "sqrt" -> result.set(Math.sqrt(result.get()));
            case "acos" -> result.set(valueFromMeasure(Math.acos(result.get())));
            case "asin" -> result.set(valueFromMeasure(Math.asin(result.get())));
            case "atan" -> result.set(valueFromMeasure(Math.atan(result.get())));
            case "cos" -> result.set(Math.cos(valueToMeasure(result.get())));
            case "sin" -> result.set(Math.sin(valueToMeasure(result.get())));
            case "tan" -> result.set(Math.tan(valueToMeasure(result.get())));
        }
    }

    /**
     * Method returns converted values.
     *
     * @param result Basic value.
     * @return converted value.
     **/
    private double valueToMeasure(double result) {
        return switch (typeTangentUnit) {
            case DEGREE -> result * Math.PI / 180;
            case GRADUS -> result * Math.PI / 200;
            case RADIAN -> result;
        };
    }

    /**
     * Method converts radians to configured output unit.
     *
     * @param result Basic value in radians.
     * @return converted value in active angle unit.
     **/
    private double valueFromMeasure(double result) {
        return switch (typeTangentUnit) {
            case DEGREE -> result * 180 / Math.PI;
            case GRADUS -> result * 200 / Math.PI;
            case RADIAN -> result;
        };
    }

    /**
     * Method defines functions with two input values.
     *
     * @param result   result of top-down parser.
     * @param function one name of function.
     * @throws ParserException error type of top-down parser.
     **/
    private void twoParameterFunctions(Number result, String function) throws ParserException {
        getToken(); // bracket
        getToken(); // number or smth like it
        firstStepParsing(result);
        if (storToken.equals(",")) {
            getToken();
            Number temp = new Number();
            firstStepParsing(temp);
            if (function.equals("pow")) {
                result.set(Math.pow(result.get(), temp.get()));
            } else if (function.equals("log")) {
                result.set(Math.log(temp.get()) / Math.log(result.get()));
            }
            if (storToken.equals(",")) {
                throw new ParserException(ParserException.Error.SYNTAX);
            } else if (!storToken.equals(")")) {
                throw new ParserException(ParserException.Error.UNBAL_PARENTS);
            }
            getToken();
        } else {
            throw new ParserException(ParserException.Error.SYNTAX);
        }
    }

    /**
     * Method defines functions with multiply input values.
     *
     * @param result   result of top-down parser.
     * @param function one name of function.
     * @throws ParserException error type of top-down parser.
     **/
    private void multiParameterFunctions(Number result, String function) throws ParserException {
        getToken(); // bracket
        getToken(); // get result before delimiter
        firstStepParsing(result);
        int i = 1;
        for (; ; ) {
            if (storToken.equals(",")) {
                getToken();
                Number temp = new Number();
                firstStepParsing(temp);
                if (function.equals("min") && result.get() > temp.get()) { // min
                    result.set(temp.get());
                } else if (function.equals("max") && result.get() < temp.get()) { // max
                    result.set(temp.get());
                } else if (function.equals("avg") || function.equals("sum")) { // sum
                    result.set(result.get() + temp.get());
                    i++;
                }
            } else if (storToken.equals(")")) {
                if (function.equals("avg")) {
                    result.set(result.get() / i);
                }
                getToken();
                break;
            } else {
                throw new ParserException(ParserException.Error.UNBAL_PARENTS);
            }
        }
    }

    /**
     * Method returns the next token from the input string.
     *
     * @throws ParserException error type of top-down parser.
     **/
    private void getToken() throws ParserException {
        typeToken = Types.NONE;
        storToken = "";
        StringBuilder strBuilder = new StringBuilder(storString.length());
        if (idString == storString.length()) {
            return;
        }

        if (isDelimiter(storString.charAt(idString))) {
            strBuilder.append(storString.charAt(idString));
            idString++;
            typeToken = Types.DELIMITER;
        } else if (Character.isLetter(storString.charAt(idString))) { //isLetter??
            int ctrl = 0;
            while (!isDelimiter(storString.charAt(idString))) {
                if (!Character.isLetterOrDigit(storString.charAt(idString))) {
                    throw new ParserException(ParserException.Error.UNKNOWN_EXPRESSION);
                }
                strBuilder.append(storString.charAt(idString));
                idString++;
                if (idString >= storString.length()) {
                    break;
                }
                ctrl++;
                if (ctrl > ParserException.IDENTIFIER_MAX_LENGTH) {
                    throw new ParserException(ParserException.Error.IDENTIFIER_TOO_LONG);
                }
            }
            if (idString < storString.length() && storString.charAt(idString) == '(') {
                typeToken = Types.FUNCTION;
            } else {
                typeToken = Types.VARIABLE;
            }
        } else if (Character.isDigit(storString.charAt(idString))) {
            while (!isDelimiter(storString.charAt(idString))) {
                if (Character.isLetter(storString.charAt(idString))) {
                    // Stop numeric token before letters so expressions like 2pi parse as 2 * pi.
                    break;
                }
                if (!Character.isDigit(storString.charAt(idString)) && storString.charAt(idString) != '.') {
                    throw new ParserException(ParserException.Error.UNKNOWN_EXPRESSION);
                }
                strBuilder.append(storString.charAt(idString));
                idString++;
                if (idString >= storString.length()) {
                    break;
                }
            }
            typeToken = Types.NUMBER;
        } else {
            throw new ParserException(ParserException.Error.UNKNOWN_EXPRESSION);
        }
        storToken = strBuilder.toString();
    }

    /**
     * Method defines the delimiter.
     **/
    private boolean isDelimiter(char ctr) {
        return (" +-/\\*%^=(),".indexOf(ctr) != -1);
    }

    /**
     * Detects token adjacency that should be interpreted as multiplication.
     */
    private boolean isImplicitMultiplicationToken() {
        return storToken.equals("(")
                || typeToken == Types.FUNCTION
                || typeToken == Types.NUMBER
                || typeToken == Types.VARIABLE;
    }

    /**
     * Types of tokens
     **/
    private enum Types {NONE, DELIMITER, VARIABLE, NUMBER, FUNCTION}
}
