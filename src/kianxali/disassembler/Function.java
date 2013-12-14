package kianxali.disassembler;

public class Function {
    private final long startAddress;
    private long endAddress;
    private String name;
    private final AddressNameListener nameListener;

    public Function(long startAddr, AddressNameListener nameListener) {
        this.startAddress = startAddr;
        this.nameListener = nameListener;
        this.endAddress = startAddr;
        this.name = String.format("sub_%X", startAddr);
    }

    public void setName(String name) {
        this.name = name;
        nameListener.onFunctionNameChange(this);
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
