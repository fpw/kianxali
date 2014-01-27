package kianxali.scripting;

import kianxali.decoder.DecodedEntity;
import org.jruby.RubyProc;

public interface ScriptAPI {
    void traverseCode(RubyProc block);
    DecodedEntity getEntityAt(long addr);
}
