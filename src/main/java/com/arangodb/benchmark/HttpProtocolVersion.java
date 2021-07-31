package com.arangodb.benchmark;

public enum HttpProtocolVersion {
    HTTP11, H2C;

    public static HttpProtocolVersion of(String v) {
        if (HTTP11.toString().equals(v)) {
            return HTTP11;
        } else if (H2C.toString().equals(v)) {
            return H2C;
        } else {
            throw new IllegalArgumentException(v);
        }
    }
}
