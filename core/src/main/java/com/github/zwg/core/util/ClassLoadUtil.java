package com.github.zwg.core.util;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
                    } else if ("jar".equals(url.getProtocol())) {
                        findJarClassAndLoad(url, packageDirName, classes);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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
                e.printStackTrace();
            }
        }
    }

    public static void findJarClassAndLoad(URL url, String packageDirName, Set<Class<?>> classes) {
        JarFile jar;
        try {
            jar = ((JarURLConnection) url.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                if (name.charAt(0) == '/') {
                    name = name.substring(1);
                }
                if (name.startsWith(packageDirName)) {
                    int idx = name.lastIndexOf('/');
                    String packageName = "";
                    if (idx != -1) {
                        packageName = name.substring(0, idx).replace('/', '.');
                    }

                    if (name.endsWith(".class") && !jarEntry.isDirectory()) {
                        String className = name
                                .substring(packageName.length() + 1, name.length() - 6);
                        try {
                            classes.add(Class.forName(packageName + "." + className));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
