import java.util.LinkedList;

/**
 * @author Lipatov Nikita
 */
public class MoveToFront {

    private static final int R = 256;

    // apply move-to-front encoding, reading from standard input and writing to standard output
    public static void encode()
    {
        String inputString = BinaryStdIn.readString();
        char[] inputCharsArray = inputString.toCharArray();

        // Store the list of chars.
        LinkedList<Integer> storeList = new LinkedList<Integer>();
        for (int i = 0; i < R; i++) {
            storeList.add(i);
        }
        // Check whether the char is in the list.
        for (int i = 0; i < inputCharsArray.length; i++) {
            int index = storeList.indexOf((int) inputCharsArray[i]);
            BinaryStdOut.write((char) index, 8);
            int remObj = storeList.remove(index);
            storeList.add(0, remObj);
        }

        BinaryStdOut.close();
    }

    // apply move-to-front decoding, reading from standard input and writing to standard output
    public static void decode()
    {
        String inputString = BinaryStdIn.readString();
        char[] inputCharArray = inputString.toCharArray();

        LinkedList<Integer> storeList = new LinkedList<Integer>();
        for (int i = 0; i < R; i++) {
            storeList.add(i);
        }

        for (int i = 0; i < inputCharArray.length; i++) {
            int index = storeList.remove((int) inputCharArray[i]);
            storeList.add(0, index);
            BinaryStdOut.write((char) index, 8);
        }

        // Total, worst, R*N, Best, N
        BinaryStdOut.close();
    }

    /**
     * How to run this:
     * 1) compile all classes from default package
     * 2) put file abra.txt in target/classes directory
     * 3) go to console in target/classes directory and run next command (Windows env):

     java -classpath .;../../../libs/* Huffman - < abra.txt | java -classpath .;../../../libs/* HexDump 16
     ER:
     50 4a 22 43 43 54 a8 40 00 00 01 8f 96 8f 94
     120 bits

        or this

     java -classpath .;../../../libs/* Huffman - < abra.txt | java -classpath .;../../../libs/* Huffman +
     ER:
     ABRACADABRA!

     */
    // if args[0] is '-', apply move-to-front encoding
    // if args[0] is '+', apply move-to-front decoding
    public static void main(String[] args)
    {
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
