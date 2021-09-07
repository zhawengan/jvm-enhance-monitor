package com.github.zwg.core.manager;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jdk.internal.org.objectweb.asm.Type;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/3
 */
public class ReflectClassManager {

    private static ReflectClassManager instance;

    private Collection<Class<?>> loadedClasses;

    private ReflectClassManager() {
        loadedClasses = new ArrayList<>();
    }

    //默认通配符匹配
    public Collection<Class<?>> searchClass(Matcher<String> matcher) {
        Collection<Class<?>> matchClasses = new ArrayList<>();
        loadedClasses.forEach(o -> {
            if (matcher.match(o.getName())) {
                matchClasses.add(o);
            }
        });
        return matchClasses;
    }

    public Collection<Method> searchClassMethod(Matcher<String> classMatcher,Matcher<JemMethod> methodMatcher){
        Collection<Class<?>> classes = searchClass(classMatcher);
        Collection<Method> methods = new ArrayList<>();
        for (Class<?> clazz:classes) {
            Set<Method> classMethods = getClassMethods(clazz);
            classMethods.forEach(o->{
                String methodDesc = Type.getType(o).toString();
                JemMethod jemMethod = new JemMethod(o.getName(),methodDesc);
                if(methodMatcher.match(jemMethod)){
                    methods.add(o);
                }
            });
        }
        return methods;
    }


    public Set<Method> getClassMethods(Class<?> clazz){
        Set<Method> methods = new HashSet<>();
        //获取当前类的所有方法
        Method[] declaredMethods = clazz.getDeclaredMethods();
        methods.addAll(Arrays.asList(declaredMethods));
        //获取所有父类
        List<Class<?>> supperClasses = getSupperClasses(clazz);
        for (Class<?> supperClass: supperClasses) {
            //遍历父类的方法，过滤掉private的
            Method[] supperClassDeclaredMethods = supperClass.getDeclaredMethods();
            for (Method method:supperClassDeclaredMethods) {
                if(Modifier.isPrivate(method.getModifiers())){
                    continue;
                }
                methods.add(method);
            }
        }
        return methods;
    }


    private List<Class<?>> getSupperClasses(Class<?> clazz) {
        List<Class<?>> classes = new ArrayList<>();
        Class<?> targetClass = clazz;
        do {
            Class<?> superclass = targetClass.getSuperclass();
            if (superclass == null) {
                break;
            }
            classes.add(superclass);
            targetClass = superclass;
        } while (true);
        return classes;
    }



    public void initLoadedClass(Collection<Class<?>> classes) {
        if (classes != null) {
            this.loadedClasses.addAll(classes);
        }
    }

    public static ReflectClassManager getInstance() {
        if (instance == null) {
            synchronized (ReflectClassManager.class) {
                if (instance == null) {
                    instance = new ReflectClassManager();
                }
            }
        }
        return instance;
    }

}
