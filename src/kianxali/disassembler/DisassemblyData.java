package kianxali.disassembler;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;

import kianxali.decoder.DecodedEntity;
import kianxali.decoder.Instruction;
import kianxali.loader.ImageFile;
import kianxali.loader.Section;

/**
 * This data structure stores the result of the disassembly. It creates a memory map
 * for the image file to reconstruct the actual runtime layout. It is passed to the
 * disassembler that will fill it.
 * @author fwi
 *
 */
public class DisassemblyData {
    private final Set<DataListener> listeners;
    private final NavigableMap<Long, DataEntry> memoryMap;

    /**
     * Construct a new disassembly data object.
     */
    public DisassemblyData() {
        this.listeners = new CopyOnWriteArraySet<>();
        this.memoryMap = new TreeMap<>();
    }

    /**
     * Adds a listener that will be informed about changes of
     * the memory map, i.e. when a new instruction or data was found
     * by the disassembler
     * @param listener the listener to add
     */
    public void addListener(DataListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener
     * @param listener the listener to remove
     */
    public void removeListener(DataListener listener) {
        listeners.remove(listener);
    }

    void tellListeners(long memAddr) {
        DataEntry entry = getInfoOnExactAddress(memAddr);
        for(DataListener listener : listeners) {
            listener.onAnalyzeChange(memAddr, entry);
        }
    }

    private void put(long memAddr, DataEntry entry) {
        memoryMap.put(memAddr, entry);
        tellListeners(memAddr);
    }

    void clear(long addr) {
        memoryMap.remove(addr);
        tellListeners(addr);
    }

    // clears instruction or data and attached data, but not function start, image start etc.
    void clearDecodedEntity(long memAddr) {
        DataEntry entry = getInfoCoveringAddress(memAddr);
        if(entry == null) {
            // nothing to do as there is no code or data
            return;
        }
        entry.setEntity(null);
        entry.clearAttachedData();
        tellListeners(memAddr);

        // clear from-references to here
        for(long refAddr : memoryMap.keySet()) {
            DataEntry refEntry = memoryMap.get(refAddr);
            if(refEntry.removeReference(entry)) {
                tellListeners(refAddr);
            }
        }
    }

    synchronized void insertImageFileWithSections(ImageFile file) {
        long imageAddress = 0L;
        if(file.getSections().size() > 0) {
            imageAddress = file.getSections().get(0).getStartAddress();
        }
        DataEntry old = getInfoOnExactAddress(imageAddress);
        if(old != null) {
            old.setStartImageFile(file);
            tellListeners(imageAddress);
        } else {
            DataEntry entry = new DataEntry(imageAddress);
            entry.setStartImageFile(file);
            put(imageAddress, entry);
        }

        for(Section section : file.getSections()) {
            long memAddrStart = section.getStartAddress();
            long memAddrEnd = section.getEndAddress();
            old = getInfoOnExactAddress(memAddrStart);
            if(old != null) {
                old.setStartSection(section);
                tellListeners(memAddrStart);
            } else {
                DataEntry entry = new DataEntry(memAddrStart);
                entry.setStartSection(section);
                put(memAddrStart, entry);
            }

            old = getInfoOnExactAddress(memAddrEnd);
            if(old != null) {
                old.setEndSection(section);
                tellListeners(memAddrEnd);
            } else {
                DataEntry entry = new DataEntry(memAddrEnd);
                entry.setEndSection(section);
                put(memAddrEnd, entry);
            }
        }
    }

    synchronized DataEntry insertEntity(DecodedEntity entity) {
        long memAddr = entity.getMemAddress();
        DataEntry old = getInfoOnExactAddress(memAddr);
        if(old != null) {
            // already got info for this address, add entity
            old.setEntity(entity);
            tellListeners(memAddr);
            return old;
        } else {
            // check if another entry covers this address, i.e. there is data or an opcode that starts before
            DecodedEntity covering = findEntityOnAddress(memAddr);
            if(covering != null) {
                throw new IllegalArgumentException("address covered by other entity");
            } else {
                // new entity entry as nothing covered the address
                DataEntry entry = new DataEntry(memAddr);
                entry.setEntity(entity);
                put(memAddr, entry);
                return entry;
            }
        }
    }

    void insertFunction(Function function) {
        long start = function.getStartAddress();
        long end = function.getEndAddress();

        DataEntry startEntry = getInfoOnExactAddress(start);
        DataEntry endEntry = getInfoOnExactAddress(end);
        if(startEntry != null && startEntry.getStartFunction() == function && endEntry != null && endEntry.getEndFunction() == function) {
            // nothing changed
            return;
        }

        if(startEntry == null) {
            startEntry = new DataEntry(start);
            put(start, startEntry);
        }
        startEntry.setStartFunction(function);
        tellListeners(start);

        if(endEntry != null) {
            endEntry.setEndFunction(function);
        }
        // TODO: add an else case
        tellListeners(end);
    }

    void updateFunctionEnd(Function function, long newEnd) {
        long oldEnd = function.getEndAddress();

        function.setEndAddress(newEnd);

        DataEntry oldEntry = getInfoOnExactAddress(oldEnd);
        if(oldEntry != null) {
            oldEntry.setEndFunction(null);
            tellListeners(oldEnd);
        }

        DataEntry newEntry = getInfoOnExactAddress(newEnd);
        if(newEntry == null) {
            newEntry = new DataEntry(newEnd);
            put(newEnd, newEntry);
        }
        newEntry.setEndFunction(function);
        tellListeners(newEnd);
    }


    void insertReference(DataEntry srcEntry, long dstAddress) {
        DataEntry entry = getInfoOnExactAddress(dstAddress);
        if(entry == null) {
            entry = new DataEntry(dstAddress);
            put(dstAddress, entry);
        }
        entry.addReferenceFrom(srcEntry);
        tellListeners(dstAddress);
    }

    /**
     * Attaches a user comment to a given memory address
     * @param memAddr the memory address to attach the comment to
     * @param comment the user comment
     */
    public void insertComment(long memAddr, String comment) {
        DataEntry entry = getInfoOnExactAddress(memAddr);
        if(entry == null) {
            entry = new DataEntry(memAddr);
            put(memAddr, entry);
        }
        entry.setComment(comment);
        tellListeners(memAddr);
    }

    /**
     * Retrieves the data entry for a given memory address.
     * The address must be the exact starting address of the entry,
     * i.e. if an address is passed that covers the middle of an entry,
     * it will not be returned. {@link DisassemblyData#getInfoCoveringAddress(long)}
     * should be used for those cases.
     * @param memAddr the address to retrieve
     * @return the data entry started at the given address or null
     */
    public synchronized DataEntry getInfoOnExactAddress(long memAddr) {
        DataEntry entry = memoryMap.get(memAddr);
        return entry;
    }

    /**
     * Retrieves the data entry for a given memory address.
     * The address needn't be the exact starting address of the entry,
     * i.e. if an address is passed that covers the middle of an entry,
     * it will still be returned.
     * @param memAddr the address to retrieve
     * @return the data entry that covers the given address or null
     */
    public synchronized DataEntry getInfoCoveringAddress(long memAddr) {
        // check if the last instruction at lower addresses overlaps
        Entry<Long, DataEntry> floorEntry = memoryMap.floorEntry(memAddr);
        if(floorEntry == null) {
            return null;
        }
        long lastAddress = floorEntry.getKey();
        DataEntry res = floorEntry.getValue();
        DecodedEntity entity = res.getEntity();
        if(entity == null) {
            return res;
        }
        if(memAddr < lastAddress || memAddr >= lastAddress + entity.getSize()) {
            return null;
        }
        return res;
    }

    /**
     * Returns the entity (instruction or data) associated with a given address.
     * It will only be returned if the exact starting address is passed.
     * @param memAddr the address to retrieve
     * @return the entity starting at the exact given address or null
     */
    public synchronized DecodedEntity getEntityOnExactAddress(long memAddr) {
        DataEntry entry = getInfoOnExactAddress(memAddr);
        if(entry == null) {
            return null;
        }
        return entry.getEntity();
    }

    synchronized DecodedEntity findEntityOnAddress(long memAddr) {
        DataEntry entry = getInfoCoveringAddress(memAddr);
        if(entry == null) {
            return null;
        }
        return entry.getEntity();
    }

    /**
     * Returns the total number of entries in the memory map
     * @return the number of entries contained in the memory map
     */
    public synchronized int getEntryCount() {
        return memoryMap.size();
    }

    /**
     * Allows a visitor to visit all entries in the memory map.
     * @param visitor a visitor that will be called with each entry
     */
    public void visitInstructions(InstructionVisitor visitor) {
        for(long addr : memoryMap.keySet()) {
            DataEntry entry = memoryMap.get(addr);
            DecodedEntity entity = entry.getEntity();
            if(entity instanceof Instruction) {
                visitor.onVisit((Instruction) entity);
            }
        }
    }
}
