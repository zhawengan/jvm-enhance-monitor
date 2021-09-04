package com.github.zwg.core.asm;

import java.io.PrintWriter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceAnnotationVisitor;
import org.objectweb.asm.util.TraceFieldVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/4
 */
public class AsmTraceClassVisitor extends ClassVisitor {

    private final PrintWriter pw;
    public final Printer p;

    public AsmTraceClassVisitor(PrintWriter pw) {
        super(Opcodes.ASM5, null);
        this.pw = pw;
        this.p = new Textifier();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces) {
        p.visit(version, access, name, signature, superName, interfaces);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        p.visitSource(source, debug);
        super.visitSource(source, debug);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        p.visitOuterClass(owner, name, descriptor);
        super.visitOuterClass(owner, name, descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        Printer printer = p.visitClassAnnotation(descriptor, visible);
        AnnotationVisitor av = this.cv == null ? null : cv.visitAnnotation(descriptor, visible);
        return new TraceAnnotationVisitor(av, printer);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
            boolean visible) {
        Printer printer = p.visitClassTypeAnnotation(typeRef, typePath, descriptor, visible);
        AnnotationVisitor av = this.cv == null ? null
                : cv.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        return new TraceAnnotationVisitor(av, printer);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        p.visitClassAttribute(attribute);
        super.visitAttribute(attribute);
    }


    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        p.visitInnerClass(name, outerName, innerName, access);
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature,
            Object value) {
        Printer printer = p.visitField(access, name, descriptor, signature, value);
        FieldVisitor fv =
                cv == null ? null : cv.visitField(access, name, descriptor, signature, value);
        return new TraceFieldVisitor(fv, printer);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
            String[] exceptions) {
        Printer printer = this.p.visitMethod(access, name, descriptor, signature, exceptions);
        MethodVisitor mv =
                cv == null ? null : cv.visitMethod(access, name, descriptor, signature, exceptions);
        return new TraceMethodVisitor(mv, printer);
    }

    @Override
    public void visitEnd() {
        p.visitClassEnd();
        if (pw != null) {
            p.print(pw);
            pw.flush();
        }
        super.visitEnd();
    }
}
