package com.github.zwg.agent;

import java.lang.instrument.Instrumentation;
import java.net.URL;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/30
 */
public class AgentLauncher {

    /**
     * 自定义类加载器。 考虑项目通过反射方式启动，优先创建自定义类加载器，然后用次加载器加载相关class
     */
    private static ClassLoader agentClassLoader;


    public static void premain(String args, Instrumentation inst) {
        main(args, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        main(args, inst);
    }

    /**
     * 1、拿到classLoader 2、根据classLoader在被监控jvm中启动一个tcp listener, 后续字节增强通过改tcp连接进行命令交互
     */
    private static void main(String args, Instrumentation inst) {

        try {
            String[] params = args.split(";");
            ClassLoader agentClassLoader = getAgentClassLoader(params[0]);
            Class<?> listenerClass = agentClassLoader
                    .loadClass("com.github.zwg.core.AgentListener");
            //通过反射方法，获取单例
            Object agentListener = listenerClass
                    .getMethod("getInstance", String.class, Instrumentation.class)
                    .invoke(null, params[1], inst);
            //校验tcp端口启动状态
            boolean isStarted = (Boolean) listenerClass.getMethod("isStarted")
                    .invoke(agentListener);
            //如果未启动。重新启动listener
            if (!isStarted) {
                listenerClass.getMethod("start").invoke(agentListener);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 使用自定义类加载器，可以在取消类增强后，
     * 从jvm中卸载自定义加载的类
     */
    private static ClassLoader getAgentClassLoader(String agentJar) throws Throwable {
        if (agentClassLoader == null) {
            synchronized (AgentLauncher.class) {
                if (agentClassLoader == null) {
                    agentClassLoader = new AgentClassLoader(new URL[]{new URL(agentJar)});
                }
            }
        }
        return agentClassLoader;
    }

    public static synchronized void resetClassLoader() {
        agentClassLoader = null;
    }
}
