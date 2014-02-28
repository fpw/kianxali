package kianxali.disassembler;

import kianxali.decoder.Instruction;

/**
 * Implementation of this interface can be passed to {@link DisassemblyData#visitInstructions(InstructionVisitor)}
 * to allow a traversal through all instructions of the image.
 * @author fwi
 *
 */
public interface InstructionVisitor {
    /**
     * Will be called for each instruction discovered in the image.
     * They will be called in order of the addresses
     * @param inst a discovered instruction
     */
    void onVisit(Instruction inst);
}
