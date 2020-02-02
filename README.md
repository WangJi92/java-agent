# java-agent
java agent 断点调试，java agent debug，java agent 学习理解
## 模块介绍
* target-java 这个是目标程序，一个简单的spring boot 工程
* simple-before-jvm-agent   这个是一个简单的jvm agent 尝试
* jvm-agent-aop  这个是一个通过byte-buddy 实现的一个controller 耗死统计的agent

除了本项目之外也可以参考这个[demo-agent](https://github.com/hawkingfoo/demo-agent)
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

---
# Java Agent 调试，agent debug

<a name="8kqvM"></a>
## 一、简介
Java agent 是在另外一个 Java 应用（“目标”应用）启动之前要执行的 Java 程序，这样 agent 就有机会修改目标应用或者应用所运行的环境。修改环境的时候做到对于项目中的代码没有入侵性，不需要修改老项目代码即可实现想要的能力，比如常见的skywoking，就是通过这样的方式实现的。比如这篇文章 通过修改字节码实现Java Agent [通过使用 Byte Buddy，便捷地创建 Java Agent](https://www.infoq.cn/article/Easily-Create-Java-Agents-with-ByteBuddy/) 。还有一些功能，比如热更新、arthas 替换class字节码等等。

<a name="qVkdl"></a>
### 问题 

- Java agent的实现原理是什么？
- Java agent 如何调试呢？习惯了现在的直接代码调试？对于agent 有点慌。

<a name="Rh5xN"></a>
## 二、原理
Java agent 主要是通过Instrumentation实现的。
<a name="JL7BY"></a>
### Instrumentation
<a name="JT1Sh"></a>
#### 简介
     [java.lang.instrument 做动态 Instrumentation](https://www.cnblogs.com/yelao/p/9841810.html) 是 Java SE 5 的新特性，它把 Java 的 instrument 功能从本地代码中解放出来，使之可以用 Java 代码的方式解决问题。使用 Instrumentation，开发者可以构建一个独立于应用程序的代理程序（Agent），用来监测和协助运行在 JVM 上的程序，甚至能够替换和修改某些类的定义。有了这样的功能，开发者就可以实现更为灵活的运行时虚拟机监控和 Java 类操作了，这样的特性实际上提供了一种虚拟机级别支持的 AOP 实现方式，使得开发者无需对 JDK 做任何升级和改动，就可以实现某些 AOP 的功能了。<br />    在 Java SE 6 里面，instrumentation 包被赋予了更强大的功能：启动后的 instrument、本地代码（native code）instrument，以及动态改变 classpath 等等。这些改变，意味着 Java 具有了更强的动态控制、解释能力，它使得 Java 语言变得更加灵活多变。<br />    “java.lang.instrument”包的具体实现，依赖于 JVMTI。JVMTI（Java Virtual Machine Tool Interface）是一套由 Java 虚拟机提供的，为 JVM 相关的工具提供的本地编程接口集合。JVMTI 是从 Java SE 5 开始引入，整合和取代了以前使用的 Java Virtual Machine Profiler Interface (JVMPI) 和 the Java Virtual Machine Debug Interface (JVMDI)，而在 Java SE 6 中，JVMPI 和 JVMDI 已经消失了。JVMTI 提供了一套”代理”程序机制，可以支持第三方工具程序以代理的方式连接和访问 JVM，并利用 JVMTI 提供的丰富的编程接口，完成很多跟 JVM 相关的功能。[当我们谈Debug时，我们在谈什么(Debug实现原理)](https://mp.weixin.qq.com/s?__biz=MzI3MTEwODc5Ng==&mid=2650859362&idx=1&sn=6d0a588da10ebf38e9d83cfa4e7e16fa&chksm=f13298b1c64511a78e3a87df0f1ddebd87719a36a9335edd759c404cd5c53d65e9383da613f1&scene=21#wechat_redirect) 这个让我想到了这篇文章，其实感觉和debug的道理一样的，debug的时候可以修改参数，或者数据的信息。
<a name="vebIv"></a>
#### 官方文档
This class provides services needed to instrument Java programming language code. Instrumentation is the addition of byte-codes to methods for the purpose of gathering data to be utilized by tools. Since the changes are purely additive, these tools do not modify application state or behavior. Examples of such benign tools include monitoring agents, profilers, coverage analyzers, and event loggers.（这个类提供了测试 Java 编程语言代码所需的服务。仪器是将字节代码添加到方法中，目的是收集工具使用的数据。由于这些更改纯粹是附加的，因此这些工具不会修改应用程序状态或行为。此类良性工具的示例包括监控代理、分析器、覆盖分析器和事件记录器。） <br />有两种方法可以获取仪器接口的实例:

- When a JVM is launched in a way that indicates an agent class. In that case an Instrumentation instance is passed to the premain method of the agent class.（当 JVM 以指示代理类的方式启动时。在这种情况下，检测实例被传递给代理类的 premain 方法。）
- When a JVM provides a mechanism to start agents sometime after the JVM is launched. In that case an Instrumentation instance is passed to the agentmain method of the agent code.（当 JVM 在 JVM 启动后的某个时候提供启动代理的机制时，检测实例被传递给代理类的 agentmain 方法）

Java Instrumentation-csdn  [https://blog.csdn.net/DorMOUSENone/article/details/81781131](https://blog.csdn.net/DorMOUSENone/article/details/81781131)
<a name="Ea5hg"></a>
## 三、Java agent debug 实践
<a name="lKcqd"></a>
### Agent 包结构
<a name="4um1r"></a>
#### agent 打包的信息
META-INF.MANIFEST.MF,需要保护一些信息，就是入口类需要传递一个Instrumentation变量给你，JVM 启动的入口类的信息 Premain-Class: com.wangji92.agent.JvmAgentAop，这样标识这个是一个agent。
```xml
Manifest-Version: 1.0
Implementation-Title: jvm-agent-aop
Premain-Class: com.wangji92.agent.JvmAgentAop
Implementation-Version: 1.0-SNAPSHOT
Built-By: wangji
Agent-Class: com.wangji92.agent.JvmAgentAop
Can-Redefine-Classes: true
Specification-Title: jvm-agent-aop
Can-Retransform-Classes: true
Created-By: Apache Maven 3.6.1
Build-Jdk: 1.8.0_181
Specification-Version: 1.0-SNAPSHOT
```

<a name="qGmBr"></a>
#### agent 入口类小例子
```java
package com.wangji92.agent;

import java.lang.instrument.Instrumentation;

/**
 * @author 汪小哥
 * @date 31-01-2020
 */
public class BeforeJvmJavaAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("this is an agent.");
        System.out.println("this is an agent 和jvm 一起启动");
        System.out.println("args:" + agentArgs + "\n");

        Class[] allLoadedClasses = inst.getAllLoadedClasses();
        for (Class loadedClass : allLoadedClasses) {
            System.out.println("load class:" + loadedClass.getCanonicalName());
            if (loadedClass.getClassLoader() != null) {
                System.out.println("  classloader"+loadedClass.getClassLoader().toString() + "\n");
            }else{
                System.out.println("\n");
            }
        }

    }
}

```
<a name="sOxje"></a>
#### maven 配置，参考arthas
```xml
 <build>
        <finalName>simple-before-jvm-agent</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <encoding>UTF-8</encoding>
                    <showDeprecation>true</showDeprecation>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>single</goal>
                        </goals>
                        <phase>package</phase>
                        <configuration>
                            <descriptorRefs>
                                <descriptorRef>jar-with-dependencies</descriptorRef>
                            </descriptorRefs>
                            <archive>
                                <manifestEntries>
                                    <Premain-Class>com.wangji92.agent.BeforeJvmJavaAgent</Premain-Class>
                                    <Agent-Class>com.wangji92.agent.BeforeJvmJavaAgent</Agent-Class>
                                    <Can-Redefine-Classes>true</Can-Redefine-Classes>
                                    <Can-Retransform-Classes>true</Can-Retransform-Classes>
                                    <Specification-Title>${project.name}</Specification-Title>
                                    <Specification-Version>${project.version}</Specification-Version>
                                    <Implementation-Title>${project.name}</Implementation-Title>
                                    <Implementation-Version>${project.version}</Implementation-Version>
                                </manifestEntries>
                            </archive>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

<a name="HJZmO"></a>
### Java agent 加载的两种方式
 根据Java agent的挂载方式有两种，一种是直接在启动的时候挂载，一种是启动完成之后进行挂载（arthas 就是通过这种方式实现的）。这里实践的时候采用第一种。
<a name="7z2Bm"></a>
#### JVM 一起启动
直接在启动参数中增加即可
```xml
# 通过java 可以查看到帮助文档 
-javaagent:<jarpath>[=<选项>]
 加载 Java 编程语言代理, 请参阅 java.lang.instrument
```

比如这个

- simple-before-jvm-agent-jar-with-dependencies.jar  agent 包
- target-java.jar 目标spring boot 程序
```bash
#!/bin/sh -x
dir=$(cd `dirname $0`;pwd)
echo $dir
mvn clean package && \
java -javaagent:$dir/simple-before-jvm-agent/target/simple-before-jvm-agent-jar-with-dependencies.jar=text-args \
-jar $dir/target-java/target/target-java.jar
```

<a name="arJbK"></a>
#### JVM 启动之后 挂载(参考arthas)
将arthas-agent attach 到目标java进程。<br />主要代码：com.taobao.arthas.core.Arthas#attachAgent，直到这里，原始的arthas-core的进程处理已经结束了，同事触发了arthas-agent.jar 作为目标java类的代码类触发了arthas-agent Agent-Class（具体可以参考maven xml 配置） agentmain 方法
```xml
## 1、attach 到目标进程
virtualMachine = VirtualMachine.attach("" + configure.getJavaPid());

## 2、在jvm启动后天就agent，第一个是agent的jar位置，第二个传递的参数
## 了解更多可以参考 java.lang.instrument.Instrumentation
virtualMachine.loadAgent(arthasAgentPath,
                    configure.getArthasCore() + ";" + configure.toString());
```

<a name="tzoM0"></a>
### 理解debug 
 [当我们谈Debug时，我们在谈什么(Debug实现原理)](https://mp.weixin.qq.com/s?__biz=MzI3MTEwODc5Ng==&mid=2650859362&idx=1&sn=6d0a588da10ebf38e9d83cfa4e7e16fa&chksm=f13298b1c64511a78e3a87df0f1ddebd87719a36a9335edd759c404cd5c53d65e9383da613f1&scene=21#wechat_redirect) 这篇文章被我引用了很多次，其实debug就是JPDA之上进行处理，Java agent 也是通过 Instrumentation 依赖JVMTI 实现对于JVM中的字节码修改，获取JVM 虚拟机的加载的字节码的信息，两者之间太像了，当我们想讨论如何debug java agnet的代码的时候，是否想过agent 和目标的代码有什么异同？对对，都是在同一个JVM中的。无论是我们使用远程debug 还是IDEA上的debug其实实质上都是建立在JPDA的基础上，JPDA（Java Platform Debugger Architecture）是Java平台调试体系结构的缩写。由3个规范组成，分别是JVMTI(JVM Tool Interface)，JDWP(Java Debug Wire Protocol)，JDI(Java Debug Interface) 。因此都是在一个JVM 里面的，只要目标class 启动的时候开启了debug，那么agent 也可以被debug的。可能在直接使用IDEA debug的时候有一些限制，比如必须在同级的module下面才能debug，那么远程debug就是天然的可以啦！

<a name="oT2yM"></a>
### 实战Agent debug(AOP 统计耗时)
<a name="hy3BQ"></a>
#### agent 入口类
```java
package com.wangji92.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * https://segmentfault.com/a/1190000007253689
 * https://www.infoq.cn/article/Easily-Create-Java-Agents-with-ByteBuddy/
 * https://blog.csdn.net/undergrowth/article/details/86493336
 * https://www.jianshu.com/p/7a5a2c78dab4
 * https://blog.csdn.net/DorMOUSENone/article/details/81781131
 * jvmAop 处理
 *
 * @author 汪小哥
 * @date 01-02-2020
 */
public class JvmAgentAop {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("this is an agent.");
        System.out.println("this is an agent 和jvm 一起启动");
        System.out.println("args:" + agentArgs + "\n");
        new AgentBuilder.Default()
                .type(nameStartsWith("com.wangji92.target").and(not(isInterface())).and(not(isStatic())))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                        return builder.method(ElementMatchers.any())
                                .intercept(MethodDelegation.to(CostTimeInterceptor.class));
                    }
                }).installOn(inst);

    }
}

