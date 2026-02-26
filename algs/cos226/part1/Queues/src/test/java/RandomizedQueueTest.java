import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Lipatov Nikita
 */
public class RandomizedQueueTest {

    @Test
    public void testAddAndIterator() {
        RandomizedQueue<String> queue = new RandomizedQueue<>();
        queue.enqueue("test-1");
        queue.enqueue("test-2");
        queue.enqueue("test-3");
        queue.enqueue("test-4");

        Assertions.assertEquals(4, queue.size());

        System.out.println("Random object = " + queue.sample());

        Iterator<String> queueItr = queue.iterator();
        while(queueItr.hasNext()) {
            System.out.println(queueItr.next());
        }
        Assertions.assertFalse(queueItr.hasNext());
    }

    @Test
    public void testAddDeleteAndIterator() {
        RandomizedQueue<String> queue = new RandomizedQueue<>();

        for (int index = 0; index < 20; index++) {
            queue.enqueue("test-1");
            queue.enqueue("test-2");
            queue.enqueue("test-3");
            queue.enqueue("test-4");

            queue.dequeue();
            queue.dequeue();
            queue.dequeue();

            Assertions.assertEquals(1, queue.size());

            System.out.println("One left random object = " + queue.sample());

            Iterator<String> queueItr = queue.iterator();
            while(queueItr.hasNext()) {
                System.out.println(queueItr.next());
            }
            Assertions.assertFalse(queueItr.hasNext());
            queue.dequeue();
        }
    }

    @Test
    public void testEmptyDequeue() {
        RandomizedQueue queue = new RandomizedQueue<>();
        Assertions.assertThrows(NoSuchElementException.class, () -> {
            queue.dequeue();
        });
    }

    @Test
    public void testEmptySample() {
        RandomizedQueue queue = new RandomizedQueue<>();
        Assertions.assertThrows(NoSuchElementException.class, () -> {
            queue.sample();
        });
    }
}
