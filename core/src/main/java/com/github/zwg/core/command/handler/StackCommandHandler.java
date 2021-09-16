package com.github.zwg.core.command.handler;

import com.github.zwg.core.advisor.AbstractAdviceListener;
import com.github.zwg.core.advisor.Advice;
import com.github.zwg.core.advisor.AdviceListener;
import com.github.zwg.core.advisor.AdviceListenerManager;
import com.github.zwg.core.annotation.Arg;
import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.asm.EnhancePoint;
import com.github.zwg.core.asm.Enhancer;
import com.github.zwg.core.command.AccessConstant;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.MonitorCallback;
import com.github.zwg.core.command.ParamConstant;
import com.github.zwg.core.manager.JemMethod;
import com.github.zwg.core.manager.MatchStrategy;
import com.github.zwg.core.manager.MethodMatcher;
import com.github.zwg.core.manager.SearchMatcher;
import com.github.zwg.core.ongl.ExpressFactory;
import com.github.zwg.core.session.Session;
import com.github.zwg.core.statistic.InvokeCost;
import com.github.zwg.core.util.ThreadUtil;
import io.netty.channel.Channel;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31 输出当前方法执行上下文
 */
@Cmd(name = ParamConstant.COMMAND_STACK, description = "print current method stack info", help = "stack -c *testClass -m *testMethod -n 3")
public class StackCommandHandler implements CommandHandler {

    @Arg(name = ParamConstant.CLASS_KEY, description = "find class expression")
    private String classPattern;

    @Arg(name = ParamConstant.METHOD_KEY, description = "find method expression")
    private String methodPattern;

    @Arg(name = ParamConstant.METHOD_DESC, required = false, defaultValue = "*", description = "method description expression")
    private String methodDesc;

    @Arg(name = ParamConstant.REG_KEY, required = false, defaultValue = "WILDCARD", description = "expression matching rules: wildcard, regular, equal")
    private String strategy;

    @Arg(name = ParamConstant.EXPRESS, description = "Conditional expression by OGNL")
    private String conditionExpress;

    @Arg(name = ParamConstant.NUMBER_PARAM, required = false, defaultValue = "-1", description = "Threshold of execution times")
    private Integer threshold;


    @Override
    public void execute(Session session, Instrumentation inst,
            MonitorCallback callback) {
        Enhancer.enhance(inst, session.getSessionId(), false, getPoint());
        AdviceListener adviceListener = getAdviceListener(session);
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

    private AdviceListener getAdviceListener(Session session) {
        return new AbstractAdviceListener() {

            private final ThreadLocal<String> stackInfo = new ThreadLocal<String>();
            private final InvokeCost invokeCost = new InvokeCost();
            private final AtomicInteger times = new AtomicInteger();

            @Override
            public int getAccess() {
                return AccessConstant.defaultMethodAccess();
            }


            @Override
            public void processMethodBeforeAdvice(Advice advice) {
                stackInfo.set(ThreadUtil.getThreadStack(ThreadUtil.getThreadInfo()));
                invokeCost.begin();
            }

            @Override
            public void processMethodFinishAdvice(Advice advice) {
                if (isInCondition(advice, invokeCost.cost())) {
                    Channel channel = session.getChannel();
                    channel.writeAndFlush(stackInfo.get(), channel.voidPromise());
                    if (threshold != null && times.incrementAndGet() >= threshold) {
                        channel.close();
                        // TODO: 2021/9/13 监控次数已到，考虑取消
                    }
                }

            }

            private boolean isInCondition(Advice advice, long cost) {
                try {
                    return StringUtils.isBlank(conditionExpress) || ExpressFactory
                            .newExpress(advice).bind(cost).is(conditionExpress);
                } catch (Exception ex) {
                    return false;
                }
            }
        };
    }
}
