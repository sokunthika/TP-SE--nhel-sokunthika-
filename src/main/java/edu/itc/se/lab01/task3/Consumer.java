package edu.itc.se.lab01.task3;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer implements Runnable {
    private final OrderQueue queue;
    private final AtomicInteger processed;
    private final String name;
    private final Random random = new Random();

    public Consumer(OrderQueue queue, AtomicInteger processed, String name) {
        this.queue = queue;
        this.processed = processed;
        this.name = name;
    }

    @Override 
    public void run() {
        try {
            while (true) {
                Order order = queue.take();

                if (order.id() == -1) {
                    System.out.println(name + " received poison pill, stopping");

                    break;
                }

                Thread.sleep(5 + random.nextInt(26));

                System.out.println(name + " processed" + order);

                processed.incrementAndGet();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
