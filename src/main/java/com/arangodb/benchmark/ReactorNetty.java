package com.arangodb.benchmark;

import io.netty.handler.codec.http.HttpHeaderNames;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ReactorNetty {

    static public String getHttpAuthorizationHeader() {
        final String plainAuth = "root:test";
        final String encodedAuth = Base64.getEncoder().encodeToString(plainAuth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedAuth;
    }

    static public String doRequest(HttpClient client) {
        return client.get()
                .uri("/_api/version")
                .responseContent()
                .aggregate()
                .asString()
                .block();
    }

    public static void main(String[] args) {
        HttpClient client = HttpClient.create()
//                .wiretap(true)
//                .protocol(HttpProtocol.H2C)
                .protocol(HttpProtocol.HTTP11)
                .host("127.0.0.1")
                .port(8529)
                .headers(h -> h.set(HttpHeaderNames.AUTHORIZATION, getHttpAuthorizationHeader()));
        client.warmup().block();

        AtomicInteger counter = new AtomicInteger();
        int nThreads = 32;
        ExecutorService es = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            es.execute(() -> {
                int j = 0;
                while (true) {
                    doRequest(client);
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
}
