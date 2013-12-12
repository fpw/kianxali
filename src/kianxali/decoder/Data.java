package kianxali.decoder;

import kianxali.image.ByteSequence;
import kianxali.util.OutputFormatter;

public class Data implements DecodedEntity {
    public enum DataType {
        BYTE, WORD, DWORD, QWORD, DQWORD,
        FLOAT, DOUBLE,
        STRING,
        FUN_PTR, UNKNOWN;
    }

    private final long memAddr;
    private DataType type;
    private Object content;

    public Data(long memAddr, DataType type) {
        this.memAddr = memAddr;
        this.type = type;
    }

    public void analyze(ByteSequence seq) {
        String str = checkForString(seq);
        if(str != null) {
            content = str;
            return;
        }

        switch(type) {
        case BYTE:      content = seq.readUByte(); break;
        case WORD:      content = seq.readUWord(); break;
        case DWORD:     content = seq.readUDword(); break;
        case QWORD:     content = seq.readSQword(); break;
        case DQWORD:    content = seq.readSQword(); break; // FIXME
        case FLOAT:     content = seq.readFloat(); break;
        case DOUBLE:    content = seq.readDouble(); break;
        case FUN_PTR:   content = seq.readUDword(); break; // FIXME -> 32 || 64
        case STRING:    content = seq.readString(); break;
        case UNKNOWN:   content = seq.readUByte(); break;
        default:        content = seq.readUByte();
        }
    }

    private String checkForString(ByteSequence seq) {
        long oldPos = seq.getPosition();
        StringBuilder res = new StringBuilder();
        boolean complete = false;
        do {
            byte b = seq.readSByte();
            if(b == 0) {
                complete = true;
                break;
            } else if(b == '\r' || b == '\n' || b >= 32) {
                res.append((char) b);
            } else {
                break;
            }
        } while(true);
        if(res.length() > 0 && complete) {
            type = DataType.STRING;
            return res.toString();
        } else {
            seq.seek(oldPos);
            return null;
        }
    }

    @Override
    public long getMemAddress() {
        return memAddr;
    }

    @Override
    public int getSize() {
        switch(type) {
        case BYTE:      return 1;
        case WORD:      return 2;
        case DWORD:     return 4;
        case QWORD:     return 8;
        case DQWORD:    return 16;
        case FLOAT:     return 4;
        case DOUBLE:    return 8;
        case FUN_PTR:   return 4; // FIXME
        case STRING:    return 1; // FIXME
        case UNKNOWN:   return 1;
        default:        return 1;
        }
    }

    public DataType getType() {
        return type;
    }

    @Override
    public String asString(OutputFormatter format) {
        if(content instanceof Number) {
            Number n = (Number) content;
            return "<" + type + ": " + format.formatImmediate(n.longValue()) + ">";
        } else {
            return "<" + type + ": " + content + ">";
        }
    }
}
