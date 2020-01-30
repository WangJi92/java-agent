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
