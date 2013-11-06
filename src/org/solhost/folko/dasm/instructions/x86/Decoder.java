package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;

public class Decoder {
    private final ByteSequence seq;
    private final Context context;

    public Decoder(ByteSequence image) {
        this.seq = image;
        this.context = new Context(CPUMode.PROTECTED);
    }

    public Instruction decodeNext() {
        decodePrefix();

        short opcode = seq.readUByte();
        seq.skip(-1);

        switch(opcode) {
        case 0x03:
            return new OpAdd(seq, context);
        case 0x0F:
            return decodeNextTwoByte0F();
        case 0x25:
            return new OpAnd(seq, context);
        case 0x33:
            return new OpXor(seq, context);
        case 0x3C:
            return new OpCmp(seq, context);
        case 0x40:
        case 0x41:
        case 0x42:
        case 0x43:
        case 0x44:
        case 0x45:
        case 0x46:
        case 0x47:
            return new OpInc(seq, context);
        case 0x50:
        case 0x51:
        case 0x52:
        case 0x53:
        case 0x54:
        case 0x55:
        case 0x56:
        case 0x57:
            return new OpPush(seq, context);
        case 0x58:
        case 0x59:
        case 0x5A:
        case 0x5B:
        case 0x5C:
        case 0x5D:
        case 0x5E:
        case 0x5F:
            return new OpPop(seq, context);
        case 0x68:
        case 0x6A:
            return new OpPush(seq, context);
        case 0x70:
        case 0x71:
        case 0x72:
        case 0x73:
        case 0x74:
        case 0x75:
        case 0x76:
        case 0x77:
        case 0x78:
        case 0x79:
        case 0x7A:
        case 0x7B:
        case 0x7C:
        case 0x7D:
        case 0x7E:
        case 0x7F:
            return new OpGroup70(seq, context);
        case 0x80:
        case 0x81:
        case 0x82:
        case 0x83:
            return new OpGroup80(seq, context);
        case 0x84:
        case 0x85:
            return new OpTest(seq, context);
        case 0x89:
        case 0x8A:
        case 0x8B:
            return new OpMov(seq, context);
        case 0x8D:
            return new OpLea(seq, context);
        case 0xA0:
        case 0xA1:
        case 0xA3:
        case 0xB8:
        case 0xB9:
        case 0xBA:
        case 0xBB:
        case 0xBC:
        case 0xBD:
        case 0xBE:
        case 0xBF:
            return new OpMov(seq, context);
        case 0xC1:
            return new OpC1(seq, context);
        case 0xC3:
            return new OpRet(seq, context);
        case 0xC7:
            return new OpMov(seq, context);
        case 0xC9:
            return new OpLeave(seq, context);
        case 0xE8:
            return new OpCall(seq, context);
        case 0xE9:
        case 0xEB:
            return new OpJmp(seq, context);
        case 0xF6:
            return new OpGroupF6(seq, context);
        case 0xFF:
            return new OpFF(seq, context);
        default:
            return new OpUnknown(seq, context);
        }
    }

    private Instruction decodeNextTwoByte0F() {
        seq.readUByte(); // 0x0F
        short opcode2 = seq.readUByte();
        seq.skip(-2);

        switch(opcode2) {
        case 0x80:
        case 0x81:
        case 0x82:
        case 0x83:
        case 0x84:
        case 0x85:
        case 0x86:
        case 0x87:
        case 0x88:
        case 0x89:
        case 0x8A:
        case 0x8B:
        case 0x8C:
        case 0x8D:
        case 0x8E:
        case 0x8F:
            return new OpGroup0F80(seq, context);
        default:
            return new OpUnknown(seq, context);
        }
    }

    private void decodePrefix() {
        context.reset();
        boolean changed = false;
        do {
            changed = true;
            short id = seq.readUByte();
            switch(id) {
            case 0xF0: context.setLockPrefix(true); break;
            case 0xF2: context.setRepNZPrefix(true); break;
            case 0xF3: context.setRepZPrefix(true);; break;
            case 0x2E: context.setSegmentOverride(Segment.CS); break;
            case 0x36: context.setSegmentOverride(Segment.SS); break;
            case 0x3E: context.setSegmentOverride(Segment.DS); break;
            case 0x26: context.setSegmentOverride(Segment.ES); break;
            case 0x64: context.setSegmentOverride(Segment.FS); break;
            case 0x65: context.setSegmentOverride(Segment.GS); break;
            case 0x66: context.setOpSizePrefix(true); break;
            case 0x67: context.setAdrSizePrefix(true); break;
            default:
                seq.skip(-1);
                changed = false;
            }
        } while(changed);
    }
}
