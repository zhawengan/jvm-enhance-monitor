package com.github.zwg.core.asm;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/6
 */
public class AsmCodeLock implements CodeLock, Opcodes {

    private boolean isLock;

    private int[] beginCodeArray = new int[]{ICONST_0, POP};

    private int[] endCodeArray = new int[]{ICONST_1, POP};

    private AdviceAdapter aa;

    private int index = 0;

    public AsmCodeLock(AdviceAdapter aa) {
        this.aa = aa;
    }

    @Override
    public void lockOrUnlock(int opcode) {
        int[] codes = isLock() ? endCodeArray : beginCodeArray;
        if (index >= codes.length) {
            reset();
            return;
        }
        if (codes[index] != opcode) {
            reset();
            return;
        }
        if (++index == codes.length) {
            isLock = !isLock;
            reset();
        }
    }

    @Override
    public boolean isLock() {
        return isLock;
    }

    @Override
    public void lockBlockCode(BlockCode blockCode) {
        lock();
        try {
            blockCode.runningCodes();
        } finally {
            unlock();
        }

    }

    private void lock() {
        for (int opcode : beginCodeArray) {
            aa.visitInsn(opcode);
        }
    }

    private void unlock() {
        for (int opcode : endCodeArray) {
            aa.visitInsn(opcode);
        }
    }

    private void reset() {
        index = 0;
    }
}
