package com.arangodb;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;

public class VertxServer extends AbstractVerticle {

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        final String req = "{\"server\":\"arango\",\"license\":\"community\",\"version\":\"3.7.13\"}";

        server.requestHandler(request -> {
            HttpServerResponse response = request.response();
            response.putHeader("content-type", "application/json");
            response.end(req);
        });

        server.listen(8080);
    }

}
