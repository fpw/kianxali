package kianxali.disassembler;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;

import kianxali.decoder.DecodedEntity;
import kianxali.image.ImageFile;
import kianxali.image.Section;

public class DisassemblyData {
    private final Set<DataListener> listeners;
    private final NavigableMap<Long, DataEntry> memoryMap;

    public DisassemblyData() {
        this.listeners = new CopyOnWriteArraySet<>();
        this.memoryMap = new TreeMap<>();
    }

    public void addListener(DataListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DataListener listener) {
        listeners.remove(listener);
    }

    void tellListeners(long memAddr) {
        for(DataListener listener : listeners) {
            listener.onAnalyzeChange(memAddr);
        }
    }

    private void put(long memAddr, DataEntry entry) {
        memoryMap.put(memAddr, entry);
        tellListeners(memAddr);
    }

    public synchronized void insertImageFileWithSections(ImageFile file) {
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

    public synchronized void insertEntity(DecodedEntity entity) {
        long memAddr = entity.getMemAddress();
        DataEntry old = getInfoOnExactAddress(memAddr);
        if(old != null) {
            // already got info for this address, add entity
            old.setEntity(entity);
            tellListeners(memAddr);
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
            }
        }
    }

    public void insertFunction(Function function) {
        long start = function.getStartAddress();
        long end = function.getEndAddress();

        DataEntry entry = getInfoOnExactAddress(start);
        if(entry == null) {
            entry = new DataEntry(start);
            put(start, entry);
        }
        entry.setStartFunction(function);
        tellListeners(start);

        entry = getInfoOnExactAddress(end);
        if(entry != null) {
            entry.setEndFunction(function);
        }
        // TODO: add an else case
        tellListeners(end);
    }

    public void insertReference(DataEntry srcEntry, long dstAddress) {
        DataEntry entry = getInfoOnExactAddress(dstAddress);
        if(entry == null) {
            entry = new DataEntry(dstAddress);
            put(dstAddress, entry);
        }
        entry.addReferenceFrom(srcEntry);
        tellListeners(dstAddress);
    }

    public synchronized DataEntry getInfoOnExactAddress(long memAddr) {
        DataEntry entry = memoryMap.get(memAddr);
        return entry;
    }

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

    public synchronized DecodedEntity getEntityOnExactAddress(long memAddr) {
        DataEntry entry = getInfoOnExactAddress(memAddr);
        if(entry == null) {
            return null;
        }
        return entry.getEntity();
    }

    public synchronized DecodedEntity findEntityOnAddress(long memAddr) {
        DataEntry entry = getInfoCoveringAddress(memAddr);
        if(entry == null) {
            return null;
        }
        return entry.getEntity();
    }

    public synchronized int getEntityCount() {
        return memoryMap.size();
    }
}
