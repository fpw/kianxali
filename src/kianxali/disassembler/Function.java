package kianxali.disassembler;

public class Function {
    private final long startAddress;
    private long endAddress;
    private String name;

    public Function(long startAddr) {
        this.startAddress = startAddr;
        this.endAddress = startAddr;
        this.name = String.format("sub_%08X", startAddr);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
