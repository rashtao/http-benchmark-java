package com.arangodb.benchmark;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpClient4Benchmark extends AbstractBenchmark {
    private final Header authHeader;
    private final int nThreads;
    private final ExecutorService es;
    private final CloseableHttpClient client;

    public HttpClient4Benchmark(HttpProtocolVersion httpVersion) {
        if (!HttpProtocolVersion.HTTP11.equals(httpVersion)) {
            throw new IllegalArgumentException();
        }

        // authHeader
        String authString = HttpHeaders.AUTHORIZATION + ": " + AUTH_HEADER;
        char[] authChars = authString.toCharArray();
        CharArrayBuffer b = new CharArrayBuffer(authChars.length);
        b.append(authChars, 0, authChars.length);
        authHeader = new BufferedHeader(b);

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
