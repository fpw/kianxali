package org.solhost.folko.dasm.xml;

import org.solhost.folko.dasm.instructions.x86.CPUMode;

// an opcode can have a different meaning depending on CPU mode, hence store it here
public class OpcodeOpts {
    public CPUMode mode;

    public boolean invalid, undefined;
    public boolean direction;
    public boolean sgnExt;
    public boolean opSize;
    public boolean modRM;
    public boolean lock;
    public byte tttn;
}
