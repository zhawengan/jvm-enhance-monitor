package com.github.zwg.core.advisor;

import com.github.zwg.core.netty.MessageUtil;
import com.github.zwg.core.session.DefaultSessionManager;
import com.github.zwg.core.session.Session;
import io.netty.channel.Channel;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/5
 */
public class AdviceListenerManager {

    private static final Logger logger = LoggerFactory.getLogger(AdviceListenerManager.class);
    private static final int FRAME_STACK_SIZE = 7;
    private static final Map<String, AdviceListener> advices = new ConcurrentHashMap<>();
    private static final Map<Thread, Stack<Object[]>> threadMethodStash = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> isSelfCall = ThreadLocal.withInitial(() -> false);

    private AdviceListenerManager(){

    }

    public static void reg(String sessionId, AdviceListener adviceListener) {
        advices.put(sessionId, adviceListener);
    }

    public static void unReg(String sessionId){
        AdviceListener listener = advices.remove(sessionId);
        if(listener!=null){
            listener.destroy();
        }
        Session session = DefaultSessionManager.getInstance().get(sessionId);
        if (session != null) {
            Channel channel = session.getChannel();
            channel.writeAndFlush(MessageUtil.buildPrompt(), channel.voidPromise());
        }
    }

    /**
     * 绑定到MonitorProxy中的ON_METHOD_BEFORE，植入业务代码，被调用触发
     */
    public static void onMethodBefore(String sessionId,
            ClassLoader classLoader,
            String className,
            String methodName,
            String methodDesc,
            Object target,
            Object[] args) {
        logger.info("prepare to invoke before method.");
        if (!advices.containsKey(sessionId)) {
            return;
        }

        if (isSelfCall.get()) {
            return;
        } else {
            isSelfCall.set(true);
        }
        try {
            Object[] methodStash = new Object[FRAME_STACK_SIZE];
            methodStash[0] = classLoader;
            methodStash[1] = className;
            methodStash[2] = methodName;
            methodStash[3] = methodDesc;
            methodStash[4] = target;
            methodStash[5] = args;
            AdviceListener adviceListener = advices.get(sessionId);
            methodStash[6] = adviceListener;

            before(adviceListener, classLoader, className, methodName, methodDesc, target, args);
            logger.info("invoke before method success.");
            saveThreadMethodStash(methodStash);
            logger.info("save method stash success.");
        } finally {
            isSelfCall.set(false);
        }
        logger.info("onMethodBefore process success.");
    }


    /**
     * 绑定到MonitorProxy中的ON_METHOD_RETURN，植入业务代码，被调用触发
     */
    public static void onMethodReturn(String sessionId, Object returnObject) {
        onMethodEnd(sessionId, false, returnObject);
    }

    /**
     * 绑定到MonitorProxy中的ON_METHOD_THROW，植入业务代码，被调用触发
     */
    public static void onMethodThrow(String sessionId, Throwable throwable) {
        onMethodEnd(sessionId, true, throwable);
    }

    /**
     * 绑定到MonitorProxy中的INVOKING_BEFORE，植入业务代码，被调用触发
     */
    public static void beforeTraceInvoking(String sessionId,
            Integer lineNumber,
            String owner,
            String name,
            String desc) {
        AdviceListener adviceListener = advices.get(sessionId);
        if (adviceListener == null) {
            return;
        }
        try {
            adviceListener.beforeTraceInvoking(lineNumber, owner, name, desc);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 绑定到MonitorProxy中的INVOKING_RETURN，植入业务代码，被调用触发
     */
    public static void afterTraceInvoking(String sessionId,
            Integer lineNumber,
            String owner,
            String name,
            String desc) {
        AdviceListener adviceListener = advices.get(sessionId);
        if (adviceListener == null) {
            return;
        }
        try {
            adviceListener.afterTraceInvoking(lineNumber, owner, name, desc);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    /**
     * 绑定到MonitorProxy中的INVOKING_THROW，植入业务代码，被调用触发
     */
    public static void afterTraceThrowing(String sessionId,
            Integer lineNumber,
            String owner,
            String name,
            String desc,
            String throwException) {
        AdviceListener adviceListener = advices.get(sessionId);
        if (adviceListener == null) {
            return;
        }
        try {
            adviceListener.afterTraceThrowing(lineNumber, owner, name, desc, throwException);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void onMethodEnd(String sessionId, boolean isThrowing,
            Object returnOrThrowable) {
        if (!advices.containsKey(sessionId)) {
            return;
        }
        if (isSelfCall.get()) {
            return;
        } else {
            isSelfCall.set(true);
        }
        try {
            Object[] methodStash = getThreadMethodStash();
            if (methodStash == null) {
                return;
            }
            ClassLoader classLoader = (ClassLoader) methodStash[0];
            String className = (String) methodStash[1];
            String methodName = (String) methodStash[2];
            String methodDesc = (String) methodStash[3];
            Object target = methodStash[4];
            Object[] args = (Object[]) methodStash[5];
            AdviceListener adviceListener = (AdviceListener) methodStash[6];
            if (!isThrowing) {
                afterReturning(adviceListener, classLoader, className, methodName, methodDesc,
                        target, args, returnOrThrowable);
            } else {
                afterThrowing(adviceListener, classLoader, className, methodName, methodDesc,
                        target, args, (Throwable) returnOrThrowable);
            }
        } finally {
            isSelfCall.set(false);
        }
    }

    private static void before(AdviceListener adviceListener, ClassLoader classLoader,
            String className, String methodName, String methodDesc, Object target, Object[] args) {
        if (adviceListener != null) {
            try {
                adviceListener
                        .beforeMethod(classLoader, className, methodName, methodDesc, target, args);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static void afterReturning(AdviceListener adviceListener, ClassLoader classLoader,
            String className,
            String methodName, String methodDesc, Object target, Object[] args, Object returnObj) {
        if (adviceListener != null) {
            try {
                adviceListener.afterMethodReturning(classLoader, className, methodName, methodDesc,
                        target, args, returnObj);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static void afterThrowing(AdviceListener adviceListener, ClassLoader classLoader,
            String className,
            String methodName, String methodDesc, Object target, Object[] args,
            Throwable throwable) {
        if (adviceListener != null) {
            try {
                adviceListener
                        .afterMethodThrowing(classLoader, className, methodName, methodDesc, target,
                                args, throwable);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void saveThreadMethodStash(Object[] methodStash) {
        Thread thread = Thread.currentThread();
        Stack<Object[]> stashData = threadMethodStash.computeIfAbsent(thread, k -> new Stack<>());
        stashData.push(methodStash);
    }

    public static Object[] getThreadMethodStash() {
        Stack<Object[]> threadStack = threadMethodStash.get(Thread.currentThread());
        if (threadStack == null || threadStack.isEmpty()) {
            return null;
        }
        return threadStack.pop();
    }


}
