package com.arangodb.benchmark;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VertxSyncBenchmark extends AbstractBenchmark {
    private final ExecutorService es;
    private final HttpProtocolVersion httpVersion;
    private final List<WebClient> clients = new ArrayList<>();
    private final List<Vertx> vertxs = new ArrayList<>();
    private final UsernamePasswordCredentials auth;

    public VertxSyncBenchmark(HttpProtocolVersion httpVersion) {
        this.httpVersion = httpVersion;
        HttpVersion protocol;
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

        for (int i = 0; i < ASYNC_THREADS; i++) {
            var vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true).setEventLoopPoolSize(1));
            var client = WebClient.create(vertx, new WebClientOptions()
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
            vertxs.add(vertx);
            clients.add(client);
        }

        es = Executors.newFixedThreadPool(SYNC_THREADS);
        auth = new UsernamePasswordCredentials(AbstractBenchmark.USER, AbstractBenchmark.PASSWD);
    }

    @Override
    public HttpProtocolVersion getHttpVersion() {
        return httpVersion;
    }

    @Override
    protected void start() {
        for (int i = 0; i < SYNC_THREADS; i++) {
            int finalI = i;
            es.execute(() -> {
                var client = clients.get(finalI % ASYNC_THREADS);
                boolean more = true;
                while (more) {
                    sendReq(client);
                    more = success(1);
                }
            });
        }
    }

    @Override
    protected void shutdown() {
        clients.forEach(WebClient::close);
        vertxs.forEach(Vertx::close);
        es.shutdown();
    }

    private void sendReq(WebClient client) {
        try {
            client
                    .get(AbstractBenchmark.PATH)
                    .authentication(auth)
                    .send().toCompletionStage().toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
