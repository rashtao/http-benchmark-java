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
    private final UsernamePasswordCredentials auth;

    public VertxClient(int maxPendingRequestsPerThread, AbstractBenchmark benchmark, HttpProtocolVersion httpVersion) {
        this.maxPendingRequestsPerThread = maxPendingRequestsPerThread;
        this.benchmark = benchmark;
        auth = new UsernamePasswordCredentials(AbstractBenchmark.USER, AbstractBenchmark.PASSWD);

        switch (httpVersion) {
            case HTTP11:
                protocol = HttpVersion.HTTP_1_1;
                break;
            case H2:
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
                .get(AbstractBenchmark.PATH)
                .authentication(auth)
                .send(createCb());
    }

    @Override
    public void start() {
        client = WebClient.create(vertx, new WebClientOptions()
                .setKeepAlive(true)
                .setTcpKeepAlive(true)
                .setPipelining(true)
                .setHttp2MultiplexingLimit(maxPendingRequestsPerThread)
                .setReuseAddress(true)
                .setReusePort(true)
                .setHttp2ClearTextUpgrade(false)
                .setProtocolVersion(protocol)
                .setDefaultHost(AbstractBenchmark.HOST)
                .setDefaultPort(AbstractBenchmark.PORT)
                .setTrustAll(true)
                .setVerifyHost(false)
                .setUseAlpn(true)
                .setSsl(true)
        );
        for (int i = 0; i < maxPendingRequestsPerThread; i++) {
            sendReq();
        }
    }

}
