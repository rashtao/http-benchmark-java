package com.arangodb.benchmark;

import io.vertx.core.Vertx;

public class VertxBenchmark extends AbstractBenchmark {

    private final int nThreads = ASYNC_THREADS;
    private final int maxPendingRequestsPerThread = MAX_PENDING_REQS_PER_THREAD;
    private final Vertx vertx = Vertx.vertx();
    private final HttpProtocolVersion httpVersion;

    public VertxBenchmark(HttpProtocolVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    @Override
    protected void start() {
        for (int i = 0; i < nThreads; i++) {
            vertx.deployVerticle(new VertxClient(maxPendingRequestsPerThread, this, httpVersion));
        }
    }

    @Override
    protected void shutdown() {
        vertx.close().toCompletionStage().toCompletableFuture().join();
    }

    @Override
    public HttpProtocolVersion getHttpVersion() {
        return httpVersion;
    }
}
