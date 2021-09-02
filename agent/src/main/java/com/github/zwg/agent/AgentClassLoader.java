package com.github.zwg.agent;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/30
 */
public class AgentClassLoader extends URLClassLoader {

    public AgentClassLoader(URL[] urls) {
        super(urls);
    }

    /**
     * 自定义classLoader加载模式， 优先自己加载类，失败后委托父类加载
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        //判断类是否已经当前类加载器加载
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }
        try {
            //当前类加载器优先加载
            Class<?> aClass = findClass(name);
            if (resolve) {
                resolveClass(aClass);
            }
            return aClass;
        } catch (Exception ex) {
            //当前类加载器加载失败后，委托父类加载器尝试
            return super.loadClass(name, resolve);
        }

    }
}
