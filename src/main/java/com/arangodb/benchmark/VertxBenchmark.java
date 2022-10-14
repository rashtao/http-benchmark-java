package com.arangodb.benchmark;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.ArrayList;
import java.util.List;

public class VertxBenchmark extends AbstractBenchmark {

    private final int nThreads = ASYNC_THREADS;
    private final int maxPendingRequestsPerThread = MAX_PENDING_REQS_PER_THREAD;
    private final List<Vertx> vertxes = new ArrayList<>();
    private final List<WebClient> clients = new ArrayList<>();
    private final HttpProtocolVersion httpVersion;
    private final HttpVersion protocol;


    public VertxBenchmark(HttpProtocolVersion httpVersion) {
        this.httpVersion = httpVersion;
        switch (httpVersion) {
            case HTTP11:
                protocol = HttpVersion.HTTP_1_1;
                break;
            case H2C:
                protocol = HttpVersion.HTTP_2;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    protected void start() {
        for (int i = 0; i < nThreads; i++) {
            Vertx vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true).setEventLoopPoolSize(1));
            int finalI = i;
            vertx.runOnContext(e -> Thread.currentThread().setName("adb-eventloop-" + finalI));
            WebClient client = WebClient.create(vertx, new WebClientOptions()
                    .setKeepAlive(true)
                    .setTcpKeepAlive(true)
                    .setPipelining(true)
                    .setReuseAddress(true)
                    .setReusePort(true)
                    .setHttp2ClearTextUpgrade(false)
                    .setProtocolVersion(protocol)
                    .setDefaultHost(AbstractBenchmark.HOST)
                    .setDefaultPort(AbstractBenchmark.PORT)
            );
            vertxes.add(vertx);
            clients.add(client);
            new VertxClient(maxPendingRequestsPerThread, this, client);
        }
    }

    @Override
    protected void shutdown() {
        clients.forEach(WebClient::close);
        vertxes.forEach(it -> it.close().toCompletionStage().toCompletableFuture().join());
    }

    @Override
    public HttpProtocolVersion getHttpVersion() {
        return httpVersion;
    }
}
