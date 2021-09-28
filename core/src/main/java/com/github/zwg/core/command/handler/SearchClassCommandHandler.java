package com.github.zwg.core.command.handler;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.github.zwg.core.annotation.Arg;
import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.ParamConstant;
import com.github.zwg.core.manager.MatchStrategy;
import com.github.zwg.core.manager.ReflectClassManager;
import com.github.zwg.core.manager.SearchMatcher;
import com.github.zwg.core.netty.MessageUtil;
import com.github.zwg.core.session.Session;
import com.github.zwg.core.util.ClassUtil;
import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
@Cmd(name = ParamConstant.COMMAND_SEARCH_CLASS)
public class SearchClassCommandHandler implements CommandHandler {

    @Arg(name = ParamConstant.CLASS_KEY, description = "find class expression")
    private String classPattern;

    @Arg(name = ParamConstant.REG_KEY, required = false, defaultValue = "WILDCARD", description = "expression matching rules: wildcard, regular, equal")
    private String strategy;


    @Override
    public void execute(Session session, Instrumentation inst) {
        SearchMatcher searchMatcher = new SearchMatcher(MatchStrategy.valueOf(strategy),
                classPattern);
        //2、查询匹配的类
        Collection<Class<?>> classes = ReflectClassManager.getInstance().searchClass(searchMatcher);
        //3、打印类信息
        session.sendCompleteMessage(MessageUtil.buildResponse(getClassInfos(classes)));
    }


    public Map<String, Object> getClassInfos(Collection<Class<?>> classes) {
        Map<String, Object> data = new HashMap<>();
        for (Class<?> clazz : classes) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("isInterface", clazz.isInterface());
            detail.put("isAnnotation", clazz.isAnnotation());
            detail.put("isEnum", clazz.isEnum());
            detail.put("isAnonymousClass", clazz.isAnonymousClass());
            detail.put("isArray", clazz.isArray());
            detail.put("isLocalClass", clazz.isLocalClass());
            detail.put("isMemberClass", clazz.isMemberClass());
            detail.put("isPrimitive", clazz.isPrimitive());
            detail.put("isSynthetic", clazz.isSynthetic());
            detail.put("simple-name", clazz.getSimpleName());
            detail.put("modifier", ClassUtil.tranModifier(clazz.getModifiers()));
            detail.put("annotation", getAnnotations(clazz));
            detail.put("interfaces", getInterfaces(clazz));
            detail.put("super-class", getSuperClasses(clazz));
            detail.put("class-loader", getClassLoader(clazz));
            detail.put("fields", getFields(clazz));
            data.put(clazz.getName(), detail);
        }
        return data;
    }

    private List<String> getFields(Class<?> clazz) {

        List<String> fieldInfo = new ArrayList<>();
        final Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String info = String.format("modifier/type/name = %s/%s/%s",
                    ClassUtil.tranModifier(field.getModifiers()), field.getType().getName(),
                    field.getName());
            fieldInfo.add(info);
        }
        return fieldInfo;
    }

    private String getAnnotations(Class<?> clazz) {
        final StringBuilder annotationSB = new StringBuilder();
        final Annotation[] annotationArray = clazz.getDeclaredAnnotations();
        if (annotationArray.length > 0) {
            for (Annotation annotation : annotationArray) {
                annotationSB.append(annotation.annotationType()).append(",");
            }
            if (annotationSB.length() > 0) {
                annotationSB.deleteCharAt(annotationSB.length() - 1);
            }
        } else {
            annotationSB.append(EMPTY);
        }
        return annotationSB.toString();
    }

    private String getInterfaces(Class<?> clazz) {
        final StringBuilder interfaceSB = new StringBuilder();
        final Class<?>[] interfaceArray = clazz.getInterfaces();
        if (interfaceArray.length == 0) {
            interfaceSB.append(EMPTY);
        } else {
            for (Class<?> i : interfaceArray) {
                interfaceSB.append(i.getName()).append(",");
            }
            if (interfaceSB.length() > 0) {
                interfaceSB.deleteCharAt(interfaceSB.length() - 1);
            }
        }
        return interfaceSB.toString();
    }

    private String getSuperClasses(Class<?> clazz) {
        Class<?> superClass = clazz.getSuperclass();
        String classNames = "";
        if (null != superClass) {
            classNames = superClass.getName();
            while (true) {
                superClass = superClass.getSuperclass();
                if (null == superClass) {
                    break;
                }
                classNames = classNames + "," + superClass.getName();
            }
        }
        return classNames;
    }


    private String getClassLoader(Class<?> clazz) {
        String classLoaderNames = "";
        ClassLoader loader = clazz.getClassLoader();
        if (null != loader) {
            classLoaderNames = loader.toString();
            while (true) {
                loader = loader.getParent();
                if (null == loader) {
                    break;
                }
                classLoaderNames = classLoaderNames + "," + loader.toString();
            }
        }
        return classLoaderNames;
    }

}
