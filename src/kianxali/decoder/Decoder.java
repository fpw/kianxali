package kianxali.decoder;

import kianxali.loader.ByteSequence;

public interface Decoder {
    Instruction decodeOpcode(Context ctx, ByteSequence seq);
}
