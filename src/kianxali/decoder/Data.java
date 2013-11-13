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
    private final DataType type;

    private Object content;

    public Data(long memAddr, DataType type) {
        this.memAddr = memAddr;
        this.type = type;
    }

    public void analyze(ByteSequence seq) {
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

    @Override
    public long getMemAddress() {
        return memAddr;
    }

    public DataType getType() {
        return type;
    }

    @Override
    public String asString(OutputFormatter format) {
        return "<" + type + ": " + content + ">";
    }
}
