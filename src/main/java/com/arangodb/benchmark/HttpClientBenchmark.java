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
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpClientBenchmark {

    public static void main(String[] args) {
        String authString = HttpHeaders.AUTHORIZATION + ": Basic " + new String(Base64.encodeBase64("root:test".getBytes(StandardCharsets.ISO_8859_1)));
        char[] authChars = authString.toCharArray();
        CharArrayBuffer b = new CharArrayBuffer(authChars.length);
        b.append(authChars, 0, authChars.length);
        Header authHeader = BufferedHeader.create(b);

        AtomicInteger counter = new AtomicInteger();
        int nThreads = 32;
        ExecutorService es = Executors.newFixedThreadPool(nThreads);
        CloseableHttpClient client = createClient(nThreads);

        for (int i = 0; i < nThreads; i++) {
            es.execute(() -> {
                HttpUriRequest request = new HttpGet("http://127.0.0.1:8529/_api/version");
                request.setHeader(authHeader);
                int j = 0;
                while (true) {
                    try {
                        CloseableHttpResponse response = client.execute(request);
                        EntityUtils.consume(response.getEntity());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    j++;
                    if (j % 1_000 == 0) {
                        counter.incrementAndGet();
                    }
                }
            });
        }

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

    private static CloseableHttpClient createClient(int connections) {
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