#!/bin/bash

mvn --version
mvn clean package

export JB_CLIENT=HttpClient4
export JB_PROTOCOL=HTTP11
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar

export JB_CLIENT=VertxSync
export JB_PROTOCOL=HTTP11
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar

export JB_CLIENT=VertxSync
export JB_PROTOCOL=H2C
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar

export JB_CLIENT=Vertx
export JB_PROTOCOL=HTTP11
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar

export JB_CLIENT=Vertx
export JB_PROTOCOL=H2C
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar
