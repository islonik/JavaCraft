package my.javacraft.mathparser.parser;

/**
 * The Roman numeral system uses only seven symbols: I, V, X, L, C, D, and M.
 *
 * I is 1
 * V is 5
 * X is 10
 * L is 50
 * C is 100
 * D is 500
 * M is 1000
 */
public class RomanNumbers {

    final static String[] Thousands = {"", "M", "MM", "MMM", "MMMM"};
    final static String[] Hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
    final static String[] Tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    final static String[] Units = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

    static String toRoman(int number) {
        StringBuilder roman = new StringBuilder();
        roman.append(Thousands[number / 1000]);
        roman.append(Hundreds[(number % 1000) / 100]);
        roman.append(Tens[(number % 100) / 10]);
        roman.append(Units[number % 10]);
        return roman.toString();
    }

}
