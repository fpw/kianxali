package kianxali.decoder;

import kianxali.loader.ByteSequence;
import kianxali.util.OutputFormatter;

/**
 * This class represents data references that can be yielded by operands.
 * The data can be used for further analysis by higher level classes.
 * @author fwi
 *
 */
public class Data implements DecodedEntity {
    /** Data type that an instance can represent */
    public enum DataType {
        BYTE, WORD, DWORD, QWORD, DQWORD, DYWORD,
        FLOAT, DOUBLE,
        STRING,
        FUN_PTR, UNKNOWN, JUMP_TABLE;
    }

    private final long memAddr;
    private DataType type;
    private Object content;
    private int tableScaling; // for data arrays

    /**
     * Construct a new data item
     * @param memAddr the address of the data
     * @param type the type of data
     */
    public Data(long memAddr, DataType type) {
        this.memAddr = memAddr;
        this.type = type;
    }

    /**
     * Change the data type
     * @param type new type
     */
    public void setType(DataType type) {
        this.type = type;
    }

    /**
     * If the data is a table, this sets the size of the
     * entries
     * @param tableScaling size of the entries in the table
     */
    public void setTableScaling(int tableScaling) {
        this.tableScaling = tableScaling;
    }

    /**
     * If the data is a table, this returns the size of its entries
     * @return the size of a table entry
     */
    public int getTableScaling() {
        return tableScaling;
    }

    /**
     * Actually analyze the contents of the memory address
     * @param seq a byte sequence already pointing to the correct address
     */
    public void analyze(ByteSequence seq) {
        String str = checkForString(seq);
        if(str != null) {
            content = str;
            return;
        }

        switch(type) {
        case BYTE:          content = seq.readUByte(); break;
        case WORD:          content = seq.readUWord(); break;
        case DWORD:         content = seq.readUDword(); break;
        case QWORD:         content = seq.readSQword(); break;
        case DQWORD:        content = seq.readSQword(); break; // FIXME
        case FLOAT:         content = seq.readFloat(); break;
        case DOUBLE:        content = seq.readDouble(); break;
        case FUN_PTR:       content = seq.readUDword(); break; // FIXME -> 32 || 64
        case STRING:        content = seq.readString(); break;
        case UNKNOWN:       content = seq.readUByte(); break;
        case DYWORD:        content = seq.readSDword(); break;
        default:            content = seq.readUByte(); break;
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
        case BYTE:          return 1;
        case WORD:          return 2;
        case DWORD:         return 4;
        case QWORD:         return 8;
        case DQWORD:        return 16;
        case DYWORD:        return 64;
        case FLOAT:         return 4;
        case DOUBLE:        return 8;
        case FUN_PTR:       return 4; // FIXME
        case STRING:        return 1; // FIXME
        case UNKNOWN:       return 1;
        default:            return 1;
        }
    }

    /**
     * Returns the type of the associated data
     * @return the type of the associated data
     */
    public DataType getType() {
        return type;
    }

    /**
     * Returns an arbitrary object that was decoded when
     * analyzing the data, can be null.
     * @return an arbitrary object representing the decoded data
     */
    public Object getRawContent() {
        return content;
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

    @Override
    public String toString() {
        if(content != null) {
            return content.toString();
        } else {
            return "<empty data>";
        }
    }
}
