package com.github.zwg.core.advisor;

import java.util.Stack;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/13
 */
public abstract class AbstractAdviceListener implements AdviceListener {

    public ThreadLocal<Stack<Advice>> adviceStack =  ThreadLocal.withInitial(Stack::new);

    @Override
    public void beforeMethod(ClassLoader classLoader, String className, String methodName,
            String methodDesc, Object target, Object[] args) {
        try {
            Advice advice = buildAdvice(classLoader, className, methodName, methodDesc, target,
                    args);
            adviceStack.get()
                    .push(advice);
            if(advice.isBefore()){
                processMethodBeforeAdvice(advice);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void afterMethodReturning(ClassLoader classLoader, String className, String methodName,
            String methodDesc, Object target, Object[] args, Object returnObject) {
        try {
            Advice advice = adviceStack.get().pop();
            advice.setReturnObj(returnObject);
            adviceStack.get().push(advice);
            if(advice.isReturn()){
                processMethodReturningAdvice(advice);
                processMethodFinishAdvice(advice);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void afterMethodThrowing(ClassLoader classLoader, String className, String methodName,
            String methodDesc, Object target, Object[] args, Throwable throwable) {
        try {
            Advice advice = adviceStack.get().pop();
            advice.setThrowable(throwable);
            adviceStack.get().push(advice);
            if(advice.isThrow()){
                processMethodThrowingAdvice(advice);
                processMethodFinishAdvice(advice);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Advice buildAdvice(ClassLoader classLoader, String className, String methodName,
            String methodDesc, Object target, Object[] args) {
        return new Advice(classLoader, className, methodName, methodDesc, target, args, null, null,
                getAccess());
    }

    public abstract int getAccess();

    public void processMethodBeforeAdvice(Advice advice){

    }

    public void processMethodReturningAdvice(Advice advice){

    }

    public void processMethodThrowingAdvice(Advice advice){

    }

    public void processMethodFinishAdvice(Advice advice){

    }
}
