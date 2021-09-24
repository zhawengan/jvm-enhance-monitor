package com.github.zwg.core.command.handler;

import static org.apache.commons.lang3.StringUtils.isBlank;

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
import com.github.zwg.core.netty.Message;
import com.github.zwg.core.netty.MessageTypeEnum;
import com.github.zwg.core.ongl.Express;
import com.github.zwg.core.ongl.ExpressFactory;
import com.github.zwg.core.session.Session;
import com.github.zwg.core.statistic.InvokeCost;
import com.github.zwg.core.util.JacksonObjectFormat;
import io.netty.channel.Channel;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
@Cmd(name = ParamConstant.COMMAND_WATCH)
public class WatchCommandHandler implements CommandHandler {

    private final Logger logger = LoggerFactory.getLogger(WatchCommandHandler.class);

    @Arg(name = ParamConstant.CLASS_KEY, description = "find class expression")
    private String classPattern;

    @Arg(name = ParamConstant.METHOD_KEY, description = "find method expression")
    private String methodPattern;

    @Arg(name = ParamConstant.METHOD_DESC, required = false, defaultValue = "*", description = "method description expression")
    private String methodDesc;

    @Arg(name = ParamConstant.REG_KEY, required = false, defaultValue = "WILDCARD", description = "expression matching rules: wildcard, regular, equal")
    private String strategy;

    @Arg(name = ParamConstant.WATCH, required = false, defaultValue = "b", description = "watch point. before(b),finish(f),exception(e),success(s)")
    private String watchPoint;

    @Arg(name = ParamConstant.NUMBER_PARAM, description = "Threshold of execution times")
    private Integer threshold;

    @Arg(name = ParamConstant.EXPRESS, description = "Conditional expression by OGNL")
    private String conditionExpress;

    private boolean isBefore = false;
    private boolean isFinish = false;
    private boolean isException = false;
    private boolean isSuccess = false;
    private final JacksonObjectFormat objectFormat = new JacksonObjectFormat();

    @Override
    public void execute(Session session, Instrumentation inst,
            MonitorCallback callback) {
        watchPointInit();
        Enhancer.enhance(inst, session.getSessionId(), false, getPoint());
        AdviceListener adviceListener = getAdviceListener(session);
        AdviceListenerManager.reg(session.getSessionId(), adviceListener);
        logger.info("watch command handler registered。session:{}", session);
    }

    private void watchPointInit() {
        if (StringUtils.isBlank(watchPoint)) {
            isBefore = true;
        } else {
            isBefore = watchPoint.contains("b");
            isFinish = watchPoint.contains("f");
            isException = watchPoint.contains("e");
            isSuccess = watchPoint.contains("s");
        }
    }

    private EnhancePoint getPoint() {
        SearchMatcher classMatcher = new SearchMatcher(MatchStrategy.valueOf(strategy),
                classPattern);
        JemMethod jemMethod = new JemMethod(methodPattern, methodDesc);
        MethodMatcher methodMatcher = new MethodMatcher(jemMethod);
        return new EnhancePoint(classMatcher, methodMatcher);
    }

    private AdviceListener getAdviceListener(final Session session) {
        return new AbstractAdviceListener() {

            private final InvokeCost invokeCost = new InvokeCost();
            private final AtomicInteger timesRef = new AtomicInteger();

            @Override
            public int getAccess() {
                return AccessConstant.defaultMethodAccess();
            }

            @Override
            public void processMethodBeforeAdvice(Advice advice) {
                invokeCost.begin();
                if (isBefore) {
                    watching(advice);
                }
                logger.info("processMethodBeforeAdvice, advice:{}", advice);
            }

            @Override
            public void processMethodReturningAdvice(Advice advice) {
                if (isSuccess) {
                    watching(advice);
                }
            }

            @Override
            public void processMethodThrowingAdvice(Advice advice) {
                if (isException) {
                    watching(advice);
                }
            }

            @Override
            public void processMethodFinishAdvice(Advice advice) {
                if (isFinish) {
                    watching(advice);
                }
            }

            private boolean isOverThreshold(int currentTimes) {
                return null != threshold
                        && currentTimes >= threshold;
            }

            private boolean isInCondition(Express express) {
                try {
                    return isBlank(conditionExpress)
                            || express.is(conditionExpress);
                } catch (Exception e) {
                    return false;
                }
            }

            private void watching(Advice advice) {
                try {
                    Express express = ExpressFactory.newExpress(advice)
                            .bind("cost", invokeCost.cost());
                    if (isInCondition(express)) {
                        Object result = express.get(conditionExpress);
                        Channel channel = session.getChannel();
                        Message response = new Message();
                        response.setMessageType(MessageTypeEnum.RESPONSE);
                        response.setBody(objectFormat.toJsonPretty(result));
                        channel.writeAndFlush(response, channel.voidPromise());
                        if (isOverThreshold(timesRef.incrementAndGet())) {
                            session.getChannel().close();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
