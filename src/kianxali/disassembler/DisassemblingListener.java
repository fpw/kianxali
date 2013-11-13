package kianxali.disassembler;

public interface DisassemblingListener {
    void onDisassemblyStart();
    void onDisassembledAddress(long memAddr);
    void onDisassemblyFinish();
}
