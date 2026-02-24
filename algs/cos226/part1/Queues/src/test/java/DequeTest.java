import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Lipatov Nikita
 */
public class DequeTest {

    @Test
    public void testSimpleAddFirst() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assert.assertEquals(3, deque.size());
    }

    @Test(expected = NullPointerException.class)
    public void testSimpleAddFirstException() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst(null);
    }

    @Test
    public void testSimpleAddLast() {
        Deque<String> deque = new Deque<String>();
        deque.addLast("first item");
        deque.addLast("second item");
        deque.addLast("third item");

        Assert.assertEquals(3, deque.size());
    }

    @Test(expected = NullPointerException.class)
    public void testSimpleAddLastException() {
        Deque<String> deque = new Deque<String>();
        deque.addLast("first item");
        deque.addLast("second item");
        deque.addLast(null);
    }

    @Test
    public void testSimpleAddFirstAddLast() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("second item");
        deque.addLast("third item");
        deque.addFirst("first item");

        Assert.assertEquals(3, deque.size());
    }

    @Test
    public void testSimpleAddLastAddFirst() {
        Deque<String> deque = new Deque<String>();
        deque.addLast("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assert.assertEquals(3, deque.size());
    }

    @Test
    public void testSimpleAddFirstRemoveFirst() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assert.assertEquals(3, deque.size());

        Assert.assertEquals("first item", deque.removeFirst());
        Assert.assertEquals("second item",  deque.removeFirst());
        Assert.assertEquals("third item",  deque.removeFirst());
        Assert.assertEquals(0, deque.size());

        deque.addFirst("first item");
        Assert.assertEquals(1, deque.size());
        Assert.assertEquals("first item", deque.removeFirst());
        Assert.assertEquals(0, deque.size());
        Assert.assertTrue(deque.isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void testSimpleAddFirstRemoveException() {
        Deque<String> deque = new Deque<String>();
        deque.removeFirst();
    }

    @Test
    public void testSimpleAddFirstRemoveLast() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assert.assertEquals(3, deque.size());

        Assert.assertEquals("third item", deque.removeLast());
        Assert.assertEquals("second item",  deque.removeLast());
        Assert.assertEquals("first item",  deque.removeLast());
        Assert.assertEquals(0, deque.size());
        Assert.assertTrue(deque.isEmpty());
    }

    @Test
    public void testSimpleAddFirstRemoveLastAddLast() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assert.assertEquals(3, deque.size());

        Assert.assertEquals("third item", deque.removeLast());
        Assert.assertEquals("second item",  deque.removeLast());
        Assert.assertEquals("first item",  deque.removeLast());

        deque.addLast("second item(2)");
        deque.addLast("third item(2)");

    }

    @Test(expected = NoSuchElementException.class)
    public void testSimpleAddLastRemoveException() {
        Deque<String> deque = new Deque<String>();
        deque.removeLast();
    }

    @Test
    public void testSimpleIteratorFor() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assert.assertEquals(3, deque.size());

        for (String iter : deque) {
            System.out.println(iter);
        }
    }

    @Test
    public void testSimpleIteratorWhile() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assert.assertEquals(3, deque.size());

        Iterator<String> iterator = deque.iterator();
        while (iterator.hasNext()) {
            String iter = iterator.next();
            System.out.println(iter);
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testSimpleIteratorNextException() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assert.assertEquals(3, deque.size());

        Iterator<String> iterator = deque.iterator();
        while (iterator.hasNext()) {
            Assert.assertTrue(iterator.hasNext());
            String iter = iterator.next();
        }
        Assert.assertFalse(iterator.hasNext());
        iterator.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSimpleIteratorRemoveException() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assert.assertEquals(3, deque.size());

        Iterator<String> iterator = deque.iterator();
        while (iterator.hasNext()) {
            iterator.remove();
        }
    }
}
