package com.arangodb.benchmark;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BufferedHeader;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpClient5Benchmark extends AbstractBenchmark {
    private final Header authHeader;
    private final int nThreads;
    private final ExecutorService es;
    private final CloseableHttpClient client;

    public HttpClient5Benchmark(HttpProtocolVersion httpVersion) {
        if (!HttpProtocolVersion.HTTP11.equals(httpVersion)) {
            throw new IllegalArgumentException();
        }

        // authHeader
        String authString = HttpHeaders.AUTHORIZATION + ": " + AUTH_HEADER;
        char[] authChars = authString.toCharArray();
        CharArrayBuffer b = new CharArrayBuffer(authChars.length);
        b.append(authChars, 0, authChars.length);
        authHeader = BufferedHeader.create(b);

        // init
        nThreads = SYNC_THREADS;
        es = Executors.newFixedThreadPool(nThreads);
        client = createClient(nThreads);
    }

    @Override
    public HttpProtocolVersion getHttpVersion() {
        return HttpProtocolVersion.HTTP11;
    }

    @Override
    protected void start() {
        for (int i = 0; i < nThreads; i++) {
            es.execute(() -> {
                HttpUriRequest request = new HttpGet(URL);
                request.setHeader(authHeader);
                boolean more = true;
                while (more) {
                    try {
                        CloseableHttpResponse response = client.execute(request);
                        EntityUtils.consume(response.getEntity());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    more = success(1);
                }
            });
        }
    }

    @Override
    protected void shutdown() {
        es.shutdown();
    }

    private CloseableHttpClient createClient(int connections) {
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .setTcpNoDelay(true)
                .setSoKeepAlive(true)
                .setSoReuseAddress(true)
                .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(connections);
        cm.setDefaultMaxPerRoute(connections);
        cm.setDefaultSocketConfig(socketConfig);
        return HttpClientBuilder.create()
                .setConnectionManager(cm)
                .build();
    }

}
