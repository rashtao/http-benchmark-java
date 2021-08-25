package com.arangodb.benchmark;

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

import java.io.IOException;

public class HttpClient5AsyncBenchmark extends AbstractBenchmark {

    private final HttpVersionPolicy protocol;
    private final int nThreads = ASYNC_THREADS;
    private final int maxPendingRequests = MAX_PENDING_REQS_PER_THREAD * nThreads;
    private final CloseableHttpAsyncClient client = createClient();
    private final Header authHeader;
    private final FutureCallback<SimpleHttpResponse> cb = createCb();
    private final HttpProtocolVersion httpVersion;

    public HttpClient5AsyncBenchmark(HttpProtocolVersion httpVersion) {
        this.httpVersion = httpVersion;
        switch (httpVersion) {
            case HTTP11:
                protocol = HttpVersionPolicy.FORCE_HTTP_1;
                break;
            case H2:
                protocol = HttpVersionPolicy.FORCE_HTTP_2;
                break;
            default:
                throw new IllegalArgumentException();
        }

        // authHeader
        String authString = HttpHeaders.AUTHORIZATION + ": " + AUTH_HEADER;
        char[] authChars = authString.toCharArray();
        CharArrayBuffer b = new CharArrayBuffer(authChars.length);
        b.append(authChars, 0, authChars.length);
        authHeader = BufferedHeader.create(b);
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
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendReq() {
        final SimpleHttpRequest request = SimpleRequestBuilder.get(URL).build();
        request.setHeader(authHeader);
        client.execute(
                SimpleRequestProducer.create(request),
                SimpleResponseConsumer.create(),
                cb);
    }

    private CloseableHttpAsyncClient createClient() {

        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(nThreads * 2)
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

    private FutureCallback<SimpleHttpResponse> createCb() {
        return new FutureCallback<>() {
            @Override
            public void completed(final SimpleHttpResponse response) {
                if (success(1))
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

}
