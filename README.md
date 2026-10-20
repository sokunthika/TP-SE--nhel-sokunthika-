# Task 2: Fixing a Race Condition

## 1. Objective

The purpose of this exercise is to reproduce a lost-update race condition using a shared counter, implement thread-safe solutions, and compare their performance.

## 2. Benchmark Configuration

- Number of threads: 8
- Increments per thread: 1,000,000
- Expected final counter value: 8,000,000
- Timing method: `System.nanoTime()`

## 3. Counter Implementations

### UnsafeCounter

Uses a plain `long` variable and `count++`. Because the increment operation is not atomic, concurrent threads can overwrite each other's updates, resulting in a lost-update race condition.

### SynchronizedCounter

Uses synchronized methods to ensure that only one thread at a time can access the counter through those methods.

### LockCounter

Uses `ReentrantLock` to protect the counter. The lock is released inside a `finally` block to ensure that it is not accidentally left locked.

### AtomicCounter

Uses `AtomicLong` and its `incrementAndGet()` method to perform thread-safe increments atomically.

### LongAdderCounter

Uses `LongAdder`, which distributes updates across internal cells under contention. This can improve throughput when many threads increment the counter frequently.

## 4. Benchmark Results

Record the actual output from your computer below.

| Implementation | Final value | Correct? | Time (ms) |
|---|---:|---|---:|
| UnsafeCounter #1 | Record result | Record result | Record result |
| UnsafeCounter #2 | Record result | Record result | Record result |
| UnsafeCounter #3 | Record result | Record result | Record result |
| SynchronizedCounter | Record result | Record result | Record result |
| LockCounter | Record result | Record result | Record result |
| AtomicCounter | Record result | Record result | Record result |
| LongAdderCounter | Record result | Record result | Record result |

## 5. Why is UnsafeCounter incorrect?

The expression `count++` is a read-modify-write operation. Two threads can read the same old value, calculate the same new value, and write it back. As a result, one increment can be lost.

## 6. Why can synchronizing only increment() still leave a bug?

Synchronizing `increment()` prevents multiple threads from performing the increment operation simultaneously on the same counter object. However, if `get()` is not synchronized or otherwise made thread-safe, a thread reading the counter concurrently may not have the required visibility guarantees for the latest updates. This is a data race between the synchronized writer and the unsynchronized reader.

Therefore, synchronizing both `increment()` and `get()` provides consistent protection for access through those methods. Other thread-safe approaches, such as `AtomicLong`, are also suitable.

In this benchmark, `get()` is called only after all worker threads have been joined. The `join()` operations provide the necessary happens-before relationship for the completed thread actions, so an unsynchronized `get()` after all joins does not necessarily produce an incorrect result in this particular test. The issue matters when reading concurrently.

## 7. Conclusion

The benchmark demonstrates that thread safety and performance are different concerns. The unsafe implementation may lose updates, while the correct implementations should produce 8,000,000. Their execution times depend on the machine and runtime conditions, so actual measurements should be used to compare their performance.

## Task 3: Producer–Consumer

### Objective

Implement a bounded buffer using `ArrayDeque`, `synchronized`, `wait()`, and `notifyAll()`. Then replace the custom buffer with `ArrayBlockingQueue` while reusing the same producers and consumers.

### Configuration

- Producers: 2
- Orders per producer: 50
- Consumers: 3
- Buffer capacity: 5
- Expected processed orders: 100

### Custom BoundedBuffer

The custom buffer uses a synchronized monitor to protect the shared queue. Producers wait while the buffer is full, and consumers wait while it is empty. After inserting or removing an order, the buffer calls `notifyAll()` to wake waiting threads so they can recheck their conditions.

### BlockingQueue

The second implementation uses `ArrayBlockingQueue`, which provides bounded storage and built-in thread coordination. The `OrderQueue` interface allows the same producer and consumer classes to work with both implementations.

### Experiment: Replacing `notifyAll()` with `notify()` and `while` with `if`

**Observed result:** [Describe what happened during your 20 runs.]

Using `if` instead of `while` is unsafe because a waiting thread must recheck its condition after waking. The buffer may still be full or empty, or the thread may wake spuriously.

Using `notify()` instead of `notifyAll()` can wake a thread that cannot make progress. For example, a producer may wake another producer while the buffer is still full, leaving consumers asleep even though a consumer needs to remove an item.

The exact failure depends on thread scheduling, so the broken version may not fail in every run.

### Results

| Implementation | Total processed | Elapsed time |
|---|---:|---:|
| Custom BoundedBuffer | 100 | [Record actual time] ms |
| ArrayBlockingQueue | 100 | [Record actual time] ms |

### Conclusion

Both implementations should process all 100 normal orders. The custom buffer demonstrates how guarded blocks and monitor coordination work, while `ArrayBlockingQueue` provides the same core functionality with less synchronization code. Performance depends on the runtime environment and should be compared using actual measurements.


#### Task 4: Thread Pools, Future and Virtual Threads

## 1. CPU-bound prime counting

The program counts prime numbers below 4,000,000 using both a single-threaded implementation and a fixed thread pool.

The parallel implementation divides the range into 16 non-overlapping chunks and submits each chunk as a Callable<Long> task. The results are collected through Future.get() and summed.

Both implementations should return 283146 primes. On a multicore machine, the parallel version may be faster because several platform threads can calculate different ranges concurrently.

## 2. I/O-bound simulated downloads

The program submits 500 simulated downloads to:
- A fixed pool of 20 platform threads.
- A virtual-thread-per-task executor.

Each successful download sleeps for 200 milliseconds to simulate waiting for a network response.

Virtual threads can handle many waiting tasks efficiently because the JVM can suspend virtual threads during supported blocking operations and use the underlying platform threads to run other tasks. A pool of 20 platform threads has a more limited number of workers, so tasks spend more time waiting in the queue.

Virtual threads are not automatically faster for CPU-intensive calculations. They are especially useful for applications with many concurrent tasks that spend time waiting for I/O.

## 3. Failure handling

Every 50th download throws IOException("boom"). The program collects results with Future.get() and catches ExecutionException to count failures without crashing.

Expected summary for each download run:
- Successful: 489
- Failed: 10
- Timed out: 1

## 4. Timeout and cancellation

One download sleeps for five seconds. The program waits for its result for one second using Future.get(1, TimeUnit.SECONDS).

If the deadline expires, the program catches TimeoutException and calls cancel(true), requesting interruption of the task.

## 5. Executor shutdown

The program uses try-with-resources to close each ExecutorService. This allows the executor to shut down and lets the JVM exit naturally after the work is completed.

## 6. Results

Record the actual times measured on your computer.

| Test | Result | Time |
|---|---|---|
| Single-threaded primes | 283146 | ... ms |
| Parallel primes | 283146 | ... ms |
| Fixed pool downloads | 489 ok, 10 failed, 1 timed out | ... ms |
| Virtual-thread downloads | 489 ok, 10 failed, 1 timed out | ... ms |

## 7. Conclusion

Future provides a way to retrieve results from asynchronous tasks and detect failures. ExecutorService manages task execution, while virtual threads can improve scalability for I/O-bound workloads. Timeouts and cancellation help the program handle slow tasks, and proper executor shutdown allows it to finish cleanly.
