package kianxali.disassembler;

public interface DataListener {
    void onAnalyzeChange(long memAddr, DataEntry entry);
}
