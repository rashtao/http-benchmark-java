# http-benchmark-java
Benchmarks of some Java http clients

## start server

```shell
sudo arangod --server.io-threads 4
```

## start server ssl

```shell
sudo arangod --ssl.keyfile ./server.pem --server.endpoint ssl://0.0.0.0:8529 --server.io-threads 4
```

## run

```shell
./run.sh
```

## Environment variables

| Name                            | Default value   |
|---------------------------------|-----------------|
| JB_USER                         | `root`          |
| JB_PASSWD                       | `test`          |
| JB_SYNC_THREADS                 | `32`            |
| JB_ASYNC_THREADS                | `4`             |
| JB_MAX_PENDING_REQS_PER_THREAD  | `32`            |
| JB_SCHEME                       | `http`          |
| JB_HOST                         | `127.0.0.1`     |
| JB_PORT                         | `8529`          |
| JB_PATH                         | `/_api/version` |
| JB_WARMUP_DURATION              | `10`            |
| JB_REQUESTS                     | `1000000`       |
| JB_PROTOCOL                     | `<mandatory>`   |
| JB_CLIENT                       | `<mandatory>`   |


The following combinations of `JB_CLIENT` and `JB_PROTOCOL` are allowed:

| `JB_CLIENT`        | `JB_PROTOCOL` | 
|--------------------|---------------|
| `Vertx`            | `HTTP11`      |
| `Vertx`            | `H2C`         |
| `HttpClient4`      | `HTTP11`      |
| `HttpClient5`      | `HTTP11`      |
| `HttpClient5Async` | `HTTP11`      |
| `HttpClient5Async` | `H2C`         |
| `AsyncHttpClient`  | `HTTP11`      |
| `ReactorNetty`     | `HTTP11`      |


## build docker image

Build docker image named `http-benchmark-java`:

```shell
mvn compile jib:dockerBuild
```


## results

req/s avg throughput for 1_000_000 `GET http://127.0.0.1:8529/_api/version` after 10s warmup:

```text
------------------------------------------------------------------------------------
|ReactorNettyBenchmark                   |HTTP11    |76155     |
|VertxBenchmark                          |HTTP11    |94688     |
|VertxBenchmark                          |H2C       |87176     |
|HttpClient4Benchmark                    |HTTP11    |73616     |
|HttpClient5Benchmark                    |HTTP11    |64825     |
|HttpClient5AsyncBenchmark               |HTTP11    |56487     |
|HttpClient5AsyncBenchmark               |H2C       |56570     |
|AsyncHttpClientBenchmark                |HTTP11    |89198     |
------------------------------------------------------------------------------------
```

req/s avg throughput for 1_000_000 `GET http://127.0.0.1:8529/_api/version?details=true` after 10s warmup:

```text
------------------------------------------------------------------------------------
|ReactorNettyBenchmark                   |HTTP11    |55377     |
|VertxBenchmark                          |HTTP11    |55956     |
|VertxBenchmark                          |H2C       |59499     |
|HttpClient4Benchmark                    |HTTP11    |52197     |
|HttpClient5Benchmark                    |HTTP11    |48952     |
|HttpClient5AsyncBenchmark               |HTTP11    |46339     |
|HttpClient5AsyncBenchmark               |H2C       |46270     |
|AsyncHttpClientBenchmark                |HTTP11    |60350     |
------------------------------------------------------------------------------------
```

### remote host (LAN, ping < 1ms)

req/s avg throughput for 1_000_000 `GET /_api/version` after 10s warmup:

```text
------------------------------------------------------------------------------------
|ReactorNettyBenchmark                   |HTTP11    |121109    |
|VertxBenchmark                          |HTTP11    |187722    |
|VertxBenchmark                          |H2C       |155448    |
|HttpClient4Benchmark                    |HTTP11    |101832    |
|HttpClient5Benchmark                    |HTTP11    |83465     |
|HttpClient5AsyncBenchmark               |HTTP11    |83416     |
|HttpClient5AsyncBenchmark               |H2C       |81652     |
|AsyncHttpClientBenchmark                |HTTP11    |132837    |
------------------------------------------------------------------------------------
```


