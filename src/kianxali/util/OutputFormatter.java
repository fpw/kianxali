package kianxali.util;

import kianxali.disassembler.AddressNameResolver;

public class OutputFormatter {
    private boolean includeRawBytes;
    private AddressNameResolver addrResolver;

    public OutputFormatter() {
    }

    public void setAddressNameResolve(AddressNameResolver resolver) {
        this.addrResolver = resolver;
    }

    public static String formatByteString(short[] bytes) {
        StringBuilder res = new StringBuilder();

        for(short b : bytes) {
            res.append(String.format("%02X", b));
        }

        return res.toString();
    }

    public void setIncludeRawBytes(boolean includeRawBytes) {
        this.includeRawBytes = includeRawBytes;
    }

    public boolean shouldIncludeRawBytes() {
        return includeRawBytes;
    }

    public String formatRegister(String name) {
        return name.toLowerCase();
    }

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

    public String formatImmediate(long immediate) {
        if(addrResolver != null && addrResolver.resolveAddress(immediate) != null) {
            return addrResolver.resolveAddress(immediate);
        }
        return formatNumber(immediate, true);
    }

    public String formatAddress(long offset) {
        if(addrResolver != null && addrResolver.resolveAddress(offset) != null) {
            return addrResolver.resolveAddress(offset);
        }
        return formatNumber(offset, false);
    }

    public String formatMnemonic(String string) {
        return string.toLowerCase();
    }
}
