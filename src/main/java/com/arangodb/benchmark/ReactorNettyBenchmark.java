package com.arangodb.benchmark;

import io.netty.handler.codec.http.HttpHeaderNames;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.function.Consumer;

public class ReactorNettyBenchmark extends AbstractBenchmark {

    private final HttpProtocolVersion httpVersion;

    public ReactorNettyBenchmark(HttpProtocolVersion httpVersion) {
        this.httpVersion = httpVersion;
        if (httpVersion != HttpProtocolVersion.HTTP11) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < ASYNC_THREADS; i++) {
            new ReactorNettyClient(HttpProtocol.HTTP11, MAX_PENDING_REQS_PER_THREAD, this);
        }
    }

    @Override
    public HttpProtocolVersion getHttpVersion() {
        return httpVersion;
    }

    @Override
    protected void start() {
    }

    @Override
    protected void shutdown() {
    }

}

class ReactorNettyClient {
    private final AbstractBenchmark benchmark;
    private final int maxPendingRequests;
    private final HttpProtocol protocol;
    private final HttpClient client;
    private final Consumer<String> cb = createCb();
    private final Scheduler scheduler;

    public ReactorNettyClient(HttpProtocol protocol, int maxPendingRequests, AbstractBenchmark benchmark) {
        this.benchmark = benchmark;
        this.protocol = protocol;
        this.maxPendingRequests = maxPendingRequests;
        scheduler = Schedulers.newSingle("consumer", true);
        client = createClient();
        for (int i = 0; i < maxPendingRequests; i++) {
            sendReq();
        }
    }

    private void sendReq() {
        client.get()
                .uri(AbstractBenchmark.PATH)
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
                .baseUrl(AbstractBenchmark.SCHEME + "://" + AbstractBenchmark.HOST + ":" + AbstractBenchmark.PORT)
                .headers(headers -> headers.set(HttpHeaderNames.AUTHORIZATION, AbstractBenchmark.AUTH_HEADER));
    }

    private Consumer<String> createCb() {
        return httpClientResponse -> {
            if (benchmark.success(1))
                sendReq();
        };
    }
}