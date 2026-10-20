
package edu.itc.se.lab01.task3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final int PRODUCERS = 2;
    private static final int ORDERS_PER_PRODUCER = 50;
    private static final int CONSUMERS = 3;
    private static final int CAPACITY = 5;

    public static void main(String[] args)
            throws InterruptedException {

        OrderQueue queue = new BoundedBuffer(CAPACITY);
        AtomicInteger processed = new AtomicInteger(0);
        AtomicInteger producersRemaining = new AtomicInteger(PRODUCERS);

        List<Thread> consumerThreads = new ArrayList<>();
        List<Thread> producerThreads = new ArrayList<>();

        long start = System.nanoTime();

        // Start the consumers first.
        for (int i = 1; i <= CONSUMERS; i++) {
            Thread thread = new Thread(
                    new Consumer(queue, processed, "consumer-" + i));
            consumerThreads.add(thread);
            thread.start();
        }

        // Start the producers.
        for (int i = 1; i <= PRODUCERS; i++) {
            Thread thread = new Thread(
                    new Producer(
                            queue,
                            ORDERS_PER_PRODUCER,
                            producersRemaining,
                            CONSUMERS),
                    "producer-" + i);

            producerThreads.add(thread);
            thread.start();
        }

        // Wait until all producers finish.
        for (Thread thread : producerThreads) {
            thread.join();
        }

        // The poison pills allow consumers to stop.
        for (Thread thread : consumerThreads) {
            thread.join();
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        int expected = PRODUCERS * ORDERS_PER_PRODUCER;
        int actual = processed.get();

        System.out.printf(
                "Total processed: %d (expected %d) in %d ms%n",
                actual, expected, ms);

        if (actual != expected) {
            throw new AssertionError(
                    "Expected " + expected + " processed orders, got "
                            + actual);
        }
    }
}
