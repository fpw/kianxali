package kianxali.disassembler;

public class Function {
    private final long startAddress, endAddress;

    public Function(long startAddr, long endAddr) {
        this.startAddress = startAddr;
        this.endAddress = endAddr;
    }

    public long getStartAddress() {
        return startAddress;
    }

    public long getEndAddress() {
        return endAddress;
    }
}
