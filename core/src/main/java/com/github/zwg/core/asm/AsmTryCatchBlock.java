package com.github.zwg.core.asm;

import org.objectweb.asm.Label;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/7
 */
public class AsmTryCatchBlock {

    protected final Label start;
    protected final Label end;
    protected final Label handler;
    protected final String type;

    AsmTryCatchBlock(Label start, Label end, Label handler, String type) {
        this.start = start;
        this.end = end;
        this.handler = handler;
        this.type = type;
    }

    public Label getStart() {
        return start;
    }

    public Label getEnd() {
        return end;
    }

    public Label getHandler() {
        return handler;
    }

    public String getType() {
        return type;
    }
}
