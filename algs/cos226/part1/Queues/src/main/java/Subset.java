/**
 * @author Lipatov Nikita
 */
public class Subset {

    /**
     * How to run: go to console and execute command (IDEA + maven environment):
      Algorithms.Part 1\Queues\target\classes>echo AA BB CC DD FF II KK DD | java -cp .;../../../libs/stdlib.jar Subset 3
     */
    public static void main(String[] args) {

        int K = 0;
        if (args != null && args.length >= 1) {
            K = Integer.parseInt(args[0]);
        }

        RandomizedQueue<String> randomizedQueue = new RandomizedQueue<String>();

        if (K > 0) {
            String input;
            while (!StdIn.isEmpty()) {
                input = StdIn.readString();
                randomizedQueue.enqueue(input);
            }

            while (randomizedQueue.size() != 0 && randomizedQueue.size() > K) {
                randomizedQueue.dequeue();
            }
        }

        for (String item : randomizedQueue) {
            System.out.println(item);
        }
    }
}
