package com.github.zwg.core.asm;

import com.github.zwg.core.manager.JemMethod;
import com.github.zwg.core.manager.Matcher;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;

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

    private boolean isIgnore(MethodVisitor mv,int access,String name,String desc){
        return mv==null
                || isAbstract(access)
                || "<clinit>".equals(name)
                || !methodMatcher.match(new JemMethod(name,desc));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
            String[] exceptions) {

        MethodVisitor mv = super
                .visitMethod(access, name, descriptor, signature, exceptions);
        if(isIgnore(mv,access,name,descriptor)){
            return mv;
        }
        return new AdviceAdapter(ASM5,new JSRInlinerAdapter(mv,access,name,descriptor,signature,exceptions),access,name,descriptor) {

            @Override
            protected void onMethodEnter() {
                super.onMethodEnter();
            }

            @Override
            protected void onMethodExit(int opcode) {
                super.onMethodExit(opcode);
            }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                super.visitMaxs(maxStack, maxLocals);
            }

            @Override
            public void visitInsn(int opcode) {
                super.visitInsn(opcode);
            }

            @Override
            public void visitLineNumber(int line, Label start) {
                super.visitLineNumber(line, start);
            }

            @Override
            public void visitMethodInsn(int opcodeAndSource, String owner, String name,
                    String descriptor, boolean isInterface) {
                super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);
            }

            @Override
            public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                super.visitTryCatchBlock(start, end, handler, type);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
            }
        };
    }
}
