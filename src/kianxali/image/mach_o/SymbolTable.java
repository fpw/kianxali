package kianxali.image.mach_o;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import kianxali.image.ByteSequence;

// Source of information: http://code.google.com/p/networkpx/source/browse/etc/idc/dyldinfo.idc

public class SymbolTable {
    private static final Logger LOG = Logger.getLogger("kianxali.image.mach_o");

    private static final short OPCODE_DONE                              = 0x00;
    private static final short OPCODE_SET_DYLIB_ORDINAL_IMM             = 0x10;
    private static final short OPCODE_SET_DYLIB_ORDINAL_ULEB            = 0x20;
    private static final short OPCODE_SET_DYLIB_SPECIAL_IMM             = 0x30;
    private static final short OPCODE_SET_SYMBOL_TRAILING_FLAGS_IMM     = 0x40;
    private static final short OPCODE_SET_TYPE_IMM                      = 0x50;
    private static final short OPCODE_SET_ADDEND_SLEB                   = 0x60;
    private static final short OPCODE_SET_SEGMENT_AND_OFFSET_ULEB       = 0x70;
    private static final short OPCODE_ADD_ADDR_ULEB                     = 0x80;
    private static final short OPCODE_DO_BIND                           = 0x90;
    private static final short OPCODE_DO_BIND_ADD_ADDR_ULEB             = 0xA0;
    private static final short OPCODE_DO_BIND_ADD_ADDR_IMM_SCALED       = 0xB0;
    private static final short OPCODE_DO_BIND_ULEB_TIMES_SKIPPING_ULEB  = 0xC0;

    private final long offset, size;
    private final Map<Long, String> symbols;

    public SymbolTable(ByteSequence seq, boolean mach64) {
        symbols = new HashMap<>();

        seq.skip(8); // rebase info
        seq.skip(8); // binding info
        seq.skip(8); // weak binding info

        offset = seq.readUDword();
        size = seq.readUDword();

        seq.skip(8); // export info
    }

    private long readULEB(ByteSequence seq) {
        long res = 0, bit = 0;
        short c;

        do {
            c = seq.readUByte();
            res |= ((c & 0x7F) << bit);
            bit += 7;
        } while((c & 0x80) != 0);
        return res;
    }

    public void load(List<MachSegment> segments, ByteSequence seq, boolean mach64) {
        seq.seek(offset);
        String symbolName = null;
        long addr = 0;
        while(seq.getPosition() - offset < size) {
            short data = seq.readUByte();
            short imm = (short) (data & 0x0F);
            short opcode = (short) (data & 0xF0);

            switch(opcode) {
            case OPCODE_SET_DYLIB_ORDINAL_ULEB:
            case OPCODE_SET_ADDEND_SLEB:
                // we don't need this but have to read the ULEB to stay in sync with the opcode stream
                readULEB(seq);
                break;
            case OPCODE_SET_SYMBOL_TRAILING_FLAGS_IMM:
                symbolName = seq.readString();
                break;
            case OPCODE_SET_SEGMENT_AND_OFFSET_ULEB:
                addr = segments.get(imm).getVirtualAddress() + readULEB(seq);
                break;
            case OPCODE_ADD_ADDR_ULEB:
                addr += readULEB(seq);
                break;
            case OPCODE_DO_BIND:
                symbols.put(addr, symbolName);
                addr += 4;
                break;
            case OPCODE_DO_BIND_ADD_ADDR_ULEB:
                symbols.put(addr, symbolName);
                addr += 4 + readULEB(seq);
                break;
            case OPCODE_DO_BIND_ADD_ADDR_IMM_SCALED:
                symbols.put(addr, symbolName);
                addr += 4 * (imm + 1);
                break;
            case OPCODE_DO_BIND_ULEB_TIMES_SKIPPING_ULEB:
                long count = readULEB(seq);
                long skip = readULEB(seq);
                symbols.put(addr, symbolName);
                addr += count * (4 + skip);
                break;
            case OPCODE_SET_DYLIB_ORDINAL_IMM:
            case OPCODE_SET_DYLIB_SPECIAL_IMM:
            case OPCODE_SET_TYPE_IMM:
            case OPCODE_DONE:
                // we don't need these at all
                break;
            default:
                LOG.warning("Invalid bind opcode: " + opcode);
                return;
            }
        }
    }

    public Map<Long, String> getSymbols() {
        return symbols;
    }
}
