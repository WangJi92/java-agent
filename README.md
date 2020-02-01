# java-agent
java agent 的理解，实战
## 模块介绍
* target-java 这个是目标程序，一个简单的spring boot 工程
* simple-before-jvm-agent   这个是一个简单的jvm agent 尝试
* jvm-agent-aop  这个是一个通过byte-buddy 实现的一个controller 耗死统计的agent

## 脚本介绍
这里的尝试记录java agent都是通过脚本触发启动，这样方便理解java agent debug的处理方式，这个理解了理解[arthas debug](https://github.com/alibaba/arthas/issues/222)更加的容易
* simple-before-jvm-agent.sh 就是一个简单的脚本启动一个agent，看一下脚本是否启动起来了
* simple-jvm-agent-aop-debug.sh 这个是一个开启远程debug，和实现耗死统计的那个一起结合，体验一下debug agent的感觉。
首先启动这个脚本，然后IDEA 开启远程debug 5005 这个端口，然后方法target-java http://127.0.0.1:7001/test/test，将断点放置在
com.wangji92.agent.CostTimeInterceptor.intercept aop 拦截的这个位置即可体验。
这个是开启远程debug
```shell script
-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n 
```


