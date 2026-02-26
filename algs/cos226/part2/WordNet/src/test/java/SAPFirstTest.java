import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Lipatov Nikita
 */
public class SAPFirstTest {

    @Test
    public void testDigrapth1() {
        String path = "digraph1.txt";
        In in = new In(path);
        Digraph G = new Digraph(in);
        SAPFirst sap = new SAPFirst(G);

        Assertions.assertEquals(1, sap.ancestor(3, 11));
        Assertions.assertEquals(5, sap.ancestor(9, 12));
        Assertions.assertEquals(0, sap.ancestor(7, 2));
        Assertions.assertEquals(-1, sap.ancestor(1, 6));
        Assertions.assertEquals(0, sap.ancestor(0, 2));
        Assertions.assertEquals(10, sap.ancestor(11, 12));

        Assertions.assertEquals(4, sap.length(3, 11));
        Assertions.assertEquals(3, sap.length(9, 12));
        Assertions.assertEquals(4, sap.length(7, 2));
        Assertions.assertEquals(-1, sap.length(1, 6));
        Assertions.assertEquals(2, sap.length(1, 2));
        Assertions.assertEquals(1, sap.length(0, 2));
        Assertions.assertEquals(2, sap.length(11, 12));

        // duplicates
        for (int i = 0; i < 1000; i++) {
            Assertions.assertEquals(1, sap.ancestor(3, 11));
            Assertions.assertEquals(5, sap.ancestor(9, 12));
            Assertions.assertEquals(0, sap.ancestor(7, 2));
            Assertions.assertEquals(-1, sap.ancestor(1, 6));
            Assertions.assertEquals(0, sap.ancestor(0, 2));
            Assertions.assertEquals(10, sap.ancestor(11, 12));

            Assertions.assertEquals(4, sap.length(3, 11));
            Assertions.assertEquals(3, sap.length(9, 12));
            Assertions.assertEquals(4, sap.length(7, 2));
            Assertions.assertEquals(-1, sap.length(1, 6));
            Assertions.assertEquals(2, sap.length(1, 2));
            Assertions.assertEquals(1, sap.length(0, 2));
            Assertions.assertEquals(2, sap.length(11, 12));
        }
    }

    @Test
    public void testDigrapth2() {
        String path = "digraph2.txt";
        In in = new In(path);
        Digraph G = new Digraph(in);
        SAPFirst sap = new SAPFirst(G);

        Assertions.assertEquals(0, sap.ancestor(0, 1));
        Assertions.assertEquals(0, sap.ancestor(5, 0));
        Assertions.assertEquals(2, sap.ancestor(1, 2));
        Assertions.assertEquals(4, sap.ancestor(3, 4));
        Assertions.assertEquals(5, sap.ancestor(4, 5));
        Assertions.assertEquals(0, sap.ancestor(1, 5));
        Assertions.assertEquals(0, sap.ancestor(1, 4));
        Assertions.assertEquals(3, sap.ancestor(1, 3));

        Assertions.assertEquals(1, sap.length(0, 1));
        Assertions.assertEquals(1, sap.length(5, 0));
        Assertions.assertEquals(1, sap.length(1, 2));
        Assertions.assertEquals(1, sap.length(3, 4));
        Assertions.assertEquals(1, sap.length(4, 5));
        Assertions.assertEquals(2, sap.length(1, 5));

        // duplicates
        for (int i = 0; i < 1000; i++) {
            Assertions.assertEquals(0, sap.ancestor(0, 1));
            Assertions.assertEquals(0, sap.ancestor(5, 0));
            Assertions.assertEquals(2, sap.ancestor(1, 2));
            Assertions.assertEquals(4, sap.ancestor(3, 4));
            Assertions.assertEquals(5, sap.ancestor(4, 5));
            Assertions.assertEquals(0, sap.ancestor(1, 5));
            Assertions.assertEquals(0, sap.ancestor(1, 4));
            Assertions.assertEquals(3, sap.ancestor(1, 3));

            Assertions.assertEquals(1, sap.length(0, 1));
            Assertions.assertEquals(1, sap.length(5, 0));
            Assertions.assertEquals(1, sap.length(1, 2));
            Assertions.assertEquals(1, sap.length(3, 4));
            Assertions.assertEquals(1, sap.length(4, 5));
            Assertions.assertEquals(2, sap.length(1, 5));
        }
    }

