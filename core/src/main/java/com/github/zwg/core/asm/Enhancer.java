package com.github.zwg.core.asm;

import static java.lang.System.arraycopy;

import com.github.zwg.core.manager.GroupMatcher;
import com.github.zwg.core.manager.JemMethod;
import com.github.zwg.core.manager.MatchStrategy;
import com.github.zwg.core.manager.Matcher;
import com.github.zwg.core.manager.MethodMatcher;
import com.github.zwg.core.manager.ReflectClassManager;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.Type;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/5
 */
public class Enhancer {

    public static void enhance(Instrumentation inst, String sessionId, boolean isTracing,
            EnhancePoint point) {
        Map<Class<?>, Matcher<JemMethod>> enhanceMap = toEnhanceMap(point);
        if (enhanceMap.isEmpty()) {
            return;
        }
        EnhanceTransformer enhanceTransformer = new EnhanceTransformer(sessionId, isTracing,
                enhanceMap);
        try {
            inst.addTransformer(enhanceTransformer);
            int size = enhanceMap.size();
            Class<?>[] classArray = new Class<?>[size];
            arraycopy(enhanceMap.keySet().toArray(), 0, classArray, 0, size);
            inst.retransformClasses(classArray);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            inst.removeTransformer(enhanceTransformer);
        }
    }

    private static Map<Class<?>, Matcher<JemMethod>> toEnhanceMap(EnhancePoint point) {
        Map<Class<?>, Matcher<JemMethod>> enhanceMap = new HashMap<>();
        Collection<Method> methods = ReflectClassManager.getInstance()
                .searchClassMethod(point.getClassMatcher(), point.getMethodMatcher());
        for (Method method : methods) {
            Class<?> targetClass = method.getDeclaringClass();
            if (isIgnore(targetClass)) {
                continue;
            }
            Matcher<JemMethod> groupMatcher = enhanceMap.get(targetClass);
            if (groupMatcher == null) {
                groupMatcher = new GroupMatcher<>();
                enhanceMap.put(targetClass, groupMatcher);
            }
            if (groupMatcher instanceof GroupMatcher) {
                JemMethod jemMethod = new JemMethod(method.getName(),
                        Type.getType(method).toString());
                ((GroupMatcher) groupMatcher)
                        .add(new MethodMatcher(MatchStrategy.EQUALS, jemMethod));
            }
        }
        return enhanceMap;
    }

    private static boolean isIgnore(Class<?> clazz) {
        return clazz == null
                || isSelf(clazz)
                || isUnsafeClass(clazz)
                || isUnsupportedClass(clazz);
    }

    /**
     * 不能对代理包内的字节码进行增强
     */
    private static boolean isSelf(Class<?> clazz) {
        return clazz.getClassLoader() != null && clazz.getClassLoader()
                .equals(Enhancer.class.getClassLoader());
    }

    /**
     * 不能对RootClassLoader加载的类进行增强
     */
    private static boolean isUnsafeClass(Class<?> clazz) {
        return clazz.getClassLoader() == null;
    }

    /**
     * 不能对数组，接口，枚举进行增强
     */
    private static boolean isUnsupportedClass(Class<?> clazz) {
        return clazz.isArray()
                || clazz.isInterface()
                || clazz.isEnum();
    }
}
