package kianxali.util;

/**
 * This interface is implemented by classes that can resolve memory addresses
 * to symbol names.
 * @author fwi
 *
 */
public interface AddressNameResolver {
    /**
     * Resolve the symbol name of a memory address.
     * @param memAddr the memory address to resolve
     * @return the name of the symbol associated with the memory address or null if no symbol
     */
    String resolveAddress(long memAddr);
}
