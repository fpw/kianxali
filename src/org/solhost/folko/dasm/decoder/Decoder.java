package org.solhost.folko.dasm.decoder;

import org.solhost.folko.dasm.images.ByteSequence;

public interface Decoder {
    Instruction decodeOpcode(Context ctx, ByteSequence seq);
}
