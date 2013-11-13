package org.solhost.folko.dasm.cpu.x86;

import java.util.ArrayList;
import java.util.List;

import org.solhost.folko.dasm.cpu.x86.X86CPU.Segment;

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
        if(lockPrefix)      res.append("lock ");
        if(waitPrefix)      res.append("wait ");
        if(repZPrefix)      res.append("repz ");
        if(repNZPrefix)     res.append("repnz ");
        return res.toString();
    }
}
