#!/bin/sh -x
dir=$(cd `dirname $0`;pwd)
echo $dir
mvn clean package && \
java -javaagent:$dir/simple-before-jvm-agent/target/simple-before-jvm-agent-jar-with-dependencies.jar=text-args \
-jar $dir/target-java/target/target-java.jar

## 简单的处理jvm 启动的时候附加 javaagent 参数进行启动 https://blog.csdn.net/qq_26761587/article/details/78817745
## 这里agent的断点调试可以的，在目标target 启动的时候，先编译agent-jar包，在目标target启动的时候添加jvm参数 -javaagent:/Users/wangji/Documents/project/agent/simple-before-jvm-agent/target/simple-before-jvm-agent-jar-with-dependencies.jar
## 然后点击运行即可进行agent的断点调试