## h2load

### /_api/version http/1.1
```text
$ h2load --h1 -t 4 -n 1000000 -c 32 -H "Authorization: Basic cm9vdDp0ZXN0" http://127.0.0.1:8529/_api/version

finished in 6.34s, 157801.43 req/s, 33.26MB/s
requests: 1000000 total, 1000000 started, 1000000 done, 1000000 succeeded, 0 failed, 0 errored, 0 timeout
status codes: 1000000 2xx, 0 3xx, 0 4xx, 0 5xx
traffic: 210.76MB (221000000) total, 116.35MB (122000000) headers (space savings 0.00%), 57.22MB (60000000) data
                     min         max         mean         sd        +/- sd
time for request:       32us     20.13ms       193us       398us    97.54%
time for connect:      186us      1.83ms       842us       452us    59.38%
time to 1st byte:     1.50ms      4.91ms      2.93ms       881us    65.63%
req/s           :    4931.50     5794.12     5155.12      190.94    59.38%
```

### /_api/version h2c
```text
$ h2load -t 4 -n 1000000 -c 32 -H "Authorization: Basic cm9vdDp0ZXN0" http://127.0.0.1:8529/_api/version

finished in 8.25s, 121226.86 req/s, 10.06MB/s
requests: 1000000 total, 1000000 started, 1000000 done, 1000000 succeeded, 0 failed, 0 errored, 0 timeout
status codes: 1000000 2xx, 0 3xx, 0 4xx, 0 5xx
traffic: 82.97MB (87003040) total, 8.58MB (9001696) headers (space savings 91.96%), 57.22MB (60000000) data
                     min         max         mean         sd        +/- sd
time for request:       35us     17.62ms       255us       327us    97.08%
time for connect:       55us       507us       241us       131us    62.50%
time to 1st byte:      497us      2.06ms      1.10ms       373us    65.63%
req/s           :    3788.50     4030.80     3858.16       60.17    81.25%
```

### /_api/version?details=true http/1.1
```text
$ h2load -t 4 -n 1000000 -c 32 -H "Authorization: Basic cm9vdDp0ZXN0" http://127.0.0.1:8529/_api/version?details=true

finished in 14.04s, 71222.68 req/s, 109.42MB/s
requests: 1000000 total, 1000000 started, 1000000 done, 1000000 succeeded, 0 failed, 0 errored, 0 timeout
status codes: 1000000 2xx, 0 3xx, 0 4xx, 0 5xx
traffic: 1.50GB (1611003040) total, 9.54MB (10001696) headers (space savings 91.23%), 1.47GB (1583000000) data
                     min         max         mean         sd        +/- sd
time for request:       65us     15.57ms       440us       362us    93.51%
time for connect:       64us       534us       258us       137us    62.50%
time to 1st byte:      560us      2.05ms      1.39ms       384us    71.88%
req/s           :    2225.73     2267.22     2248.45       12.20    65.63%
```

### /_api/version?details=true h2c

```text
$ h2load -t 4 -n 1000000 -c 32 -H "Authorization: Basic cm9vdDp0ZXN0" http://127.0.0.1:8529/_api/version?details=true

finished in 13.79s, 72530.58 req/s, 111.43MB/s
requests: 1000000 total, 1000000 started, 1000000 done, 1000000 succeeded, 0 failed, 0 errored, 0 timeout
status codes: 1000000 2xx, 0 3xx, 0 4xx, 0 5xx
traffic: 1.50GB (1611003040) total, 9.54MB (10001696) headers (space savings 91.23%), 1.47GB (1583000000) data
                     min         max         mean         sd        +/- sd
time for request:       56us     13.49ms       433us       367us    93.60%
time for connect:       58us       611us       282us       174us    62.50%
time to 1st byte:      569us      1.83ms      1.22ms       353us    53.13%
req/s           :    2266.61     2353.32     2285.28       21.96    71.88%
```