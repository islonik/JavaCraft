import java.util.Arrays;

/**
 * @author Lipatov Nikita
 */
public class BurrowsWheeler {
    private static final int R = 256;

    // apply Burrows-Wheeler encoding, reading from standard input and writing to standard output
    public static void encode() {
        String str = BinaryStdIn.readString();
        char[] input = str.toCharArray();

        CircularSuffixArray csa = new CircularSuffixArray(str);

        for (int i = 0; i < csa.length(); i++) {

            if (csa.index(i) == 0) {
                BinaryStdOut.write(i);
                break;
            }
        }

        for (int i = 0; i < str.length(); i++) {
            int idx = (csa.index(i) + csa.length() - 1) % csa.length();
            BinaryStdOut.write(input[idx], 8);
        }

        BinaryStdOut.close();
    }

    // apply Burrows-Wheeler decoding, reading from standard input and writing to standard output
    public static void decode() {
        int first = BinaryStdIn.readInt();
        String str = BinaryStdIn.readString();
        char[] input = str.toCharArray();
        char[] sorted = new char[input.length];

        for (int i = 0; i < input.length; i++) {
            sorted[i] = input[i];
        }

        Arrays.sort(sorted);

        int []baseIndex = new int[R];
        int []next = new int[input.length];

        // First, construct the next array...
        for (int i = 0; i < input.length; i++) {
            next[i] = getNextChar(sorted[i], input, baseIndex);
        }

        // show the string.
        int i;
        int ptr;
        for (i = 0, ptr = first; i < next.length; i++, ptr = next[ptr]) {
            BinaryStdOut.write(sorted[ptr], 8);
        }

        BinaryStdOut.close();
    }

    private static int getNextChar(char c, char[] input, int[] baseIndex)
    {
        for (int i = baseIndex[c]; i < input.length; i++) {
            if (input[i] == c) {
                baseIndex[c] = i + 1;
                return i;
            }
        }
        return 0; // end
    }

    /**
     *
     * How to run:
     java -classpath .;../../../libs/* BurrowsWheeler - < abra.txt | java -classpath .;../../../libs/* HexDump 16
     ER:
     00 00 00 03 41 52 44 21 52 43 41 41 41 41 42 42
     128 bits
     * @param args
     */
    // if args[0] is '-', apply Burrows-Wheeler encoding
    // if args[0] is '+', apply Burrows-Wheeler decoding
    public static void main(String[] args) {
        if (args != null && args.length >= 1) {
            String arg = args[0];
            if (arg.equals("-")) {
                encode();
            } else if (arg.equals("+")) {
                decode();
            }
        } else { // default case
            System.out.println("Nothing is found!");
        }
    }
}
