package com.github.zwg.core.command.handler;

import static com.github.zwg.core.util.ClassUtil.tranModifier;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.github.zwg.core.annotation.Arg;
import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.ParamConstant;
import com.github.zwg.core.manager.JemMethod;
import com.github.zwg.core.manager.MatchStrategy;
import com.github.zwg.core.manager.MethodMatcher;
import com.github.zwg.core.manager.ReflectClassManager;
import com.github.zwg.core.manager.SearchMatcher;
import com.github.zwg.core.netty.MessageUtil;
import com.github.zwg.core.session.Session;
import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
@Cmd(name = ParamConstant.COMMAND_SEARCH_METHOD)
public class SearchMethodCommandHandler implements CommandHandler {

    private final Logger logger = LoggerFactory.getLogger(SearchMethodCommandHandler.class);

    @Arg(name = ParamConstant.CLASS_KEY, description = "find class expression")
    private String classPattern;

    @Arg(name = ParamConstant.METHOD_KEY, description = "find method expression")
    private String methodPattern;

    @Arg(name = ParamConstant.METHOD_DESC, required = false, defaultValue = "*", description = "method description expression")
    private String methodDesc;

    @Arg(name = ParamConstant.REG_KEY, required = false, defaultValue = "WILDCARD", description = "expression matching rules: wildcard, regular, equal")
    private String strategy;


    @Override
    public void execute(Session session, Instrumentation inst) {
        SearchMatcher classMatcher = new SearchMatcher(MatchStrategy.valueOf(strategy),
                classPattern);
        JemMethod jemMethod = new JemMethod(methodPattern, methodDesc);
        MethodMatcher methodMatcher = new MethodMatcher(jemMethod);
        //2、查询匹配的类
        Collection<Method> methods = ReflectClassManager.getInstance()
                .searchClassMethod(classMatcher, methodMatcher);
        //3、打印类信息
        Map<String, Object> methodInfos = getMethodInfos(methods);
        logger.info("get method info by classPattern:{},methodPattern:{},:{}", classPattern,
                methodPattern, methodInfos);
        session.sendCompleteMessage(MessageUtil.buildResponse(methodInfos));
    }

    public Map<String, Object> getMethodInfos(Collection<Method> methods) {
        Map<String, Object> methodInfos = new HashMap<>();
        for (Method method : methods) {
            Map<String, Object> data = new HashMap<>();
            data.put("method", method.getName());
            data.put("declaring-class", method.getDeclaringClass());
            data.put("modifier", tranModifier(method.getModifiers()));
            data.put("annotation", getAnnotations(method));
            data.put("parameters", getParameters(method));
            data.put("return", method.getReturnType().getName());
            data.put("exceptions", getExceptions(method));
            methodInfos.put(String.valueOf(method.hashCode()), data);
        }
        return methodInfos;
    }

    private String getAnnotations(Method method) {

        final StringBuilder annotationSB = new StringBuilder();
        final Annotation[] annotationArray = method.getDeclaredAnnotations();

        if (annotationArray.length > 0) {
            for (Annotation annotation : annotationArray) {
                annotationSB.append(annotation.annotationType().getName()).append(",");
            }
            annotationSB.deleteCharAt(annotationSB.length() - 1);
        } else {
            annotationSB.append(EMPTY);
        }

        return annotationSB.toString();
    }

    private String getParameters(Method method) {
        final StringBuilder paramsSB = new StringBuilder();
        final Class<?>[] paramTypes = method.getParameterTypes();
        for (Class<?> clazz : paramTypes) {
            paramsSB.append(clazz.getName()).append(";");
        }
        return paramsSB.toString();
    }

    private String getExceptions(Method method) {
        final StringBuilder exceptionSB = new StringBuilder();
        final Class<?>[] exceptionTypes = method.getExceptionTypes();
        for (Class<?> clazz : exceptionTypes) {
            exceptionSB.append(clazz.getName()).append("\n");
        }
        return exceptionSB.toString();
    }


}
