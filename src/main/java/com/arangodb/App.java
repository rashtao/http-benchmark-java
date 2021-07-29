package com.arangodb;

import com.arangodb.benchmark.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class App {

    public static void main(String[] args) {
        List<Result> results = Arrays.asList(
                        new VertxBenchmark(HttpProtocolVersion.HTTP11),
                        new VertxBenchmark(HttpProtocolVersion.H2C),
                        new HttpClient4Benchmark(HttpProtocolVersion.HTTP11),
                        new HttpClient5Benchmark(HttpProtocolVersion.HTTP11),
                        new HttpClient5AsyncBenchmark(HttpProtocolVersion.HTTP11),
                        new HttpClient5AsyncBenchmark(HttpProtocolVersion.H2C)
                ).stream()
                .peek(App::runBenchmark)
                .map(it -> new Result(it.getClass().getSimpleName(), it.getHttpVersion(), it.getThroughput()))
                .collect(Collectors.toList());

        System.out.println("------------------------------------------------------------------------------------");
        results.forEach(Result::print);
        System.out.println("------------------------------------------------------------------------------------");
    }

    private static void runBenchmark(AbstractBenchmark b) {
        System.out.println("---");
        System.out.println(b.getClass().getSimpleName() + " " + b.getHttpVersion());

        // warmup
        b.startBenchmark();

        // start monitor / warmup
        b.startMonitor(10);

        // start benchmark
        b.startMeasuring(1_000_000);
        System.out.println(b.waitComplete());
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