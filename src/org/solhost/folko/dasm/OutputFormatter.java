package org.solhost.folko.dasm;

public class OutputFormatter {
    private boolean includePrefixBytes;

    public OutputFormatter() {
    }

    public void setIncludePrefixBytes(boolean includePrefixBytes) {
        this.includePrefixBytes = includePrefixBytes;
    }

    public boolean isIncludePrefixBytes() {
        return includePrefixBytes;
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
        } else if(offset > 0){
            return String.format("%Xh", offset);
        } else {
            return "0";
        }
    }
}
