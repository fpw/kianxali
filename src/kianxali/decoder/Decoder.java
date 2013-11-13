package kianxali.decoder;

import kianxali.image.ByteSequence;

public interface Decoder {
    Instruction decodeOpcode(Context ctx, ByteSequence seq);
}
