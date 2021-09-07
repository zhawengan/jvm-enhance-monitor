package com.github.zwg.core.asm;

import com.github.zwg.core.manager.JemMethod;
import com.github.zwg.core.manager.Matcher;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/5
 */
public class EnhanceTransformer implements ClassFileTransformer {

    private final String sessionId;
    private final boolean isTracing;
    private final Map<Class<?>, Matcher<JemMethod>> target;

    public EnhanceTransformer(String sessionId, boolean isTracing, Map<Class<?>, Matcher<JemMethod>> target) {
        this.sessionId = sessionId;
        this.isTracing = isTracing;
        this.target = target;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        //过滤掉非目标类
        if (target.get(classBeingRedefined) == null) {
            return null;
        }
        byte[] byteCodes = EnhanceClassManager.getInstance().get(classBeingRedefined);
        if (byteCodes == null) {
            byteCodes = classfileBuffer;
        }
        ClassReader cr = new ClassReader(byteCodes);
        Matcher<JemMethod> methodMatcher = target.get(classBeingRedefined);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cr.accept(new AdviceClassVisitor(sessionId, isTracing, className, methodMatcher, cw),
                ClassReader.EXPAND_FRAMES);
        byte[] classBytes = cw.toByteArray();
        EnhanceClassManager.getInstance().put(classBeingRedefined, classBytes);
        return classBytes;
    }
}
