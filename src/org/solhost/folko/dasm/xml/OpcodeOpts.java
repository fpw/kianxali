package org.solhost.folko.dasm.xml;

import java.util.ArrayList;
import java.util.List;
import org.solhost.folko.dasm.instructions.x86.CPUMode;

// an opcode can have a different meaning depending on CPU mode, hence store it here
public class OpcodeOpts {
    private final List<Syntax> syntaxes;

    public CPUMode mode;
    public boolean invalid, undefined;
    public boolean direction;
    public boolean sgnExt;
    public boolean opSize;
    public boolean modRM;
    public boolean lock;
    public byte tttn;

    public OpcodeOpts() {
        this.syntaxes = new ArrayList<>(4);
    }

    // without opcode extension
    public void addSyntax(Syntax syntax) {
        syntaxes.add(syntax);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("<mode=" + mode + " modRM=" + modRM);
        for(Syntax syntax : syntaxes) {
            res.append(" " + syntax.toString());
        }
        res.append(">");
        return res.toString();
    }
}
