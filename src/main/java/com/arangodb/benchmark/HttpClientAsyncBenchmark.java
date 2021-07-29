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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpClientAsyncBenchmark extends AbstractBenchmark {

    private final HttpVersionPolicy protocol;
    private final int nThreads = 4;
    private final int maxPendingRequests = 32 * nThreads;
    private final CloseableHttpAsyncClient client = createClient();
    private final Header authHeader;

    public HttpClientAsyncBenchmark(HttpProtocolVersion httpVersion) {
        switch (httpVersion) {
            case HTTP11:
                protocol = HttpVersionPolicy.FORCE_HTTP_1;
                break;
            case H2C:
                protocol = HttpVersionPolicy.FORCE_HTTP_2;
                break;
            default:
                throw new IllegalArgumentException();
        }

        // authHeader
        String authString = HttpHeaders.AUTHORIZATION + ": Basic " + new String(Base64.encodeBase64("root:test".getBytes(StandardCharsets.ISO_8859_1)));
        char[] authChars = authString.toCharArray();
        CharArrayBuffer b = new CharArrayBuffer(authChars.length);
        b.append(authChars, 0, authChars.length);
        authHeader = BufferedHeader.create(b);

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
        final SimpleHttpRequest request = SimpleRequestBuilder.get("http://127.0.0.1:8529/_api/version").build();
        request.setHeader(authHeader);
        client.execute(
                SimpleRequestProducer.create(request),
                SimpleResponseConsumer.create(),
                createCb());
    }

    private CloseableHttpAsyncClient createClient() {

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
