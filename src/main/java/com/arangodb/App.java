package com.arangodb;

import com.arangodb.benchmark.AbstractBenchmark;
import com.arangodb.benchmark.HttpProtocolVersion;

public class App {

    public static void main(String[] args) {
        AbstractBenchmark b = AbstractBenchmark.of(args[0], args[1]);
        runBenchmark(b);
        Result res = new Result(b.getClass().getSimpleName(), b.getHttpVersion(), b.getThroughput());
        System.out.println("------------------------------------------------------------------------------------");
        res.print();
        System.out.println("------------------------------------------------------------------------------------");
    }

    private static void runBenchmark(AbstractBenchmark b) {
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