package com.arangodb.benchmark;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public abstract class AbstractBenchmark {

    private final CountDownLatch completed = new CountDownLatch(1);
    private volatile Long startTime = null;
    private volatile Long endTime = null;
    private volatile int targetCount = Integer.MAX_VALUE;
    private final AtomicInteger counter = new AtomicInteger();

    private static Map<String, Function<HttpProtocolVersion, AbstractBenchmark>> instantiatorMap = Map.of(
            "HttpClient4", HttpClient4Benchmark::new,
            "HttpClient5", HttpClient5Benchmark::new,
            "HttpClient5Async", HttpClient5AsyncBenchmark::new,
            "Vertx", VertxBenchmark::new
    );

    public static AbstractBenchmark of(String type, String httpVersion) {
        if (!instantiatorMap.containsKey(type))
            throw new IllegalArgumentException(type);
        return instantiatorMap.get(type).apply(HttpProtocolVersion.of(httpVersion));
    }

    public void startMonitor(int duration) {
        for (int i = 0; i < duration; i++) {
            counter.set(0);
            long start = new Date().getTime();
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long current = new Date().getTime();
            long elapsed = current - start;
            double reqsPerSec = 1_000.0 * counter.get() / elapsed;
            System.out.println("reqs/s: " + reqsPerSec);
        }
    }

    public void startBenchmark() {
        start();
        new Thread(() -> {
            try {
                completed.await();
                // wait graceful shutdown
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // force shutdown
            shutdown();
        }).start();
    }

    public void startMeasuring(int count) {
        counter.set(0);
        targetCount = count;
        startTime = System.currentTimeMillis();
    }

    public long waitComplete() {
        try {
            completed.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return endTime - startTime;
    }

    /**
     * @return req/s
     */
    public long getThroughput() {
        return targetCount * 1000L / (endTime - startTime);
    }

    /**
     * notify the success of #count requests
     *
     * @return whether more requests should be performed
     */
    protected boolean success(int count) {
        if (endTime != null) return false;
        if (counter.addAndGet(count) >= targetCount) {
            endTime = System.currentTimeMillis();
            completed.countDown();
            return false;
        }
        return true;
    }

    protected abstract void start();

    protected abstract void shutdown();

    public abstract HttpProtocolVersion getHttpVersion();

}
