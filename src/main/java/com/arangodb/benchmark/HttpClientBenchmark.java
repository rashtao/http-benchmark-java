package com.arangodb.benchmark;

import org.apache.commons.codec.binary.Base64;
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
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpClientBenchmark extends AbstractBenchmark {
    private final Header authHeader;
    private final int nThreads;
    private final ExecutorService es;
    private final CloseableHttpClient client;

    public HttpClientBenchmark(HttpProtocolVersion httpProtocolVersion) {
        super(httpProtocolVersion);
        if (!HttpProtocolVersion.HTTP11.equals(httpProtocolVersion)) {
            throw new IllegalArgumentException();
        }

        // authHeader
        String authString = HttpHeaders.AUTHORIZATION + ": Basic " + new String(Base64.encodeBase64("root:test".getBytes(StandardCharsets.ISO_8859_1)));
        char[] authChars = authString.toCharArray();
        CharArrayBuffer b = new CharArrayBuffer(authChars.length);
        b.append(authChars, 0, authChars.length);
        authHeader = BufferedHeader.create(b);

        // init
        nThreads = 32;
        es = Executors.newFixedThreadPool(nThreads);
        client = createClient(nThreads);
    }

    @Override
    protected void start() {
        for (int i = 0; i < nThreads; i++) {
            es.execute(() -> {
                HttpUriRequest request = new HttpGet("http://127.0.0.1:8529/_api/version");
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
