package com.github.zwg.core.command;

import com.github.zwg.core.advisor.AdviceListener;
import com.github.zwg.core.advisor.AdviceListenerManager;
import com.github.zwg.core.asm.EnhancePoint;
import com.github.zwg.core.asm.Enhancer;
import com.github.zwg.core.execption.BadCommandException;
import com.github.zwg.core.manager.JemMethod;
import com.github.zwg.core.manager.MatchStrategy;
import com.github.zwg.core.manager.MethodMatcher;
import com.github.zwg.core.manager.SearchMatcher;
import com.github.zwg.core.session.Session;
import com.github.zwg.core.util.ParamConstant;
import java.lang.instrument.Instrumentation;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/6
 */
public abstract class AbstractEnhanceCommandHandler implements CommandHandler{

    @Override
    public void execute(Session session, Command command, Instrumentation inst,
            MonitorCallback callback) {
        Map<String, String> options = command.getOptions();
        //0、获取类匹配方式
        String reg = options.get(ParamConstant.REG_KEY);
        //1、获取class,method的匹配表达式
        String classPattern = options.get(ParamConstant.CLASS_KEY);
        String methodPattern = options.get(ParamConstant.METHOD_KEY);
        String methodDesc = options.getOrDefault(ParamConstant.METHOD_DESC,"*");
        if (StringUtils.isBlank(classPattern) || StringUtils.isBlank(methodPattern)) {
            throw new BadCommandException("classPattern or methodPattern unValid");
        }
        SearchMatcher classMatcher = new SearchMatcher(
                StringUtils.isBlank(reg) ? MatchStrategy.WILDCARD : MatchStrategy.valueOf(reg),
                classPattern);
        JemMethod jemMethod = new JemMethod(methodPattern,methodDesc);
        MethodMatcher methodMatcher = new MethodMatcher(jemMethod);
        EnhancePoint point = new EnhancePoint(classMatcher,methodMatcher);
        Enhancer.enhance(inst,session.getSessionId(),false,point);
        AdviceListener adviceListener = getAdviceListener();
        AdviceListenerManager.reg(session.getSessionId(), adviceListener);

    }

    public abstract AdviceListener getAdviceListener();


}
