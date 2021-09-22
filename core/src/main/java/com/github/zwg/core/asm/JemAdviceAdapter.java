package com.github.zwg.core.asm;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/6
 */
public class JemAdviceAdapter extends AdviceAdapter {

    private final Logger logger = LoggerFactory.getLogger(JemAdviceAdapter.class);
    private static final Type ASM_TYPE_INT = Type.getType(int.class);
    private final Label beginLabel = new Label();
    private final Label endLabel = new Label();
    private final Type ASM_TYPE_MONITOR = Type.getType("Lcom/github/zwg/agent/MonitorProxy;");
    private final Type ASM_TYPE_METHOD = Type.getType(java.lang.reflect.Method.class);
    private final Type ASM_TYPE_OBJECT = Type.getType(Object.class);
    private final Method ASM_METHOD_METHOD_INVOKE = Method
            .getMethod("Object invoke(Object,Object[])");
    private final Type ASM_TYPE_STRING = Type.getType(String.class);
    private final Type ASM_TYPE_OBJECT_ARRAY = Type.getType(Object[].class);
    private final Type ASM_TYPE_CLASS = Type.getType(Class.class);
    private final Type ASM_TYPE_THROWABLE = Type.getType(Throwable.class);
    private final Type ASM_TYPE_INTEGER = Type.getType(Integer.class);
    private final Type ASM_TYPE_CLASS_LOADER = Type.getType(ClassLoader.class);
    private final String sessionId;
    private final boolean isTracing;
    private final String className;
    private final String methodName;
    private final String methodDesc;

    private Integer currentLineNumber;

    private final CodeLock codeLock = new AsmCodeLock(this);

    protected JemAdviceAdapter(String sessionId, boolean isTracing, String className,
            MethodVisitor methodVisitor,
            int access,
            String name, String descriptor) {
        super(ASM5, methodVisitor, access, name, descriptor);
        this.sessionId = sessionId;
        this.isTracing = isTracing;
        this.className = className;
        this.methodName = name;
        this.methodDesc = descriptor;
    }

    @Override
    protected void onMethodEnter() {
        logger.debug("visit method enter. class:{},method:{}", this.className, this.methodName);
        codeLock.lockBlockCode(() -> {
            /**通过字节码方式，调用MonitorProxy的ON_METHOD_BEFORE方法,实际代理对象为
             *AdviceListenerManager.onMethodBefore(String sessionId,
             *                        ClassLoader classLoader,
             *                        String className,
             *                        String methodName,
             *                        String methodDesc,
             *                        Object target,
             *                        Object[] args
             *                )
             */
            //获取静态成员
            getStatic(ASM_TYPE_MONITOR, "ON_METHOD_BEFORE", ASM_TYPE_METHOD);
            //静态方法，第一个参数是null,将参数推到线程栈上
            push((Type) null);
            //将方法的参数依次推送到线程栈上
            loadParamsForMethodEnter();
            //调用方法
            invokeVirtual(ASM_TYPE_METHOD, ASM_METHOD_METHOD_INVOKE);
            pop();
        });
        mark(beginLabel);
    }

