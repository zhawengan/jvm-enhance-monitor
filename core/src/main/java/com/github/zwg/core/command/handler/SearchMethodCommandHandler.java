package com.github.zwg.core.command.handler;

import static com.github.zwg.core.util.ClassModifierUtil.tranModifier;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.github.zwg.core.command.Command;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.MonitorCallback;
import com.github.zwg.core.execption.BadCommandException;
import com.github.zwg.core.manager.JemMethod;
import com.github.zwg.core.manager.MatchStrategy;
import com.github.zwg.core.manager.MethodMatcher;
import com.github.zwg.core.manager.ReflectClassManager;
import com.github.zwg.core.manager.SearchMatcher;
import com.github.zwg.core.session.Session;
import com.github.zwg.core.util.ParamConstant;
import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public class SearchMethodCommandHandler implements CommandHandler {

    @Override
    public String getCommandName() {
        return "sm";
    }

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
        JemMethod  jemMethod = new JemMethod(methodPattern,methodDesc);
        MethodMatcher methodMatcher = new MethodMatcher(jemMethod);
        //2、查询匹配的类
        Collection<Method> methods = ReflectClassManager.getInstance()
                .searchClassMethod(classMatcher, methodMatcher);
        //3、打印类信息
        callback.execute(getMethodInfos(methods));
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