```

<a name="6kPib"></a>
#### byte-buddy AOP 处理
byte-buddy 修改字节码可以参考这个 [https://blog.csdn.net/undergrowth/article/details/86493336](https://blog.csdn.net/undergrowth/article/details/86493336)，<br />这里只是简单的统计耗死问题。
```java
package com.wangji92.agent;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * aop 耗时计算
 *
 * @author 汪小哥
 * @date 01-02-2020
 */
public class CostTimeInterceptor {

    @RuntimeType
    public static Object intercept(@Origin Method method,
                                   @SuperCall Callable<?> callable) throws Exception {
        System.out.println("call " + method.getName());
        long start = System.currentTimeMillis();
        try {
            return callable.call();
        } finally {
            System.out.println(method + " took " + (System.currentTimeMillis() - start));
        }
    }
}

```
<a name="OgscL"></a>
#### 目标类

```java
/**
 * @author 汪小哥
 * @date 30-01-2020
 */
@RestController
@RequestMapping("/")
public class JavaAgentTargetController {

    @RequestMapping("/test/{name}")
    @ResponseBody
    public String getClassName(@PathVariable String name) {
        String result = this.getClass().getSimpleName().concat(name);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
           //
        }
        return result;
    }
}
```

<a name="BFg76"></a>
#### 启动脚本
 这里通过脚本启动，并开启远程debug，然后在IDEA 中添加一个remote debug 填写端口5005，随便访问一个目录类，即可实现debug agent的目的
```bash
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

```


<a name="Iem3m"></a>
## 四、总结
不断的学习、总结，才能理解更加的深刻。<br />代码地址：[https://github.com/WangJi92/java-agent](https://github.com/WangJi92/java-agent)
> 更多[汪小哥](https://wangji.blog.csdn.net/)


