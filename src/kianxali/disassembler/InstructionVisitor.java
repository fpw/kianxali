package kianxali.disassembler;

import kianxali.decoder.Instruction;

public interface InstructionVisitor {
    void onVisit(Instruction inst);
}
