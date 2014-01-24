package kianxali.scripting;

import org.jruby.RubyProc;

public interface ScriptAPI {
    void traverseCode(RubyProc block);
}
