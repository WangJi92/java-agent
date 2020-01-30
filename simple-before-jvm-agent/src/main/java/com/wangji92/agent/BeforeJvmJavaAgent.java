package com.wangji92.agent;

import lombok.extern.slf4j.Slf4j;

import java.lang.instrument.Instrumentation;

/**
 * @author 汪小哥
 * @date 31-01-2020
 */
@Slf4j
public class BeforeJvmJavaAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("this is an agent.");
        log.info("this is an agent 和jvm 一起启动");
        log.info("args:" + agentArgs + "\n");
        log.info(inst.getAllLoadedClasses().toString());
    }
}