    @Override
    protected void onMethodExit(int opcode) {
        logger.debug("visit method exit. class:{},method:{},opcode:{}", this.className,
                this.methodName, opcode);
        if (opcode != ATHROW) {
            codeLock.lockBlockCode(() -> {
                loadReturn(opcode);
                /**通过字节码方式，调用MonitorProxy的ON_METHOD_RETURN方法,实际代理对象为
                 *AdviceListenerManager.onMethodReturn(String sessionId,
                 *                        Object returnObject
                 *                )
                 */
                getStatic(ASM_TYPE_MONITOR, "ON_METHOD_RETURN", ASM_TYPE_METHOD);
                push((Type) null);
                loadParamsForMethodReturn();
                invokeVirtual(ASM_TYPE_METHOD, ASM_METHOD_METHOD_INVOKE);
                pop();
            });
        }
    }


    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        logger.debug("visit method maxs. class:{},method:{}", this.className, this.methodName);
        mark(endLabel);
        catchException(beginLabel, endLabel, ASM_TYPE_THROWABLE);
        codeLock.lockBlockCode(() -> {
            //加载异常
            dup();
            /**通过字节码方式，调用MonitorProxy的 ON_METHOD_THROW 方法,实际代理对象为
             *AdviceListenerManager.onMethodThrow(String sessionId,
             *                        Throwable throwable
             *                )
             */
            getStatic(ASM_TYPE_MONITOR, "ON_METHOD_THROW", ASM_TYPE_METHOD);
            push((Type) null);
            loadThrowArgs();
            invokeVirtual(ASM_TYPE_METHOD, ASM_METHOD_METHOD_INVOKE);
            pop();
        });
        throwException();
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
        codeLock.lockOrUnlock(opcode);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        currentLineNumber = line;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
            String descriptor, boolean isInterface) {
        if (!isTracing || codeLock.isLock()) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            return;
        }
        //方法调用前通知
        tracing(TracingType.TRY, owner, name, descriptor);

        Label beginLabel = new Label();
        Label endLabel = new Label();
        Label finallyLabel = new Label();

        //try{
        mark(beginLabel);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        mark(endLabel);

        tracing(TracingType.END, owner, name, descriptor);
        goTo(finallyLabel);
        //}catch{
        catchException(beginLabel, endLabel, ASM_TYPE_THROWABLE);
        tracing(TracingType.EXCEPTION, owner, name, descriptor);

        throwException();
        //finally{
        mark(finallyLabel);
        //}

    }


    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    private void loadParamsForMethodEnter() {
        push(7);
        newArray(ASM_TYPE_OBJECT);

        dup();
        push(0);
        push(sessionId);
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(1);
        loadClassLoader();
        arrayStore(ASM_TYPE_CLASS_LOADER);

        dup();
        push(2);
        push(tranClassName(className));
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(3);
        push(methodName);
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(4);
        push(methodDesc);
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(5);
        loadThisOrPushNull();
        arrayStore(ASM_TYPE_OBJECT);

        dup();
        push(6);
        loadArgArray();
        arrayStore(ASM_TYPE_OBJECT_ARRAY);
    }

    private void loadParamsForMethodReturn() {
        dup2X1();
        pop2();
        push(2);
        newArray(ASM_TYPE_OBJECT);
        dup();
        dup2X1();
        pop2();
        push(0);
        swap();
        arrayStore(ASM_TYPE_OBJECT);

        dup();
        push(1);
        push(sessionId);
        arrayStore(ASM_TYPE_STRING);
    }


    private void loadThisOrPushNull() {
        if (isStaticMethod()) {
            push((Type) null);
        } else {
            loadThis();
        }
    }

    private void loadClassLoader() {
        if (isStaticMethod()) {
            visitLdcInsn(className);
            invokeStatic(ASM_TYPE_CLASS, Method.getMethod("Class forName(String)"));
            invokeStatic(ASM_TYPE_CLASS, Method.getMethod("ClassLoader getClassLoader()"));
        } else {
            loadThis();
            invokeVirtual(ASM_TYPE_OBJECT, Method.getMethod("Class getClass()"));
            invokeVirtual(ASM_TYPE_CLASS, Method.getMethod("ClassLoader getClassLoader()"));
        }
    }

    private String tranClassName(String className) {
        return StringUtils.replace(className, "/", ".");
    }

    private boolean isStaticMethod() {
        return (methodAccess & ACC_STATIC) != 0;
    }


    private void loadReturn(int opcode) {
        switch (opcode) {
            case RETURN:
                push((Type) null);
                break;
            case ARETURN:
                dup();
                break;
            case LRETURN:
            case DRETURN:
                dup2();
                box(Type.getReturnType(methodDesc));
                break;
            default:
                dup();
                box(Type.getReturnType(methodDesc));
                break;
        }
    }

    private void loadThrowArgs() {
        dup2X1();
        pop2();
        push(2);
        newArray(ASM_TYPE_OBJECT);

        dup();
        dup2X1();
        pop2();
        push(0);
        swap();
        arrayStore(ASM_TYPE_THROWABLE);

        dup();
        push(1);
        push(sessionId);
        arrayStore(ASM_TYPE_STRING);
    }

    /**
     * 通过字节码方式，调用MonitorProxy的 INVOKING_BEFORE,INVOKING_RETURN,INVOKING_THROW 方法,实际代理对象为
     * AdviceListenerManager.beforeTraceInvoking(String sessionId, Integer lineNumber, String owner,
     * String name, String desc)
     *
     * AdviceListenerManager.afterTraceInvoking(String sessionId, Integer lineNumber, String owner,
     * String name, String desc)
     *
     * AdviceListenerManager.afterTraceThrowing(String sessionId, Integer lineNumber, String owner,
     * String name, String desc, String throwException)
     */
    private void tracing(TracingType tracingType, String owner, String name, String desc) {

        codeLock.lockBlockCode(() -> {
            if (TracingType.EXCEPTION == tracingType) {
                loadArrayForInvokeThrowTracing(owner, name, desc);
            } else {
                loadArrayForInvokeBeforeOrAfterTracing(owner, name, desc);
            }
            switch (tracingType) {
                case TRY:
                    getStatic(ASM_TYPE_MONITOR, "INVOKING_BEFORE", ASM_TYPE_METHOD);
                    break;
                case END:
                    getStatic(ASM_TYPE_MONITOR, "INVOKING_RETURN", ASM_TYPE_METHOD);
                    break;
                case EXCEPTION:
                    getStatic(ASM_TYPE_MONITOR, "INVOKING_THROW", ASM_TYPE_METHOD);
                    break;
            }
            swap();
            push((Type) null);
            swap();
            invokeVirtual(ASM_TYPE_METHOD, ASM_METHOD_METHOD_INVOKE);
            pop();
        });

    }

    private void loadArrayForInvokeBeforeOrAfterTracing(String owner, String name, String desc) {

        push(5);
        newArray(ASM_TYPE_OBJECT);

        dup();
        push(0);
        push(sessionId);
        arrayStore(ASM_TYPE_STRING);

        if (null != currentLineNumber) {
            dup();
            push(1);
            push(currentLineNumber);
            box(ASM_TYPE_INT);
            arrayStore(ASM_TYPE_INTEGER);
        }

        dup();
        push(2);
        push(owner);
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(3);
        push(name);
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(4);
        push(desc);
        arrayStore(ASM_TYPE_STRING);

    }

    private void loadArrayForInvokeThrowTracing(String owner, String name, String desc) {

        push(6);
        newArray(ASM_TYPE_OBJECT);

        dup();
        push(0);
        push(sessionId);
        arrayStore(ASM_TYPE_STRING);

        if (null != currentLineNumber) {
            dup();
            push(1);
            push(currentLineNumber);
            box(ASM_TYPE_INT);
            arrayStore(ASM_TYPE_INTEGER);
        }

        dup();
        push(2);
        push(owner);
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(3);
        push(name);
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(4);
        push(desc);
        arrayStore(ASM_TYPE_STRING);

        dup2();
        swap();
        invokeVirtual(ASM_TYPE_OBJECT, Method.getMethod("Class getClass()"));
        invokeVirtual(ASM_TYPE_CLASS, Method.getMethod("String getName()"));
        push(5);
        swap();
        arrayStore(ASM_TYPE_STRING);

    }
}
