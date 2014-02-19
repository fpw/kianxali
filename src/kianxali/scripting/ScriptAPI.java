package kianxali.scripting;

import kianxali.decoder.DecodedEntity;
import org.jruby.RubyProc;

public interface ScriptAPI {
    void traverseCode(RubyProc block);
    boolean isCodeAddress(Long addr);
    DecodedEntity getEntityAt(Long addr);
    Short readByte(Long addr);
    void patchByte(Long addr, Short b);
    void reanalyze(Long addr);
}
