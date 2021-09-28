package com.github.zwg.core.command.handler;

import com.github.zwg.core.advisor.AdviceListener;
import com.github.zwg.core.advisor.AdviceListenerManager;
import com.github.zwg.core.annotation.Arg;
import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.asm.EnhancePoint;
import com.github.zwg.core.asm.Enhancer;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.ParamConstant;
import com.github.zwg.core.manager.JemMethod;
import com.github.zwg.core.manager.MatchStrategy;
import com.github.zwg.core.manager.MethodMatcher;
import com.github.zwg.core.manager.SearchMatcher;
import com.github.zwg.core.session.Session;
import java.lang.instrument.Instrumentation;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31 负责输出一个类中的所有方法调用路径
 */
@Cmd(name = ParamConstant.COMMAND_PATH_TRACE)
public class PathTraceCommandHandler implements CommandHandler {

    @Arg(name = ParamConstant.CLASS_KEY, description = "find class expression")
    private String classPattern;

    @Arg(name = ParamConstant.METHOD_KEY, description = "find method expression")
    private String methodPattern;

    @Arg(name = ParamConstant.METHOD_DESC, required = false, defaultValue = "*", description = "method description expression")
    private String methodDesc;

    @Arg(name = ParamConstant.REG_KEY, required = false, defaultValue = "WILDCARD", description = "expression matching rules: wildcard, regular, equal")
    private String strategy;

    private boolean isTracing = true;

    @Override
    public void execute(Session session, Instrumentation inst) {
        Enhancer.enhance(inst, session.getSessionId(), isTracing, getPoint());
        AdviceListener adviceListener = getAdviceListener();
        AdviceListenerManager.reg(session.getSessionId(), adviceListener);
    }

    private EnhancePoint getPoint() {
        SearchMatcher classMatcher = new SearchMatcher(MatchStrategy.valueOf(strategy),
                classPattern);
        JemMethod jemMethod = new JemMethod(methodPattern, methodDesc);
        MethodMatcher methodMatcher = new MethodMatcher(jemMethod);
        EnhancePoint point = new EnhancePoint(classMatcher, methodMatcher);
        return point;
    }

    private AdviceListener getAdviceListener() {
        return new AdviceListener() {
            @Override
            public void beforeMethod(ClassLoader classLoader, String className, String methodName,
                    String methodDesc, Object target, Object[] args) {

            }

            @Override
            public void afterMethodReturning(ClassLoader classLoader, String className,
                    String methodName, String methodDesc, Object target, Object[] args,
                    Object returnObject) {

            }

            @Override
            public void afterMethodThrowing(ClassLoader classLoader, String className,
                    String methodName, String methodDesc, Object target, Object[] args,
                    Throwable throwable) {

            }

            @Override
            public void beforeTraceInvoking(Integer lineNumber, String className, String methodName,
                    String methodDesc) {

            }

            @Override
            public void afterTraceInvoking(Integer lineNumber, String className, String methodName,
                    String methodDesc) {

            }

            @Override
            public void afterTraceThrowing(Integer lineNumber, String className, String methodName,
                    String methodDesc, String exception) {

            }
        };
    }
}
