package com.arangodb.benchmark;

import org.apache.commons.codec.binary.Base64;
import org.apache.hc.client5.http.async.methods.*;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BufferedHeader;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.CharArrayBuffer;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncClientBenchmark {

    private static final HttpVersionPolicy protocol = HttpVersionPolicy.FORCE_HTTP_1;
//    private static final HttpVersionPolicy protocol = HttpVersionPolicy.FORCE_HTTP_2;
    private static final int nThreads = 4;
    private static final int maxPendingRequests = 32;
    private static final AtomicInteger pendingReqs = new AtomicInteger();
    private static final AtomicInteger counter = new AtomicInteger();
    private static final CloseableHttpAsyncClient client = createClient();
    private static final Header authHeader;

    static {
        String authString = HttpHeaders.AUTHORIZATION + ": Basic " + new String(Base64.encodeBase64("root:test".getBytes(StandardCharsets.ISO_8859_1)));
        char[] authChars = authString.toCharArray();
        CharArrayBuffer b = new CharArrayBuffer(authChars.length);
        b.append(authChars, 0, authChars.length);
        authHeader = BufferedHeader.create(b);
    }

    private static FutureCallback<SimpleHttpResponse> createCb() {
        return new FutureCallback<SimpleHttpResponse>() {
            @Override
            public void completed(final SimpleHttpResponse response) {
                counter.incrementAndGet();
                pendingReqs.decrementAndGet();
                sendReq();
            }

            @Override
            public void failed(final Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void cancelled() {
                System.err.println("cancelled");
            }
        };
    }

    public static void main(final String[] args) {
        startMonitor();
        for (int i = 0; i < maxPendingRequests; i++) {
            sendReq();
        }
    }

    private static void startMonitor() {
        new Thread(() -> {
            while (true) {
                long start = new Date().getTime();
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long current = new Date().getTime();
                long elapsed = current - start;
                double reqsPerSec = 1_000.0 * counter.get() / elapsed;
                counter.set(0);
                System.out.println("reqs/s: " + reqsPerSec);
            }
        }).start();
    }

    private static void sendReq() {
        if (pendingReqs.get() >= maxPendingRequests) return;
        pendingReqs.incrementAndGet();
        final SimpleHttpRequest request = SimpleRequestBuilder.get("http://127.0.0.1:8529/_api/version").build();
        request.setHeader(authHeader);
        client.execute(
                SimpleRequestProducer.create(request),
                SimpleResponseConsumer.create(),
                createCb());
    }

    private static CloseableHttpAsyncClient createClient() {

        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(nThreads)
                .build();

        PoolingAsyncClientConnectionManager cm = new PoolingAsyncClientConnectionManager();
        cm.setMaxTotal(maxPendingRequests);
        cm.setDefaultMaxPerRoute(maxPendingRequests);

        final CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setVersionPolicy(protocol)
                .setIOReactorConfig(ioReactorConfig)
                .setConnectionManager(cm)
                .build();
        httpclient.start();
        return httpclient;
    }

}
