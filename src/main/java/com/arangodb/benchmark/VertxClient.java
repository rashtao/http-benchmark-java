package com.arangodb.benchmark;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

public class VertxClient {

    private final AbstractBenchmark benchmark;
    private final WebClient client;
    private final UsernamePasswordCredentials auth;

    public VertxClient(int maxPendingRequestsPerThread, AbstractBenchmark benchmark, WebClient client) {
        this.benchmark = benchmark;
        this.client = client;
        auth = new UsernamePasswordCredentials(AbstractBenchmark.USER, AbstractBenchmark.PASSWD);
        for (int i = 0; i < maxPendingRequestsPerThread; i++) {
            sendReq();
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

}
