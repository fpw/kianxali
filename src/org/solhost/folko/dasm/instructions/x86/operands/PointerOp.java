package org.solhost.folko.dasm.instructions.x86.operands;

import org.solhost.folko.dasm.instructions.x86.Context;
import org.solhost.folko.dasm.instructions.x86.Register;
import org.solhost.folko.dasm.instructions.x86.Context.OperandSize;

public class PointerOp implements Operand {
    private final Context context;
    private Register baseRegister, indexRegister;
    private Integer indexScale;
    private Long offset;

    // ptr [address]
    public PointerOp(Context ctx, long offset) {
        this.context = ctx;
        this.offset = offset;
    }

    // ptr [register]
    public PointerOp(Context ctx, Register baseRegister) {
        this.context = ctx;
        this.baseRegister = baseRegister;
    }

    public PointerOp(Context ctx, Register baseRegister, long offset) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        this.offset = offset;
    }

    // ptr [scale * register]
    public PointerOp(Context ctx, Register indexRegister, int scale) {
        this.context = ctx;
        this.indexRegister = indexRegister;
        if(scale > 1) {
            this.indexScale = scale;
        }
    }

    // ptr [scale * register + offset]
    public PointerOp(Context ctx, Register indexRegister, int scale, long offset) {
        this.context = ctx;
        this.indexRegister = indexRegister;
        if(scale > 1) {
            this.indexScale = scale;
        }
        this.offset = offset;
    }

    // ptr [base + scale * index]
    public PointerOp(Context ctx, Register baseRegister, int scale, Register indexRegister) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        if(scale > 1) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
    }

    // ptr [scale * index + offset]
    public PointerOp(Context ctx, int scale, Register indexRegister, long offset) {
        this.context = ctx;
        if(scale > 1) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
        this.offset = offset;
    }

    // ptr [base + scale * index + offset]
    public PointerOp(Context ctx, Register baseRegister, int scale, Register indexRegister, long offset) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        if(scale > 1) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
        this.offset = offset;
    }

    @Override
    public String asString(int flags) {
        StringBuilder str = new StringBuilder();
        OperandSize size = context.getOperandSize();
        if(context.getOpSizeOverride() != null) {
            size = context.getOpSizeOverride();
        }

        switch(size) {
        case O8: str.append("byte ptr "); break;
        case O16: str.append("word ptr "); break;
        case O32: str.append("dword ptr "); break;
        default: throw new RuntimeException("invalid operand size: " + context.getOperandSize());
        }

        if(context.getSegmentOverride() != null) {
            str.append(context.getSegmentOverride() + ":");
        }

        str.append("[");
        boolean needsPlus = false;
        if(baseRegister != null) {
            str.append(baseRegister);
            needsPlus = true;
        }
        if(indexScale != null) {
            if(needsPlus) {
                str.append(" + ");
            }
            str.append(indexScale + " * ");
            needsPlus = false;
        }
        if(indexRegister != null) {
            if(needsPlus) {
                str.append(" + ");
            }
            str.append(indexRegister);
            needsPlus = true;
        }
        if(offset != null) {
            if(needsPlus) {
                str.append(offset < 0 ? " - " : " + ");
            }
            if(offset < 0) {
                str.append(String.format("%Xh", -offset));
            } else {
                str.append(String.format("%Xh", offset));
            }
        }
        str.append("]");

        return str.toString();
    }

    @Override
    public String toString() {
        return asString(0);
    }
}
