package org.solhost.folko.dasm;

import org.solhost.folko.dasm.decoder.Instruction;

public interface DisassemblingListener {
    public void onInstructionDecode(Instruction inst);
}
