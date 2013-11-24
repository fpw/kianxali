package kianxali.disassembler;

public interface DisassemblyListener {
    void onAnalyzeStart();
    void onAnalyzeError(long memAddr);
    void onAnalyzeStop();
}
