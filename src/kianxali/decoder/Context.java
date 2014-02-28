package kianxali.decoder;

/**
 * A context stores information that is required for and modified when parsing
 * opcodes. It is mostly architecture dependent.
 * @author fwi
 *
 */
public interface Context {
    /**
     * Create an instruction decoder for the this context.
     * @return an instruction decoder matching the configuration of the context
     */
    Decoder createInstructionDecoder();

    /**
     * Set the current address of execution.
     * @param pointer the memory address of the current location
     */
    void setInstructionPointer(long pointer);

    /**
     * Returns the memory address of the current location
     * @return the memory address of the current location
     */
    long getInstructionPointer();

    /**
     * Returns the default size of memory addresses in bytes
     * @return the default size of memory addresses in bytes
     */
    int getDefaultAddressSize();
}
