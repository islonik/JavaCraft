package my.javacraft.mathparser.parser;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Top-down parser.
 *
 * @author Lipatov Nikita
 * @version 1.0.0
 **/
public class Parser {
    public int typeTangentUnit;    // unit of angle
    private int idString;           // pointer in string
    private String storString;      // full string
    private String storToken;       // current token
    private Types typeToken;       // type of current token
    // Storage of variables
    private final ConcurrentMap<String, Double> storVars = new ConcurrentHashMap<>();

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
    public Parser(int unit) {
        typeTangentUnit = unit;
    }

    public int getTangentUnit() {
        return typeTangentUnit;
    }

    public void setTangentUnit(int unit) {
        typeTangentUnit = unit;
    }

    /**
     * Method should transform input string into sequences of tokens, which should be calculated and return in output string.
     *
     * @param expression expression for parsing
     * @return String result of top-down parser or error message
     **/
    public String calculate(String expression) {
        try {
            // remove all spaces
            expression = expression.replaceAll(" ", "");
            if (expression.length() > 1024) {
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
        } catch (ParserException exception) {
            return exception.toString();
        }
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
            if (!storVars.containsKey(token)) {
                storVars.put(token, 0.0);
            }
            getToken();
            if (!storToken.equals("=")) {
                putBack();
                if (!storVars.containsKey(token)) {
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
            } else if (token.equals("+")) {
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
        while ((token = storToken).equals("*") || token.equals("/") || token.equals("%")) {
            getToken();
            Number temp = new Number();
            fourthStepParsing(temp);
            if (token.equals("/")) {
                if (temp.get() == 0.0) {
                    throw new ParserException(ParserException.Error.DIVISION_BY_ZERO);
                }
                result.set(result.get() / temp.get());
            } else if (token.equals("%")) {
                if (temp.get() == 0.0) {
                    throw new ParserException(ParserException.Error.DIVISION_BY_ZERO);
                }
                result.set(result.get() % temp.get());
            } else if (token.equals("*")) {
                result.set(result.get() * temp.get());
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
        if ((typeToken == Types.DELIMITER) && storToken.equals("+") || storToken.equals("-")) {
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
            throw new ParserException(ParserException.Error.SYNTAX);
        }
        return Double.parseDouble(storVars.get(vname).toString());
    }

    /**
     * Метод определяет к скольки принимаемым параметрам относится функция.
     *
     * @param result result of top-down parser.
     * @throws ParserException error type of top-down parser.
     **/
    private void functions(Number result) throws ParserException {
        String str;
        if (isRegularExpression((str = storToken), "abs;acos;asin;atan;cos;log10;round;sin;sqrt;tan;")) {
            oneParameterFunctions(result, str);
        } else if (isRegularExpression((str = storToken), "pow;log;")) {
            twoParameterFunctions(result, str);
        } else if (isRegularExpression((str = storToken), "min;max;sum;avg;")) {
            multiParameterFunctions(result, str);
        } else {
            throw new ParserException(ParserException.Error.SYNTAX);
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
            case "log10" -> result.set(Math.log10(result.get()));
            case "round" -> result.set(Math.round(result.get()));
            case "sqrt" -> result.set(Math.sqrt(result.get()));
            case "acos" -> result.set(Math.acos(valueToMeasure(result.get())));
            case "asin" -> result.set(Math.asin(valueToMeasure(result.get())));
            case "atan" -> result.set(Math.atan(valueToMeasure(result.get())));
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
            case ParserType.DEGREE:
                yield result * Math.PI / 180;
            case ParserType.GRADUS:
                yield result * Math.PI / 200;
            default: // ParserType.RADIAN
                yield result;
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
                } else if (isRegularExpression(function, "avg;sum;")) { // sum
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
                strBuilder.append(storString.charAt(idString));
                idString++;
                if (idString >= storString.length()) {
                    break;
                }
                ctrl++;
                if (ctrl >= 32) {
                    throw new ParserException(ParserException.Error.UNKNOWN_EXPRESSION);
                }
            }
            if (idString < storString.length() && storString.charAt(idString) == '(') {
                typeToken = Types.FUNCTION;
            } else {
                typeToken = Types.VARIABLE;
            }
        } else if (Character.isDigit(storString.charAt(idString))) {
            while (!isDelimiter(storString.charAt(idString))) {
                strBuilder.append(storString.charAt(idString));
                idString++;
                if (idString >= storString.length()) {
                    break;
                }
            }
            typeToken = Types.NUMBER;
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
     * Compare two strings.
     * TODO: Should I rewrite it?.
     **/
    private boolean isRegularExpression(String str, String expression) {
        int idString = 0;
        StringBuilder strbuf = new StringBuilder(expression.length());
        while (expression.length() > idString) {
            // If we find the hiatus
            while (expression.charAt(idString) == ' ') {
                idString++;
            }
            // If the delimiter was found
            if (expression.charAt(idString) == ';') {
                // No delimiter
                if (str.contentEquals(strbuf)) {
                    return true;
                } else {
                    strbuf.delete(0, strbuf.length());
                }
            } else {
                strbuf.append(expression.charAt(idString));
            }
            idString++;
        }
        // No delimiter
        return str.contentEquals(strbuf);
    }

    /**
     * Types of tokens
     **/
    private enum Types {NONE, DELIMITER, VARIABLE, NUMBER, FUNCTION}
}

