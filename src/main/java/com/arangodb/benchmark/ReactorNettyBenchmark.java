package com.arangodb.benchmark;

import io.netty.handler.codec.http.HttpHeaderNames;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.function.Consumer;

public class ReactorNettyBenchmark extends AbstractBenchmark {

    private final HttpProtocol protocol;
    private final int maxPendingRequests = MAX_PENDING_REQS_PER_THREAD * ASYNC_THREADS;
    private final HttpClient client;
    private final Consumer<String> cb = createCb();
    private final HttpProtocolVersion httpVersion;
    private final Scheduler scheduler = Schedulers.single();

    public ReactorNettyBenchmark(HttpProtocolVersion httpVersion) {
        this.httpVersion = httpVersion;
        switch (httpVersion) {
            case HTTP11:
                protocol = HttpProtocol.HTTP11;
                break;
            case H2C:
                protocol = HttpProtocol.H2C;
                break;
            default:
                throw new IllegalArgumentException();
        }
        client = createClient();
    }

    @Override
    public HttpProtocolVersion getHttpVersion() {
        return httpVersion;
    }

    @Override
    protected void start() {
        for (int i = 0; i < maxPendingRequests; i++) {
            sendReq();
        }
    }

    @Override
    protected void shutdown() {
    }

    private void sendReq() {
        client.get()
                .uri(PATH)
                .responseContent()
                .asString()
                .subscribeOn(scheduler)
                .subscribe(cb, Throwable::printStackTrace);
    }

    private HttpClient createClient() {
        return HttpClient.create(ConnectionProvider
                        .builder("fixed")
                        .maxConnections(maxPendingRequests)
                        .build())
                .protocol(protocol)
                .keepAlive(true)
                .baseUrl(SCHEME + "://" + HOST + ":" + PORT)
                .headers(headers -> headers.set(HttpHeaderNames.AUTHORIZATION, AUTH_HEADER));
    }

    private Consumer<String> createCb() {
        return httpClientResponse -> {
            if (success(1))
                sendReq();
        };
    }

}
