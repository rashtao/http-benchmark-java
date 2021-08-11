# http-benchmark-java
Benchmarks of some Java http clients

## start server

```shell
sudo arangod --server.io-threads 4
```

## run

```shell
./run.sh
```

## results

req/s avg throughput for 1_000_000 `GET http://127.0.0.1:8529/_api/version` after 10s warmup:

```text
------------------------------------------------------------------------------------
|VertxBenchmark                          |HTTP11    |96730     |
|VertxBenchmark                          |H2C       |83948     |
|HttpClient4Benchmark                    |HTTP11    |70686     |
|HttpClient5Benchmark                    |HTTP11    |61728     |
|HttpClient5AsyncBenchmark               |HTTP11    |55432     |
|HttpClient5AsyncBenchmark               |H2C       |54347     |
|AsyncHttpClientBenchmark                |HTTP11    |80664     |
------------------------------------------------------------------------------------
```

## h2load

### http/1.1
```text
$ h2load --h1 -t 4 -n 1000000 -c 32 -H "Authorization: Basic cm9vdDp0ZXN0" http://127.0.0.1:8529/_api/version
starting benchmark...
spawning thread #0: 8 total client(s). 250000 total requests
spawning thread #1: 8 total client(s). 250000 total requests
spawning thread #2: 8 total client(s). 250000 total requests
spawning thread #3: 8 total client(s). 250000 total requests
Application protocol: http/1.1
progress: 10% done
progress: 20% done
progress: 30% done
progress: 40% done
progress: 50% done
progress: 60% done
progress: 70% done
progress: 80% done
progress: 90% done
progress: 100% done

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

### h2c
```text
$ h2load -t 4 -n 1000000 -c 32 -H "Authorization: Basic cm9vdDp0ZXN0" http://127.0.0.1:8529/_api/version
starting benchmark...
spawning thread #0: 8 total client(s). 250000 total requests
spawning thread #1: 8 total client(s). 250000 total requests
spawning thread #2: 8 total client(s). 250000 total requests
spawning thread #3: 8 total client(s). 250000 total requests
Application protocol: h2c
progress: 10% done
progress: 20% done
progress: 30% done
progress: 40% done
progress: 50% done
progress: 60% done
progress: 70% done
progress: 80% done
progress: 90% done
progress: 100% done

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
