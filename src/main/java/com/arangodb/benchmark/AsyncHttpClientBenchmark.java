package com.arangodb.benchmark;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.*;

import java.io.IOException;

public class AsyncHttpClientBenchmark extends AbstractBenchmark {

    private final int nThreads = ASYNC_THREADS;
    private final int maxPendingRequests = MAX_PENDING_REQS_PER_THREAD * nThreads;
    private final AsyncHttpClient client = createClient();
    private final AsyncHandler<String> cb = createCb();
    private final HttpProtocolVersion httpVersion;

    public AsyncHttpClientBenchmark(HttpProtocolVersion httpVersion) {
        if (!HttpProtocolVersion.HTTP11.equals(httpVersion)) {
            throw new IllegalArgumentException();
        }
        this.httpVersion = httpVersion;
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
        client.prepareGet(URL)
                .setHeader(HttpHeaderNames.AUTHORIZATION, AUTH_HEADER)
                .execute(cb);
    }

    private AsyncHttpClient createClient() {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setIoThreadsCount(nThreads)
                .setKeepAlive(true);
        return Dsl.asyncHttpClient(clientBuilder);
    }

    private AsyncHandler<String> createCb() {
        return new AsyncHandler<>() {
            @Override
            public State onStatusReceived(HttpResponseStatus httpResponseStatus) throws Exception {
                return null;
            }

            @Override
            public State onHeadersReceived(HttpHeaders httpHeaders) throws Exception {
                return null;
            }

            @Override
            public State onBodyPartReceived(HttpResponseBodyPart httpResponseBodyPart) throws Exception {
                return null;
            }

            @Override
            public void onThrowable(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public String onCompleted() throws Exception {
                if (success(1))
                    sendReq();
                return null;
            }
        };
    }

}
