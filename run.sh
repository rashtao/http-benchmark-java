#!/bin/bash

mvn --version
mvn clean package
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar Vertx HTTP11
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar Vertx H2C
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar HttpClient4 HTTP11
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar HttpClient5 HTTP11
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar HttpClient5Async HTTP11
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar HttpClient5Async H2C
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar AsyncHttpClient HTTP11
