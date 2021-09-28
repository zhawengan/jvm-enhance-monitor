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

    /**
     * 通过opcode判断是解锁还是上锁的基本原则是 1、连续读到两个opcode,且这两个opcode的值及顺序完全和beginCodeArray或者endCodeArray一致
     * 2、找到连续的匹配点后，锁状态直接变更
     */
    @Override
    public void lockOrUnlock(int opcode) {
        int[] codes = isLock() ? endCodeArray : beginCodeArray;
        if (index >= codes.length) {
            reset();
            return;
        }
        /**
         * 如果只是第一个或者第二个opcode与数组值不匹配，表示只是偶然的业务代码，
         * index重置
         */
        if (codes[index] != opcode) {
            reset();
            return;
        }
        /**
         * 到得此处，表示codes[index]==opcode, index的值是0或者1
         * 所以++index==codes.length.表示完全匹配到了一个锁位置
         */
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
