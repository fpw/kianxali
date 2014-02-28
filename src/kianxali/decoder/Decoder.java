package kianxali.decoder;

import kianxali.loader.ByteSequence;

/**
 * A decoder reads bytes from a sequence until an instruction is fully
 * decoded with all its operands.
 * @author fwi
 *
 */
public interface Decoder {
    /**
     * Decode the next instruction from a byte sequence
     * @param ctx the current context
     * @param seq the byte sequence to read the instruction from
     * @return
     */
    Instruction decodeOpcode(Context ctx, ByteSequence seq);
}
