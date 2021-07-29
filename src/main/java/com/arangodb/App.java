package com.arangodb;

import com.arangodb.benchmark.AbstractBenchmark;
import com.arangodb.benchmark.HttpClientBenchmark;
import com.arangodb.benchmark.HttpProtocolVersion;
import com.arangodb.benchmark.VertxBenchmark;

public class App {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("---");
        System.out.println("Vertx HTTP11");
        runBenchmark(new VertxBenchmark(HttpProtocolVersion.HTTP11));

        System.out.println("---");
        System.out.println("Vertx H2C");
        runBenchmark(new VertxBenchmark(HttpProtocolVersion.H2C));

        System.out.println("---");
        System.out.println("HttpClient HTTP11");
        runBenchmark(new HttpClientBenchmark(HttpProtocolVersion.HTTP11));

    }

    private static void runBenchmark(AbstractBenchmark b) throws InterruptedException {
        // warmup
        b.startBenchmark();

        // start monitor / warmup
        b.startMonitor(10);

        // start benchmark
        b.startMeasuring(1_000_000);
        System.out.println(b.waitComplete());
    }

}
