# http-benchmark-java
Benchmarks of some Java http clients

## results

req/s avg throughput for 1_000_000 `GET http://127.0.0.1:8529/_api/version` after 10s warmup:

```text
------------------------------------------------------------------------------------
|VertxBenchmark                          |HTTP11    |95102     |
|VertxBenchmark                          |H2C       |86888     |
|HttpClient4Benchmark                    |HTTP11    |70338     |
|HttpClient5Benchmark                    |HTTP11    |62992     |
|HttpClient5AsyncBenchmark               |HTTP11    |56401     |
|HttpClient5AsyncBenchmark               |H2C       |56404     |
------------------------------------------------------------------------------------
```