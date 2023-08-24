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
            Number _temp = new Number();
            firstStepParsing(_temp);
            if (!storToken.isEmpty()) {
                throw new ParserException(ParserException.Error.SYNTAX);
            }
            return Double.toString(_temp.get());
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
        String _token;
        Types _tempType;
        if (typeToken == Types.VARIABLE) {
            _token = storToken;
            _tempType = Types.VARIABLE;
            if (!storVars.containsKey(_token)) {
                storVars.put(_token, 0.0);
            }
            getToken();
            if (!storToken.equals("=")) {
                putBack();
                if (!storVars.containsKey(_token)) {
                    storVars.remove(_token);
                }
                storToken = _token;
                typeToken = _tempType;
            } else {
                getToken();
                secondStepParsing(result);
                storVars.put(_token, result.get());
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
        String _token;
        while ((_token = storToken).equals("+") || _token.equals("-")) {
            getToken();
            Number _temp = new Number();
            thirdStepParsing(_temp);
            if (_token.equals("-")) {
                result.set(result.get() - _temp.get());
            } else if (_token.equals("+")) {
                result.set(result.get() + _temp.get());
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
        String _token;
        while ((_token = storToken).equals("*") || _token.equals("/") || _token.equals("%")) {
            getToken();
            Number _temp = new Number();
            fourthStepParsing(_temp);
            if (_token.equals("/")) {
                if (_temp.get() == 0.0) {
                    throw new ParserException(ParserException.Error.DIVISION_BY_ZERO);
                }
                result.set(result.get() / _temp.get());
            } else if (_token.equals("%")) {
                if (_temp.get() == 0.0) {
                    throw new ParserException(ParserException.Error.DIVISION_BY_ZERO);
                }
                result.set(result.get() % _temp.get());
            } else if (_token.equals("*")) {
                result.set(result.get() * _temp.get());
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
        String _str;
        if (isRegularExpression((_str = storToken), "abs;acos;asin;atan;cos;log10;round;sin;sqrt;tan;")) {
            oneParameterFunctions(result, _str);
        } else if (isRegularExpression((_str = storToken), "pow;log;")) {
            twoParameterFunctions(result, _str);
        } else if (isRegularExpression((_str = storToken), "min;max;sum;avg;")) {
            multiParameterFunctions(result, _str);
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
        if (function.equals("abs")) {
            result.set(Math.abs(result.get()));
        } else if (function.equals("log10")) {
            result.set(Math.log10(result.get()));
        } else if (function.equals("round")) {
            result.set(Math.round(result.get()));
        } else if (function.equals("sqrt")) {
            result.set(Math.sqrt(result.get()));
        } else if (function.equals("acos")) {
            result.set(Math.acos(valueToMeasure(result.get())));
        } else if (function.equals("asin")) {
            result.set(Math.asin(valueToMeasure(result.get())));
        } else if (function.equals("atan")) {
            result.set(Math.atan(valueToMeasure(result.get())));
        } else if (function.equals("cos")) {
            result.set(Math.cos(valueToMeasure(result.get())));
        } else if (function.equals("sin")) {
            result.set(Math.sin(valueToMeasure(result.get())));
        } else if (function.equals("tan")) {
            result.set(Math.tan(valueToMeasure(result.get())));
        }
    }

    /**
     * Method returns converted values.
     *
     * @param result Basic value.
     * @return converted value.
     **/
    private double valueToMeasure(double result) {
        switch (typeTangentUnit) {
            case ParserType.DEGREE:
                result = result * Math.PI / 180;
                break;
            case ParserType.GRADUS:
                result = result * Math.PI / 200;
                break;
            case ParserType.RADIAN:
                break;
        }
        return result;
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

        if (idString == storString.length()) {
            return;
        }

        if (isDelimiter(storString.charAt(idString))) {
            strBuilder.append(storString.charAt(idString));
            idString++;
            typeToken = Types.DELIMITER;
        } else if (Character.isLetter(storString.charAt(idString))) { //isLetter??
            int _ctrl = 0;
            while (!isDelimiter(storString.charAt(idString))) {
                strBuilder.append(storString.charAt(idString));
                idString++;
                if (idString >= storString.length()) {
                    break;
                }
                _ctrl++;
                if (_ctrl >= 32) {
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
        int _idString = 0;
        StringBuilder _strbuf = new StringBuilder(expression.length());
        while (expression.length() > _idString) {
            // If we find the hiatus
            while (expression.charAt(_idString) == ' ') {
                _idString++;
            }
            // If the delimiter was found
            if (expression.charAt(_idString) == ';') {
                // No delimiter
                if (str.contentEquals(_strbuf)) {
                    return true;
                } else {
                    _strbuf.delete(0, _strbuf.length());
                }
            } else {
                _strbuf.append(expression.charAt(_idString));
            }
            _idString++;
        }
        // No delimiter
        return str.contentEquals(_strbuf);
    }

    /**
     * Types of tokens
     **/
    private enum Types {NONE, DELIMITER, VARIABLE, NUMBER, FUNCTION}
}

