import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lipatov Nikita
 */
public class CircularSuffixArray {

    private String source;
    private int []array;
    private int size;

    // circular suffix array of s
    public CircularSuffixArray(String source) {
        if (source == null) {
            throw new NullPointerException("The argument of CircularSuffixArray is null!");
        }
        this.source = source;
        this.size = source.length();

        List<Node> suffixes = sort();

        this.array = new int[this.size];
        for (int i = 0; i < suffixes.size(); i++) {
            this.array[i] = suffixes.get(i).index;
        }
    }

    // length of s
    public int length() {
        return this.size;
    }

    // returns index of ith sorted suffix
    public int index(int i) {
        if (i >= length()) {
            throw new IndexOutOfBoundsException();
        }
        return array[i];
    }

    private List<Node> sort() {
        StringBuilder stringBuilder = new StringBuilder(this.source);
        List<Node> suffixes = biasAll(stringBuilder);
        Collections.sort(suffixes);
        return suffixes;
    }

    private List<Node> biasAll(StringBuilder stringBuilder){
        List<Node> result = new ArrayList<Node>();

        Node defNode = new Node();
        defNode.source = stringBuilder.toString();
        defNode.index = 0;
        result.add(defNode);

        for (int i = 1; i < this.size; i++) {
            Node temp = new Node();
            char ch = stringBuilder.charAt(0);
            stringBuilder = stringBuilder.deleteCharAt(0);
            stringBuilder.append(ch);
            temp.source = stringBuilder.toString();
            temp.index = i;
            result.add(temp);
        }
        return result;
    }

    private class Node implements Comparable<Node> {
        private String source;
        private int index;

        @Override
        public int compareTo(Node o) {
            return source.compareTo(o.source);
        }
    }

    // unit testing of the methods (optional)
    public static void main(String[] args) {
    }
}
