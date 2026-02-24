import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertEquals(1, sap.ancestor(3, 11));
        Assert.assertEquals(5, sap.ancestor(9, 12));
        Assert.assertEquals(0, sap.ancestor(7, 2));
        Assert.assertEquals(-1, sap.ancestor(1, 6));
        Assert.assertEquals(0, sap.ancestor(0, 2));
        Assert.assertEquals(10, sap.ancestor(11, 12));

        Assert.assertEquals(4, sap.length(3, 11));
        Assert.assertEquals(3, sap.length(9, 12));
        Assert.assertEquals(4, sap.length(7, 2));
        Assert.assertEquals(-1, sap.length(1, 6));
        Assert.assertEquals(2, sap.length(1, 2));
        Assert.assertEquals(1, sap.length(0, 2));
        Assert.assertEquals(2, sap.length(11, 12));

        // duplicates
        for (int i = 0; i < 1000; i++) {
            Assert.assertEquals(1, sap.ancestor(3, 11));
            Assert.assertEquals(5, sap.ancestor(9, 12));
            Assert.assertEquals(0, sap.ancestor(7, 2));
            Assert.assertEquals(-1, sap.ancestor(1, 6));
            Assert.assertEquals(0, sap.ancestor(0, 2));
            Assert.assertEquals(10, sap.ancestor(11, 12));

            Assert.assertEquals(4, sap.length(3, 11));
            Assert.assertEquals(3, sap.length(9, 12));
            Assert.assertEquals(4, sap.length(7, 2));
            Assert.assertEquals(-1, sap.length(1, 6));
            Assert.assertEquals(2, sap.length(1, 2));
            Assert.assertEquals(1, sap.length(0, 2));
            Assert.assertEquals(2, sap.length(11, 12));
        }
    }

    @Test
    public void testDigrapth2() {
        String path = "digraph2.txt";
        In in = new In(path);
        Digraph G = new Digraph(in);
        SAPFirst sap = new SAPFirst(G);

        Assert.assertEquals(0, sap.ancestor(0, 1));
        Assert.assertEquals(0, sap.ancestor(5, 0));
        Assert.assertEquals(2, sap.ancestor(1, 2));
        Assert.assertEquals(4, sap.ancestor(3, 4));
        Assert.assertEquals(5, sap.ancestor(4, 5));
        Assert.assertEquals(0, sap.ancestor(1, 5));
        Assert.assertEquals(0, sap.ancestor(1, 4));
        Assert.assertEquals(3, sap.ancestor(1, 3));

        Assert.assertEquals(1, sap.length(0, 1));
        Assert.assertEquals(1, sap.length(5, 0));
        Assert.assertEquals(1, sap.length(1, 2));
        Assert.assertEquals(1, sap.length(3, 4));
        Assert.assertEquals(1, sap.length(4, 5));
        Assert.assertEquals(2, sap.length(1, 5));

        // duplicates
        for (int i = 0; i < 1000; i++) {
            Assert.assertEquals(0, sap.ancestor(0, 1));
            Assert.assertEquals(0, sap.ancestor(5, 0));
            Assert.assertEquals(2, sap.ancestor(1, 2));
            Assert.assertEquals(4, sap.ancestor(3, 4));
            Assert.assertEquals(5, sap.ancestor(4, 5));
            Assert.assertEquals(0, sap.ancestor(1, 5));
            Assert.assertEquals(0, sap.ancestor(1, 4));
            Assert.assertEquals(3, sap.ancestor(1, 3));

            Assert.assertEquals(1, sap.length(0, 1));
            Assert.assertEquals(1, sap.length(5, 0));
            Assert.assertEquals(1, sap.length(1, 2));
            Assert.assertEquals(1, sap.length(3, 4));
            Assert.assertEquals(1, sap.length(4, 5));
            Assert.assertEquals(2, sap.length(1, 5));
        }
    }

    @Test
    public void testDigrapth3() {
        String path = "digraph3.txt";
        In in = new In(path);
        Digraph G = new Digraph(in);
        SAPFirst sap = new SAPFirst(G);

        Assert.assertEquals(1, sap.ancestor(1, 4));
        Assert.assertEquals(2, sap.ancestor(2, 5));
        Assert.assertEquals(3, sap.ancestor(3, 6));
        Assert.assertEquals(11, sap.ancestor(7, 13)); // dif SAP has 8
        Assert.assertEquals(11, sap.ancestor(10, 13));
        Assert.assertEquals(11, sap.ancestor(11, 13));
        Assert.assertEquals(12, sap.ancestor(12, 13));
        Assert.assertEquals(12, sap.ancestor(13, 12));

        Assert.assertEquals(3, sap.length(1, 4));
        Assert.assertEquals(3, sap.length(2, 5));
        Assert.assertEquals(3, sap.length(3, 6));
        Assert.assertEquals(7, sap.length(7, 13)); // dif SAP has 6
        Assert.assertEquals(4, sap.length(10, 13));
        Assert.assertEquals(3, sap.length(11, 13));
        Assert.assertEquals(4, sap.length(12, 13));
        Assert.assertEquals(4, sap.length(13, 12));

        // duplicates
        for (int i = 0; i < 1000; i++) {
            Assert.assertEquals(1, sap.ancestor(1, 4));
            Assert.assertEquals(2, sap.ancestor(2, 5));
            Assert.assertEquals(3, sap.ancestor(3, 6));
            Assert.assertEquals(11, sap.ancestor(7, 13)); // SAP has 8
            Assert.assertEquals(11, sap.ancestor(10, 13));
            Assert.assertEquals(11, sap.ancestor(11, 13));
            Assert.assertEquals(12, sap.ancestor(12, 13));
            Assert.assertEquals(12, sap.ancestor(13, 12));

            Assert.assertEquals(3, sap.length(1, 4));
            Assert.assertEquals(3, sap.length(2, 5));
            Assert.assertEquals(3, sap.length(3, 6));
            Assert.assertEquals(7, sap.length(7, 13)); // dif SAP has 6
            Assert.assertEquals(4, sap.length(10, 13));
            Assert.assertEquals(3, sap.length(11, 13));
            Assert.assertEquals(4, sap.length(12, 13));
            Assert.assertEquals(4, sap.length(13, 12));
        }
    }

    @Test
    public void testDigrapth4() {
        String path = "digraph4.txt";
        In in = new In(path);
        Digraph G = new Digraph(in);
        SAPFirst sap = new SAPFirst(G);

        Assert.assertEquals(8, sap.ancestor(1, 9));
        Assert.assertEquals(8, sap.ancestor(0, 7));
        Assert.assertEquals(6, sap.ancestor(3, 0));
        Assert.assertEquals(6, sap.ancestor(4, 8));

        Assert.assertEquals(4, sap.length(1, 9));
        Assert.assertEquals(2, sap.length(0, 7));
        Assert.assertEquals(5, sap.length(3, 0));
        Assert.assertEquals(3, sap.length(4, 8));

        // duplicates
        for (int i = 0; i < 1000; i++) {
            Assert.assertEquals(8, sap.ancestor(1, 9));
            Assert.assertEquals(8, sap.ancestor(0, 7));
            Assert.assertEquals(6, sap.ancestor(3, 0));
            Assert.assertEquals(6, sap.ancestor(4, 8));

            Assert.assertEquals(4, sap.length(1, 9));
            Assert.assertEquals(2, sap.length(0, 7));
            Assert.assertEquals(5, sap.length(3, 0));
            Assert.assertEquals(3, sap.length(4, 8));
        }
    }
}
