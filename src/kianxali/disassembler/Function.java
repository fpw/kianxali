package kianxali.disassembler;

/**
 * This class represents a function discovered by the disassembler
 * @author fwi
 *
 */
public class Function {
    private final long startAddress;
    private long endAddress;
    private String name;
    private final AddressNameListener nameListener;

    Function(long startAddr, AddressNameListener nameListener) {
        this.startAddress = startAddr;
        this.nameListener = nameListener;
        this.endAddress = startAddr;
        this.name = String.format("sub_%X", startAddr);
    }

    /**
     * Changes the name of the function
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
        nameListener.onFunctionNameChange(this);
    }

    /**
     * Returns the name of the function
     * @return name of the function
     */
    public String getName() {
        return name;
    }

    void setEndAddress(long endAddress) {
        this.endAddress = endAddress;
    }

    /**
     * Returns the starting address of the function
     * @return the start address of the function
     */
    public long getStartAddress() {
        return startAddress;
    }

    /**
     * Returns the last address of this function
     * @return the last address of the function
     */
    public long getEndAddress() {
        return endAddress;
    }

    @Override
    public String toString() {
        return getName();
    }
}
