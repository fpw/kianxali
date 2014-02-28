package kianxali.util;


/**
 * This class stores information about how the output of instructions
 * and data should be formatted, e.g. the format of hex strings, addresses,
 * mnemonics and so on.
 * @author fwi
 *
 */
public class OutputFormatter {
    private boolean includeRawBytes;
    private AddressNameResolver addrResolver;

    /**
     * Construct a new formatter with default settings.
     */
    public OutputFormatter() {
    }

    /**
     * Sets a resolver that provides symbol names for memory addresses.
     * It will be used to display symbol names instead of addresses if present.
     * @param resolver the resolver to ask about memory addresses
     */
    public void setAddressNameResolve(AddressNameResolver resolver) {
        this.addrResolver = resolver;
    }

    /**
     * Formats a byte string as a hex string
     * @param bytes the byte array
     * @return a hex string describing the byte array
     */
    public static String formatByteString(short[] bytes) {
        StringBuilder res = new StringBuilder();

        for(short b : bytes) {
            res.append(String.format("%02X", b));
        }

        return res.toString();
    }

    /**
     * Configure whether the raw instruction bytes should be included when displaying
     * an instruction
     * @param includeRawBytes true if the instruction bytes should be included
     */
    public void setIncludeRawBytes(boolean includeRawBytes) {
        this.includeRawBytes = includeRawBytes;
    }

    /**
     * Returns whether raw instruction bytes should be included when displaying
     * an instruction
     * @return true if the instruction bytes should be included
     */
    public boolean shouldIncludeRawBytes() {
        return includeRawBytes;
    }

    /**
     * Formats a register name
     * @param name the name of the register
     * @return the formatted name of the register
     */
    public String formatRegister(String name) {
        return name.toLowerCase();
    }

    /**
     * Formats a given number.
     * @param num the number to format
     * @param includeMinus whether the sign should be output
     * @return a string containing the formatted number
     */
    public String formatNumber(long num, boolean includeMinus) {
        StringBuilder res = new StringBuilder();
        if(num < 0) {
            res.append(Long.toHexString(-num).toUpperCase());
        } else {
            res.append(Long.toHexString(num).toUpperCase());
        }

        // Denote that the string is hexadecimal if needed
        if(Math.abs(num) > 9) {
            res.append("h");
        }

        // add a leading zero if the representation starts with a letter
        if(Character.isAlphabetic(res.charAt(0))) {
            res.insert(0, "0");
        }

        // add minus if number is negative and user wants to include the sign
        if(num < 0 && includeMinus) {
            res.insert(0, "-");
        }

        return res.toString();
    }

    /**
     * Formats a given immediate
     * @param immediate the immediate to format
     * @return a string describing the immediate
     */
    public String formatImmediate(long immediate) {
        if(addrResolver != null && addrResolver.resolveAddress(immediate) != null) {
            return addrResolver.resolveAddress(immediate);
        }
        return formatNumber(immediate, true);
    }

    /**
     * Formats a given virtual memory address
     * @param offset the address to format
     * @return the formatted addresses
     */
    public String formatAddress(long offset) {
        if(addrResolver != null && addrResolver.resolveAddress(offset) != null) {
            return addrResolver.resolveAddress(offset);
        }
        return formatNumber(offset, false);
    }

    /**
     * Formats a given mnemonic
     * @param string the mnemonic to format
     * @return the formatted mnemonic
     */
    public String formatMnemonic(String string) {
        return string.toLowerCase();
    }
}
