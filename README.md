# http-benchmark-java
Benchmarks of some Java http clients

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
| JB_SYNC_THREADS                 | `64`            |
| JB_ASYNC_THREADS                | `8`             |
| JB_MAX_PENDING_REQS_PER_THREAD  | `128`           |
| JB_SCHEME                       | `https`         |
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
| `Vertx`            | `H2`          |


## build docker image

Build docker image named `http-benchmark-java`:

```shell
mvn compile jib:dockerBuild
```


## results

req/s avg throughput for 1_000_000 `GET http://127.0.0.1:8529/_api/version` after 10s warmup:

```text
------------------------------------------------------------------------------------
|VertxBenchmark                          |HTTP11    |43415     |
|VertxBenchmark                          |H2        |62480     |
------------------------------------------------------------------------------------
```

req/s avg throughput for 1_000_000 `GET http://127.0.0.1:8529/_api/version?details=true` after 10s warmup:

```text
------------------------------------------------------------------------------------
|VertxBenchmark                          |HTTP11    |38483     |
|VertxBenchmark                          |H2        |42416     |
------------------------------------------------------------------------------------
```

### remote host (LAN, ping < 1ms)

req/s avg throughput for 1_000_000 `GET /_api/version` after 10s warmup:

```text
------------------------------------------------------------------------------------
|VertxBenchmark                          |HTTP11    |120743   |
|VertxBenchmark                          |H2        |96283    |
------------------------------------------------------------------------------------
```


## h2load

### /_api/version http/1.1
```text
$ h2load --h1 -t 4 -n 1000000 -c 32 -H "Authorization: Basic cm9vdDp0ZXN0" https://127.0.0.1:8529/_api/version          

finished in 15.98s, 62560.08 req/s, 13.19MB/s
requests: 1000000 total, 1000000 started, 1000000 done, 1000000 succeeded, 0 failed, 0 errored, 0 timeout
status codes: 1000000 2xx, 0 3xx, 0 4xx, 0 5xx
traffic: 210.76MB (221000000) total, 116.35MB (122000000) headers (space savings 0.00%), 57.22MB (60000000) data
                     min         max         mean         sd        +/- sd
time for request:       67us     18.45ms       503us       572us    95.03%
time for connect:     3.67ms     17.87ms      9.83ms      4.22ms    65.63%
time to 1st byte:    10.86ms     18.86ms     14.03ms      2.55ms    59.38%
req/s           :    1955.05     2062.42     1983.55       26.67    59.38%
```

### /_api/version h2
```text
$ h2load -t 4 -n 1000000 -c 32 -H "Authorization: Basic cm9vdDp0ZXN0" https://127.0.0.1:8529/_api/version

finished in 13.79s, 72524.67 req/s, 6.02MB/s
requests: 1000000 total, 1000000 started, 1000000 done, 1000000 succeeded, 0 failed, 0 errored, 0 timeout
status codes: 1000000 2xx, 0 3xx, 0 4xx, 0 5xx
traffic: 82.97MB (87003040) total, 8.58MB (9001696) headers (space savings 91.96%), 57.22MB (60000000) data
                     min         max         mean         sd        +/- sd
time for request:       55us     16.07ms       421us       506us    95.79%
time for connect:     3.16ms     18.34ms      9.81ms      4.44ms    62.50%
time to 1st byte:     9.27ms     19.43ms     13.39ms      3.12ms    46.88%
req/s           :    2266.47     2529.91     2338.59       72.36    84.38%
```

### /_api/version?details=true http/1.1
```text
$ h2load --h1 -t 4 -n 1000000 -c 32 -H "Authorization: Basic cm9vdDp0ZXN0" https://127.0.0.1:8529/_api/version?details=true

finished in 20.91s, 47815.21 req/s, 79.62MB/s
requests: 1000000 total, 1000000 started, 1000000 done, 1000000 succeeded, 0 failed, 0 errored, 0 timeout
status codes: 1000000 2xx, 0 3xx, 0 4xx, 0 5xx
traffic: 1.63GB (1746000000) total, 118.26MB (124000000) headers (space savings 0.00%), 1.47GB (1583000000) data
                     min         max         mean         sd        +/- sd
time for request:       91us     16.58ms       662us       582us    92.63%
time for connect:     3.39ms     17.93ms      9.35ms      4.30ms    59.38%
time to 1st byte:     9.81ms     19.03ms     13.68ms      2.71ms    68.75%
req/s           :    1494.24     1533.63     1506.38        9.62    56.25%
```

### /_api/version?details=true h2

```text
$ h2load -t 4 -n 1000000 -c 32 -H "Authorization: Basic cm9vdDp0ZXN0" https://127.0.0.1:8529/_api/version?details=true

finished in 19.84s, 50414.72 req/s, 77.46MB/s
requests: 1000000 total, 1000000 started, 1000000 done, 1000000 succeeded, 0 failed, 0 errored, 0 timeout
status codes: 1000000 2xx, 0 3xx, 0 4xx, 0 5xx
traffic: 1.50GB (1611003040) total, 9.54MB (10001696) headers (space savings 91.23%), 1.47GB (1583000000) data
                     min         max         mean         sd        +/- sd
time for request:       87us     19.28ms       620us       578us    93.54%
time for connect:     4.13ms     19.37ms     10.18ms      3.91ms    65.63%
time to 1st byte:    12.73ms     20.78ms     16.61ms      2.66ms    53.13%
req/s           :    1575.51     1627.62     1592.40       15.47    65.63%
```