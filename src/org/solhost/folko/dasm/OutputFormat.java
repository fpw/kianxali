package org.solhost.folko.dasm;

public class OutputFormat {
    private final AliasResolver aliases;
    private boolean includePrefixBytes;

    public OutputFormat(AliasResolver resolver) {
        this.aliases = resolver;
    }

    public void setIncludePrefixBytes(boolean includePrefixBytes) {
        this.includePrefixBytes = includePrefixBytes;
    }

    public boolean isIncludePrefixBytes() {
        return includePrefixBytes;
    }

    public String formatImmediate(long immediate) {
        String alias = aliases.getAddressAlias(immediate);
        if(alias != null) {
            return alias;
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
        String alias = aliases.getAddressAlias(offset);
        if(alias != null) {
            return alias;
        }

        if(offset < 0) {
            return String.format("%Xh", -offset);
        } else if(offset > 0){
            return String.format("%Xh", offset);
        } else {
            return "0";
        }
    }
}
