package kianxali.decoder.arch.x86;

import java.util.ArrayList;
import java.util.List;

import kianxali.decoder.arch.x86.X86CPU.Segment;

/**
 * This class is used to store the information that can be encoded in
 * prefixes to opcodes. The information are stored in flags and as raw
 * bytes (in case the opcode needs to check whether a mandatory prefix
 * is present).
 * @author fwi
 *
 */
public class Prefix {
    public Segment overrideSegment;
    public boolean lockPrefix, waitPrefix;
    public boolean repZPrefix, repNZPrefix, opSizePrefix, adrSizePrefix;
    public boolean rexWPrefix, rexRPrefix, rexBPrefix, rexXPrefix;
    public List<Short> prefixBytes;

    public Prefix() {
        prefixBytes = new ArrayList<>(5);
    }

    public void pushPrefixByte(short b) {
        prefixBytes.add(b);
    }

    public void popPrefixByte() {
        int size = prefixBytes.size();
        if(size > 0) {
            prefixBytes.remove(size - 1);
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if(lockPrefix) {
            res.append("lock ");
        } else if(waitPrefix) {
            res.append("wait ");
        }

        if(repZPrefix) {
            res.append("repz ");
        } else if(repNZPrefix) {
            res.append("repnz ");
        }
        return res.toString();
    }
}
