package kianxali.disassembler;

/**
 * Implementations of this interface can register at the disassembler
 * to be notified when the disassembly starts, ends or runs into an
 * error
 * @author fwi
 *
 */
public interface DisassemblyListener {
    /**
     * Will be called when the analysis starts
     */
    void onAnalyzeStart();

    /**
     * Will be called when the analysis runs into an error
     * @param memAddr the erroneous memory address
     * @param reason reason for the error
     */
    void onAnalyzeError(long memAddr, String reason);

    /**
     * Will be called when the analysis stops
     */
    void onAnalyzeStop();
}
