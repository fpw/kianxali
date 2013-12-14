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

    public String formatImmediate(long immediate) {
        if(addrResolver != null && addrResolver.resolveAddress(immediate) != null) {
            return addrResolver.resolveAddress(immediate);
        }

        if(immediate < 0) {
            return String.format("-%Xh", -immediate);
        } else if(immediate > 0) {
            return String.format("%Xh", immediate);
        } else {
            return "0";
        }
    }

    public String formatAddress(long offset) {
        if(addrResolver != null && addrResolver.resolveAddress(offset) != null) {
            return addrResolver.resolveAddress(offset);
        }

        if(offset < 0) {
            return String.format("%Xh", -offset);
        } else if(offset > 0) {
            return String.format("%Xh", offset);
        } else {
            return "0";
        }
    }

    public String formatMnemonic(String string) {
        return string.toLowerCase();
    }
}
