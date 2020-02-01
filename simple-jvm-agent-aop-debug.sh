#!/bin/sh -x
dir=$(cd `dirname $0`;pwd)
echo $dir
mvn clean package && \
java -agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n \
-javaagent:$dir/jvm-agent-aop/target/jvm-agent-aop-jar-with-dependencies.jar=text-args \
-jar $dir/target-java/target/target-java.jar

# 本文主要是为了测试 agent的debug 功能，从而了解arthas debug
# 实际上，agent的 remote debug 只要agent和target 在同一个jvm 中就会被加载，从而可以debug。


## 通过Byte Buddy 实现字节码修改（skyworking 也是这样的） 参考 https://zhuanlan.zhihu.com/p/84514959
## 使用bytebuddy构建agent   https://segmentfault.com/a/1190000007253689
## 通过使用 Byte Buddy，便捷地创建 Java Agent  https://www.infoq.cn/article/Easily-Create-Java-Agents-with-ByteBuddy/
## byte-buddy 1.9.6 简述及原理1  https://blog.csdn.net/undergrowth/article/details/86493336 github 有demo
## java agent: JVM的层面的"AOP"  https://www.jianshu.com/p/7a5a2c78dab4

