package kianxali.disassembler;

/**
 * Implementations of this interface can register at a {@link Function} to be
 * notified when the name changes.
 * @author fwi
 *
 */
public interface AddressNameListener {
    /**
     * Will be called when the name of the function changes.
     * @param fun the function whose name changed
     */
    void onFunctionNameChange(Function fun);
}
