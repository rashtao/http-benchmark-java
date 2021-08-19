package com.arangodb.benchmark;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class VertxClient extends AbstractVerticle {

    private final HttpVersion protocol;
    private final int maxPendingRequestsPerThread;
    private final AbstractBenchmark benchmark;
    private WebClient client;
    private final UsernamePasswordCredentials auth = new UsernamePasswordCredentials("root", "test");
    private final int port;
    private final String host;
    private final String path;

    public VertxClient(int maxPendingRequestsPerThread, AbstractBenchmark benchmark, HttpProtocolVersion httpVersion,
    int port, String host, String path) {
        this.maxPendingRequestsPerThread = maxPendingRequestsPerThread;
        this.benchmark = benchmark;
        this.port = port;
        this.host = host;
        this.path = path;

        switch (httpVersion) {
            case HTTP11:
                protocol = HttpVersion.HTTP_1_1;
                break;
            case H2C:
                protocol = HttpVersion.HTTP_2;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private Handler<AsyncResult<HttpResponse<Buffer>>> createCb() {
        return (AsyncResult<HttpResponse<Buffer>> ar) -> {
            if (ar.succeeded()) {
                if (benchmark.success(1))
                    sendReq();
            } else {
                ar.cause().printStackTrace();
            }
        };
    }

    private void sendReq() {
        client
                .get(port, host, path)
                .authentication(auth)
                .send(createCb());
    }

    @Override
    public void start() throws Exception {
        client = WebClient.create(vertx, new WebClientOptions()
                .setLogActivity(true)
                .setKeepAlive(true)
                .setTcpKeepAlive(true)
                .setPipelining(true)
                .setReuseAddress(true)
                .setReusePort(true)
                .setHttp2ClearTextUpgrade(false)
                .setProtocolVersion(protocol)
                .setMaxPoolSize(4)
                .setHttp2MaxPoolSize(4)
        );
        for (int i = 0; i < maxPendingRequestsPerThread; i++) {
            sendReq();
        }
    }

}
