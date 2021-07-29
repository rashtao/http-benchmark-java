package com.arangodb;

import com.arangodb.benchmark.HttpProtocolVersion;
import com.arangodb.benchmark.VertxBenchmark;

public class App {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("---");
        System.out.println("VertxBenchmark HTTP11");
        runBenchmark(new VertxBenchmark(HttpProtocolVersion.HTTP11));

        System.out.println("---");
        System.out.println("VertxBenchmark H2C");
        runBenchmark(new VertxBenchmark(HttpProtocolVersion.H2C));
    }

    private static void runBenchmark(VertxBenchmark b) throws InterruptedException {
        // warmup
        b.startBenchmark();

        // start monitor / warmup
        b.startMonitor(10);

        // start benchmark
        b.startMeasuring(1_000_000);
        System.out.println(b.waitComplete());
    }

}
