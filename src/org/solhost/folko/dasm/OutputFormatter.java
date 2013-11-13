package org.solhost.folko.dasm;

public class OutputFormatter {
    private boolean includePrefixBytes;

    public OutputFormatter() {
    }

    public void setIncludePrefixBytes(boolean includePrefixBytes) {
        this.includePrefixBytes = includePrefixBytes;
    }

    public boolean shouldIncludePrefixBytes() {
        return includePrefixBytes;
    }

    public String formatRegister(String name) {
        return name.toLowerCase();
    }

    public String formatImmediate(long immediate) {
         if(immediate < 0) {
            return String.format("-%Xh", -immediate);
        } else if(immediate > 0) {
            return String.format("%Xh", immediate);
        } else {
            return "0";
        }
    }

    public String formatAddress(long offset) {
         if(offset < 0) {
            return String.format("%Xh", -offset);
        } else if(offset > 0) {
            return String.format("%Xh", offset);
        } else {
            return "0";
        }
    }
}
