#!/bin/bash

mvn --version
mvn clean package

#export JB_PATH="/_api/version?details=true"

export JB_CLIENT=Vertx
export JB_PROTOCOL=HTTP11
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar

export JB_CLIENT=Vertx
export JB_PROTOCOL=H2
java -jar ./target/http-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar
