#!/bin/sh -x
dir=$(cd `dirname $0`;pwd)
echo $dir
mvn clean package && \
java -javaagent:$dir/simple-before-jvm-agent/target/simple-before-jvm-agent-jar-with-dependencies.jar=text-args \
-jar $dir/target-java/target/target-java.jar
