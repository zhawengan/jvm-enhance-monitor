package com.github.zwg.core.util;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/2
 */
public class ClassLoadUtil {

    public static Set<Class<?>> scanPackage(ClassLoader classLoader, String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        if (!StringUtils.isBlank(packageName)) {
            String packageDirName = packageName.replace('.', '/');
            try {
                Enumeration<URL> resources = classLoader.getResources(packageDirName);
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    if ("file".equals(url.getProtocol())) {
                        String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                        findClassAndLoad(classLoader, packageName, filePath, classes);
                    }
                }
            } catch (Exception ex) {

            }
        }
        return classes;
    }

    public static void findClassAndLoad(ClassLoader classLoader, String packageName,
            String packagePath, Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles(file -> file.getName().endsWith(".class"));
        for (File file : files) {
            String className = file.getName().substring(0, file.getName().length() - 6);
            try {
                classes.add(classLoader.loadClass(packageName + "." + className));
            } catch (ClassNotFoundException e) {

            }
        }
    }

}
