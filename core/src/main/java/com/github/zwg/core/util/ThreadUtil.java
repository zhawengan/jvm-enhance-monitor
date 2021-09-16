package com.github.zwg.core.util;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/13
 */
public class ThreadUtil {

    public static String getThreadInfo() {
        Thread thread = Thread.currentThread();
        return String.format("thread_name=\"%s\" thread_id=0x%s;is_daemon=%s;priority=%s;",
                thread.getName(),
                Long.toHexString(thread.getId()),
                thread.isDaemon(),
                thread.getPriority());
    }

    public static String getThreadStack(String title) {
        Thread currentThread = Thread.currentThread();
        StackTraceElement[] stackTrace = currentThread.getStackTrace();
        StringBuilder builder = new StringBuilder().append(title).append("\n");
        for (int i = 0; i < stackTrace.length; i++) {
            builder.append(String.format("        at %s.%s(%s:%s)\n", stackTrace[i].getClassName(),
                    stackTrace[i].getMethodName(), stackTrace[i].getFileName(),
                    stackTrace[i].getLineNumber()));
        }
        return builder.toString();
    }
}
