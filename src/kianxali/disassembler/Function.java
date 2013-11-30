package kianxali.disassembler;

public class Function {
    private final long startAddress;
    private long endAddress;

    public Function(long startAddr) {
        this.startAddress = startAddr;
        this.endAddress = startAddr;
    }

    public void setEndAddress(long endAddress) {
        this.endAddress = endAddress;
    }

    public long getStartAddress() {
        return startAddress;
    }

    public long getEndAddress() {
        return endAddress;
    }
}
