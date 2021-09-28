package com.github.zwg.core.asm;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/6
 */
public interface CodeLock {

    /**
     * 通过对字节码的判断，决定当前代码是锁定还是解锁
     */
    void lockOrUnlock(int opcode);

    /**
     * 判断当前代码是否还在锁定中
     */
    boolean isLock();

    /**
     * 将代码块纳入锁保护范围
     */
    void lockBlockCode(BlockCode blockCode);

    interface BlockCode {

        /**
         * 代码
         */
        void runningCodes();
    }
}
