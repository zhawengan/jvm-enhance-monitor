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
            initMonitorProxy(agentClassLoader);
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
     * 获取AdviceWeaver，并将其方法注册到监控代理中,这样，通过监控代理AppClassLoader对象 和AgentClassLoader对象关联起来
     */
    private static void initMonitorProxy(ClassLoader classLoader)
            throws ClassNotFoundException, NoSuchMethodException {
        Class<?> adviceWeaver = classLoader.loadClass("com.github.zwg.core.advisor.AdviceWeaver");
        MonitorProxy.init(
                adviceWeaver.getMethod("onMethodBefore",
                        String.class,//sessionId
                        ClassLoader.class,//classLoader
                        String.class,//className
                        String.class,//methodName
                        String.class,//methodDesc
                        Object.class,//target
                        Object[].class//args
                ),
                adviceWeaver.getMethod("onMethodReturn",
                        String.class,//sessionId
                        Object.class//returnObject
                ),
                adviceWeaver.getMethod("onMethodThrow",
                        String.class,//sessionId
                        Throwable.class//throwable
                ),
                adviceWeaver.getMethod("invokingBefore",
                        String.class,//sessionId
                        Integer.class,//lineNumber
                        String.class,//owner
                        String.class,//name
                        String.class//desc
                ),
                adviceWeaver.getMethod("invokingReturn",
                        String.class,//sessionId
                        Integer.class,//lineNumber
                        String.class,//owner
                        String.class,//name
                        String.class//desc
                ),
                adviceWeaver.getMethod("invokingThrow",
                        String.class,//sessionId
                        Integer.class,//lineNumber
                        String.class,//owner
                        String.class,//name
                        String.class,//desc
                        String.class//throwException
                )
        );
    }

    /**
     * 使用自定义类加载器，可以在取消类增强后， 从jvm中卸载自定义加载的类
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
