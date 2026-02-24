import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Lipatov Nikita
 */
public class RandomizedQueue<Item> implements Iterable<Item> {

    private static final int DEFAULT_SIZE = 32;

    private Item []items;
    private int size;

    // construct an empty randomized queue
    public RandomizedQueue() {
        items = (Item[]) new Object[DEFAULT_SIZE];
    }

    private void resize(int newCapacity) {
        Item []temp = (Item[]) new Object[newCapacity];
        for (int i = 0; i < items.length; i++) {
            temp[i] = items[i];
        }
        items = temp;
    }

    // is the queue empty?
    public boolean isEmpty() {
        return (size() == 0);
    }

    // return the number of items on the queue
    public int size() {
        return size;
    }

    // add the item
    public void enqueue(Item item) {
        if (item == null) {
            throw new NullPointerException("Item is null!");
        }
        if (size() > items.length * 0.75) {
            resize(items.length * 2);
        }
        items[size++] = item;
    }

    // delete and return a random item
    public Item dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty!");
        }
        int random = StdRandom.uniform(size);
        Item temp = items[random];
        if (random + 1 == size) {
            items[size - 1] = null;
        } else {
            items[random] = items[size - 1];
            items[size - 1] = null;
        }
        size--;
        return temp;
    }

    // return (but do not delete) a random item
    public Item sample() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty!");
        }
        int random = StdRandom.uniform(size);
        return items[random];
    }

    // return an independent iterator over items in random order
    public Iterator<Item> iterator() {
        return new RandomizedQueueItr(items, size);
    }

    // unit testing
    public static void main(String[] args) {

    }

    private class RandomizedQueueItr implements Iterator<Item> {

        private Item []queue;
        private Item currentItem = null;
        private int index;

        public RandomizedQueueItr(Item []queue, int size) {
            Item []temp = (Item[]) new Object[size];
            for (int i = 0; i < size; i++) {
                temp[i] = queue[i];
            }
            StdRandom.shuffle(temp);
            this.queue = temp;
            index = 0;
            if (size != 0) {
                currentItem = this.queue[index++];
            }
        }

        public boolean hasNext() {
            return currentItem != null;
        }

        public Item next() {
            if (currentItem == null) {
                throw new NoSuchElementException();
            }
            Item temp = currentItem;
            if (index == queue.length) {
                currentItem = null;
            } else {
                currentItem = queue[index++];
            }
            return temp;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
