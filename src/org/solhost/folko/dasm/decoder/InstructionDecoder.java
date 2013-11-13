package org.solhost.folko.dasm.decoder;

import org.solhost.folko.dasm.ByteSequence;

public interface InstructionDecoder {
    Instruction decodeOpcode(Context ctx, ByteSequence seq);
}
