package org.solhost.folko.dasm;

import org.solhost.folko.dasm.decoder.Instruction;

public interface DisassemblingListener {
    void onInstructionDecode(Instruction inst);
}