    @Test
    public void testDigrapth3() {
        String path = "digraph3.txt";
        In in = new In(path);
        Digraph G = new Digraph(in);
        SAPFirst sap = new SAPFirst(G);

        Assertions.assertEquals(1, sap.ancestor(1, 4));
        Assertions.assertEquals(2, sap.ancestor(2, 5));
        Assertions.assertEquals(3, sap.ancestor(3, 6));
        Assertions.assertEquals(11, sap.ancestor(7, 13)); // dif SAP has 8
        Assertions.assertEquals(11, sap.ancestor(10, 13));
        Assertions.assertEquals(11, sap.ancestor(11, 13));
        Assertions.assertEquals(12, sap.ancestor(12, 13));
        Assertions.assertEquals(12, sap.ancestor(13, 12));

        Assertions.assertEquals(3, sap.length(1, 4));
        Assertions.assertEquals(3, sap.length(2, 5));
        Assertions.assertEquals(3, sap.length(3, 6));
        Assertions.assertEquals(7, sap.length(7, 13)); // dif SAP has 6
        Assertions.assertEquals(4, sap.length(10, 13));
        Assertions.assertEquals(3, sap.length(11, 13));
        Assertions.assertEquals(4, sap.length(12, 13));
        Assertions.assertEquals(4, sap.length(13, 12));

        // duplicates
        for (int i = 0; i < 1000; i++) {
            Assertions.assertEquals(1, sap.ancestor(1, 4));
            Assertions.assertEquals(2, sap.ancestor(2, 5));
            Assertions.assertEquals(3, sap.ancestor(3, 6));
            Assertions.assertEquals(11, sap.ancestor(7, 13)); // SAP has 8
            Assertions.assertEquals(11, sap.ancestor(10, 13));
            Assertions.assertEquals(11, sap.ancestor(11, 13));
            Assertions.assertEquals(12, sap.ancestor(12, 13));
            Assertions.assertEquals(12, sap.ancestor(13, 12));

            Assertions.assertEquals(3, sap.length(1, 4));
            Assertions.assertEquals(3, sap.length(2, 5));
            Assertions.assertEquals(3, sap.length(3, 6));
            Assertions.assertEquals(7, sap.length(7, 13)); // dif SAP has 6
            Assertions.assertEquals(4, sap.length(10, 13));
            Assertions.assertEquals(3, sap.length(11, 13));
            Assertions.assertEquals(4, sap.length(12, 13));
            Assertions.assertEquals(4, sap.length(13, 12));
        }
    }

    @Test
    public void testDigrapth4() {
        String path = "digraph4.txt";
        In in = new In(path);
        Digraph G = new Digraph(in);
        SAPFirst sap = new SAPFirst(G);

        Assertions.assertEquals(8, sap.ancestor(1, 9));
        Assertions.assertEquals(8, sap.ancestor(0, 7));
        Assertions.assertEquals(6, sap.ancestor(3, 0));
        Assertions.assertEquals(6, sap.ancestor(4, 8));

        Assertions.assertEquals(4, sap.length(1, 9));
        Assertions.assertEquals(2, sap.length(0, 7));
        Assertions.assertEquals(5, sap.length(3, 0));
        Assertions.assertEquals(3, sap.length(4, 8));

        // duplicates
        for (int i = 0; i < 1000; i++) {
            Assertions.assertEquals(8, sap.ancestor(1, 9));
            Assertions.assertEquals(8, sap.ancestor(0, 7));
            Assertions.assertEquals(6, sap.ancestor(3, 0));
            Assertions.assertEquals(6, sap.ancestor(4, 8));

            Assertions.assertEquals(4, sap.length(1, 9));
            Assertions.assertEquals(2, sap.length(0, 7));
            Assertions.assertEquals(5, sap.length(3, 0));
            Assertions.assertEquals(3, sap.length(4, 8));
        }
    }
}
