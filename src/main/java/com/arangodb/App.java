package com.arangodb;

import com.arangodb.benchmark.AbstractBenchmark;
import com.arangodb.benchmark.HttpProtocolVersion;

public class App {

    public static void main(String[] args) {
        String client = System.getenv("JB_CLIENT");
        String protocol = System.getenv("JB_PROTOCOL");
        AbstractBenchmark b = AbstractBenchmark.of(client, protocol);
        runBenchmark(b);
        Result res = new Result(b.getClass().getSimpleName(), b.getHttpVersion(), b.getThroughput());
        System.out.println("------------------------------------------------------------------------------------");
        res.print();
        System.out.println("------------------------------------------------------------------------------------");
    }

    private static void runBenchmark(AbstractBenchmark b) {
        System.out.println(b.getClass().getSimpleName() + " " + b.getHttpVersion());
        int warmupDuration = Integer.parseInt(getEnv("JB_WARMUP_DURATION", "10"));
        int numberOfRequests = Integer.parseInt(getEnv("JB_REQUESTS", "1000000"));

        // warmup
        b.startBenchmark();

        // start monitor / warmup
        b.startMonitor(warmupDuration);

        // start benchmark
        b.startMeasuring(numberOfRequests);
        System.out.println(b.waitComplete());
    }

    private static String getEnv(String name, String defaultValue) {
        String v = System.getenv(name);
        if (v != null)
            return v;
        else
            return defaultValue;
    }

}


class Result {
    private static String format = "|%1$-40s|%2$-10s|%3$-10s|\n";
    final String name;
    final HttpProtocolVersion httpVersion;
    final long thrpt;

    Result(String name, HttpProtocolVersion httpVersion, long thrpt) {
        this.name = name;
        this.httpVersion = httpVersion;
        this.thrpt = thrpt;
    }

    public void print() {
        System.out.format(format, name, httpVersion, thrpt);
    }
}