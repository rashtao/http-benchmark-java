package com.arangodb;

import io.vertx.core.Vertx;

public class ServerApp {
    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        for (int i = 0; i < 8; i++) {
            vertx.deployVerticle(new VertxServer());
        }
    }
}
