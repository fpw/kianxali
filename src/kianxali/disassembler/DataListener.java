package kianxali.disassembler;

/**
 * This is the main listener interface for the disassembler.
 * It can be used to register at the {@link DisassemblyData} instance
 * to get notified when the information of an address changes.
 * @author fwi
 *
 */
public interface DataListener {
    /**
     * Will be called when the information for a memory address changes
     * @param memAddr the memory address that was just analyzed
     * @param entry the new entry for this memory address or null if it was cleared
     */
    void onAnalyzeChange(long memAddr, DataEntry entry);
}
