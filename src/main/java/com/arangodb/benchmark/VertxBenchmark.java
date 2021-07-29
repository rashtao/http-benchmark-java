package com.arangodb.benchmark;

import io.vertx.core.Vertx;

public class VertxBenchmark extends AbstractBenchmark {

    private final int nThreads = 4;
    private final Vertx vertx = Vertx.vertx();

    public VertxBenchmark(HttpProtocolVersion httpProtocolVersion) {
        super(httpProtocolVersion);
    }

    @Override
    protected void start() {
        for (int i = 0; i < nThreads; i++) {
            vertx.deployVerticle(new VertxClient(8 * nThreads, this));
        }
    }

    @Override
    protected void shutdown() {
        vertx.close().toCompletionStage().toCompletableFuture().join();
    }
}
