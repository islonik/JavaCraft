import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        Assertions.assertEquals(3, deque.size());
    }

    @Test
    public void testSimpleAddFirstException() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        Assertions.assertThrows(NullPointerException.class, () -> {
            deque.addFirst(null);
        });
    }

    @Test
    public void testSimpleAddLast() {
        Deque<String> deque = new Deque<String>();
        deque.addLast("first item");
        deque.addLast("second item");
        deque.addLast("third item");

        Assertions.assertEquals(3, deque.size());
    }

    @Test
    public void testSimpleAddLastException() {
        Deque<String> deque = new Deque<String>();
        deque.addLast("first item");
        deque.addLast("second item");
        Assertions.assertThrows(NullPointerException.class, () -> {
            deque.addLast(null);
        });
    }

    @Test
    public void testSimpleAddFirstAddLast() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("second item");
        deque.addLast("third item");
        deque.addFirst("first item");

        Assertions.assertEquals(3, deque.size());
    }

    @Test
    public void testSimpleAddLastAddFirst() {
        Deque<String> deque = new Deque<String>();
        deque.addLast("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assertions.assertEquals(3, deque.size());
    }

    @Test
    public void testSimpleAddFirstRemoveFirst() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assertions.assertEquals(3, deque.size());

        Assertions.assertEquals("first item", deque.removeFirst());
        Assertions.assertEquals("second item",  deque.removeFirst());
        Assertions.assertEquals("third item",  deque.removeFirst());
        Assertions.assertEquals(0, deque.size());

        deque.addFirst("first item");
        Assertions.assertEquals(1, deque.size());
        Assertions.assertEquals("first item", deque.removeFirst());
        Assertions.assertEquals(0, deque.size());
        Assertions.assertTrue(deque.isEmpty());
    }

    @Test
    public void testSimpleAddFirstRemoveException() {
        Deque<String> deque = new Deque<String>();
        Assertions.assertThrows(NoSuchElementException.class, () -> {
            deque.removeFirst();
        });
    }

    @Test
    public void testSimpleAddFirstRemoveLast() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assertions.assertEquals(3, deque.size());

        Assertions.assertEquals("third item", deque.removeLast());
        Assertions.assertEquals("second item",  deque.removeLast());
        Assertions.assertEquals("first item",  deque.removeLast());
        Assertions.assertEquals(0, deque.size());
        Assertions.assertTrue(deque.isEmpty());
    }

    @Test
    public void testSimpleAddFirstRemoveLastAddLast() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assertions.assertEquals(3, deque.size());

        Assertions.assertEquals("third item", deque.removeLast());
        Assertions.assertEquals("second item",  deque.removeLast());
        Assertions.assertEquals("first item",  deque.removeLast());

        deque.addLast("second item(2)");
        deque.addLast("third item(2)");

    }

    @Test
    public void testSimpleAddLastRemoveException() {
        Deque<String> deque = new Deque<String>();
        Assertions.assertThrows(NoSuchElementException.class, () -> {
            deque.removeLast();
        });
    }

    @Test
    public void testSimpleIteratorFor() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assertions.assertEquals(3, deque.size());

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

        Assertions.assertEquals(3, deque.size());

        Iterator<String> iterator = deque.iterator();
        while (iterator.hasNext()) {
            String iter = iterator.next();
            System.out.println(iter);
        }
    }

    @Test
    public void testSimpleIteratorNextException() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assertions.assertEquals(3, deque.size());

        Iterator<String> iterator = deque.iterator();
        while (iterator.hasNext()) {
            Assertions.assertTrue(iterator.hasNext());
            String iter = iterator.next();
        }
        Assertions.assertFalse(iterator.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, () -> {
            iterator.next();
        });
    }

    @Test
    public void testSimpleIteratorRemoveException() {
        Deque<String> deque = new Deque<String>();
        deque.addFirst("third item");
        deque.addFirst("second item");
        deque.addFirst("first item");

        Assertions.assertEquals(3, deque.size());

        Iterator<String> iterator = deque.iterator();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            while (iterator.hasNext()) {
                iterator.remove();
            }
        });
    }
}
