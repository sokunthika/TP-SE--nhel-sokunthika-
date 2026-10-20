
package edu.itc.se.lab01.task4;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    static final long LIMIT = 4_000_000;
    static final int CHUNKS = 16;
    static final int DOWNLOADS = 500;

    // Single-threaded version for comparison.
    static long countPrimes(long from, long to) {
        return new PrimeCounter(from, to).call();
    }

    static void runDownloads(
            String label,
            ExecutorService executor) throws Exception {

        long start = System.nanoTime();
        List<Future<String>> futures = new ArrayList<>();

        // Submit all 500 download tasks.
        for (int i = 1; i <= DOWNLOADS; i++) {
            boolean fail = i % 50 == 0;
            boolean slow = i == 1;

            FakeDownload task = new FakeDownload(
                    "https://example.com/file" + i,
                    fail,
                    slow);

            futures.add(executor.submit(task));
        }

        int ok = 0;
        int failed = 0;
        int timedOut = 0;

        // Task 1 is deliberately slow; give it a 1-second deadline.
        try {
            futures.get(0).get(1, TimeUnit.SECONDS);
            ok++;
        } catch (TimeoutException e) {
            futures.get(0).cancel(true);
            timedOut++;
        } catch (ExecutionException e) {
            failed++;
        }

        // Collect all remaining results.
        for (int i = 1; i < futures.size(); i++) {
            try {
                futures.get(i).get();
                ok++;
            } catch (ExecutionException e) {
                failed++;
            } catch (CancellationException e) {
                timedOut++;
            }
        }

        long ms = (System.nanoTime() - start) / 1_000_000;

        System.out.printf(
                "%-40s ok=%d failed=%d timedOut=%d in %d ms%n",
                label, ok, failed, timedOut, ms);
    }

    public static void main(String[] args) throws Exception {
        // PART A: CPU-bound prime counting.
        long t0 = System.nanoTime();
        long single = countPrimes(0, LIMIT);
        long singleMs = (System.nanoTime() - t0) / 1_000_000;

        System.out.printf(
                "single-threaded: %d primes in %d ms%n",
                single, singleMs);

        t0 = System.nanoTime();
        long total = 0;

        int workers = Runtime.getRuntime().availableProcessors();

        try (ExecutorService pool = Executors.newFixedThreadPool(workers)) {

            List<Future<Long>> futures = new ArrayList<>();

            long chunkSize = LIMIT / CHUNKS;

            for (int i = 0; i < CHUNKS; i++) {
                long from = i * chunkSize;
                long to = (i == CHUNKS - 1)
                        ? LIMIT
                        : from + chunkSize;

                futures.add(pool.submit(new PrimeCounter(from, to)));
            }

            for (Future<Long> future : futures) {
                total += future.get();
            }
        }

        long parallelMs = (System.nanoTime() - t0) / 1_000_000;

        System.out.printf(
                "pool (%d threads): %d primes in %d ms%n",
                workers, total, parallelMs);

        if (single != total) {
            throw new AssertionError(
                    "Single and parallel prime counts differ!");
        }

        // PART B: I/O-bound simulated downloads.
        try (ExecutorService pool = Executors.newFixedThreadPool(20)) {
            runDownloads(
                    "downloads on fixed(20) platform threads",
                    pool);
        }

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            runDownloads(
                    "downloads on virtual threads",
                    pool);
        }
    }
}
