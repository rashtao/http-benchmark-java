package com.arangodb.benchmark;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public abstract class AbstractBenchmark {

    public static final String USER = getEnv("JB_USER", "root");
    public static final String PASSWD = getEnv("JB_PASSWD", "test");
    public static final String AUTH_HEADER = "Basic " + new String(Base64.encodeBase64((USER + ":" + PASSWD).getBytes(StandardCharsets.ISO_8859_1)));

    public static final int SYNC_THREADS = Integer.parseInt(getEnv("JB_SYNC_THREADS", "128"));
    public static final int ASYNC_THREADS = Integer.parseInt(getEnv("JB_ASYNC_THREADS", "8"));
    public static final int MAX_PENDING_REQS_PER_THREAD = Integer.parseInt(getEnv("JB_MAX_PENDING_REQS_PER_THREAD", "128"));

    public static final String SCHEME = getEnv("JB_SCHEME", "http");
    public static final String HOST = getEnv("JB_HOST", "192.168.99.10");
    public static final int PORT = Integer.parseInt(getEnv("JB_PORT", "8080"));
    public static final String PATH = getEnv("JB_PATH", "/_api/version?details=true");
    public static final String URL = SCHEME + "://" + HOST + ":" + PORT + PATH;

    private final CountDownLatch completed = new CountDownLatch(1);
    private volatile Long startTime = null;
    private volatile Long endTime = null;
    private volatile int targetCount = Integer.MAX_VALUE;
    private final AtomicInteger counter = new AtomicInteger();

    private static Map<String, Function<HttpProtocolVersion, AbstractBenchmark>> instantiatorMap = Map.of(
            "HttpClient4", HttpClient4Benchmark::new,
            "Vertx", VertxBenchmark::new,
            "VertxSync", VertxSyncBenchmark::new
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

    private static String getEnv(String name, String defaultValue) {
        String v = System.getenv(name);
        if (v != null)
            return v;
        else
            return defaultValue;
    }

}
