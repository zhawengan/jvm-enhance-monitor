package com.github.zwg.core.asm;

import com.github.zwg.core.manager.JemMethod;
import com.github.zwg.core.manager.Matcher;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/4
 */
public class AdviceClassVisitor extends ClassVisitor implements Opcodes {

    private final String sessionId;
    private final boolean isTracing;
    private final String internalClassName;
    private final Matcher<JemMethod> methodMatcher;

    public AdviceClassVisitor(String sessionId, boolean isTracing, String internalClassName,
            Matcher<JemMethod> methodMatcher, ClassVisitor cv) {
        super(ASM5, cv);
        this.sessionId = sessionId;
        this.isTracing = isTracing;
        this.internalClassName = internalClassName;
        this.methodMatcher = methodMatcher;
    }

    private boolean isAbstract(int access) {
        return (ACC_ABSTRACT & access) == ACC_ABSTRACT;
    }

    private boolean isIgnore(MethodVisitor mv, int access, String name, String desc) {
        return mv == null
                || isAbstract(access)
                || "<clinit>".equals(name)
                || !methodMatcher.match(new JemMethod(name, desc));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
            String[] exceptions) {

        MethodVisitor mv = super
                .visitMethod(access, name, descriptor, signature, exceptions);
        if (isIgnore(mv, access, name, descriptor)) {
            return mv;
        }
        return new JemAdviceAdapter(sessionId, isTracing, internalClassName, mv, access, name,
                descriptor);
    }
}
