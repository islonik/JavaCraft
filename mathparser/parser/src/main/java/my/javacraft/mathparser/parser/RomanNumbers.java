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

    static int fromRoman(String romanNumber) {
        int number = 0;

        for (int i = Thousands.length - 1; i >= 0; i--) {
            if (romanNumber.startsWith(Thousands[i])) {
                number += i * 1000;
                romanNumber = romanNumber.substring(Thousands[i].length());
                break;
            }
        }

        for (int i = Hundreds.length - 1; i >= 0; i--) {
            if (romanNumber.startsWith(Hundreds[i])) {
                number += i * 100;
                romanNumber = romanNumber.substring(Hundreds[i].length());
                break;
            }
        }

        for (int i = Tens.length - 1; i >= 0; i--) {
            if (romanNumber.startsWith(Tens[i])) {
                number += i * 10;
                romanNumber = romanNumber.substring(Tens[i].length());
                break;
            }
        }

        for (int i = Units.length - 1; i >= 0; i--) {
            if (romanNumber.startsWith(Units[i])) {
                number += i;
                break;
            }
        }

        return number;
    }

}
