package edu.itc.se.lab01.task3;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Producer implements Runnable{
    private final OrderQueue queue;
    private final int orderCount;
    private final AtomicInteger producersRemaining;
    private final int consumerCount;
    private final Random random = new Random();

    public Producer(OrderQueue queue, int orderCount, AtomicInteger producersRemaining, int consumerCount) {
        this.queue = queue;
        this.orderCount = orderCount;
        this.producersRemaining = producersRemaining;
        this.consumerCount = consumerCount;
    }

    @Override 
    public void run() {
        try {
            for (int i = 0; i < orderCount; i++){
                Order order = new Order(
                    i+1,
                    "item-" + (i + 1),
                    random.nextInt(5) + 1
                );

                queue.put(order);

                Thread.sleep(random.nextInt(21));
            }
            if (producersRemaining.decrementAndGet() == 0) {
                for (int i = 0; i < consumerCount; i++) {
                    queue.put(new Order(-1, "POISON", 0));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
