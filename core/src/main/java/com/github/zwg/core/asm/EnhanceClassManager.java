package com.github.zwg.core.asm;

import static java.lang.System.arraycopy;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/4
 */
public class EnhanceClassManager {

    private static EnhanceClassManager manager;
    /**
     * 类的原始字节码
     */
    private final Map<Class<?>, byte[]> classCache = new HashMap<>();

    private EnhanceClassManager() {

    }

    public static EnhanceClassManager getInstance() {
        if (manager == null) {
            synchronized (EnhanceClassManager.manager) {
                if (manager == null) {
                    manager = new EnhanceClassManager();
                }
            }
        }
        return manager;
    }

    public byte[] get(Class<?> clazz) {
        return classCache.get(clazz);
    }

    public void put(Class<?> clazz, byte[] byteCodes) {
        classCache.put(clazz, byteCodes);
    }

    public synchronized void reset(Instrumentation inst) {
        if (classCache.isEmpty()) {
            return;
        }
        ClassFileTransformer transformer = (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> null;
        try {
            inst.addTransformer(transformer);
            int size = classCache.size();
            Class<?>[] classes = new Class[size];
            arraycopy(classCache.keySet().toArray(), 0, classes, 0, size);
            inst.retransformClasses(classes);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            inst.removeTransformer(transformer);
            classCache.clear();
        }
    }

}
