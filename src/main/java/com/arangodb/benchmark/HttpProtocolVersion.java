package com.arangodb.benchmark;

public enum HttpProtocolVersion {
    HTTP11, H2;

    public static HttpProtocolVersion of(String v) {
        if (HTTP11.toString().equals(v)) {
            return HTTP11;
        } else if (H2.toString().equals(v)) {
            return H2;
        } else {
            throw new IllegalArgumentException(v);
        }
    }
}
