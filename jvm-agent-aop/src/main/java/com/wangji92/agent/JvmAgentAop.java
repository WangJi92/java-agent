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
