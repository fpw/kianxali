package kianxali.disassembler;

import kianxali.decoder.DecodedEntity;

public interface DisassemblyListener {
    void onAnalyzeStart();
    void onAnalyzeEntity(DecodedEntity entity);
    void onAnalyzeError(long memAddr);
    void onAnalyzeStop();
}
