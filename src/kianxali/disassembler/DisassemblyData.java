package kianxali.disassembler;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import kianxali.decoder.DecodedEntity;

public class DisassemblyData {
    private final NavigableMap<Long, DataEntry> memoryMap;

    private class DataEntry {
        DecodedEntity entity;

        public DataEntry(DecodedEntity entity) {
            this.entity = entity;
        }
    }

    public DisassemblyData() {
        this.memoryMap = new TreeMap<>();
    }

    public synchronized void insertEntity(DecodedEntity entity) {
        long memAddr = entity.getMemAddress();
        DecodedEntity old = findEntityOnAddress(memAddr);
        if(old != null) {
            memoryMap.remove(old.getMemAddress());
        }
        memoryMap.put(memAddr, new DataEntry(entity));
    }

    public synchronized void clearEntity(long memAddr) {
        DecodedEntity old = findEntityOnAddress(memAddr);
        if(old != null) {
            memoryMap.remove(old.getMemAddress());
        }
    }

    public synchronized DecodedEntity getEntityOnExactAddress(long memAddr) {
        DataEntry entry = memoryMap.get(memAddr);
        if(entry == null) {
            return null;
        }
        return entry.entity;
    }

    public synchronized DecodedEntity findEntityOnAddress(long memAddr) {
        // check if the last instruction at lower addresses overlaps
        Entry<Long, DataEntry> floorEntry = memoryMap.floorEntry(memAddr);
        if(floorEntry == null) {
            return null;
        }
        long lastAddress = floorEntry.getKey();
        DecodedEntity res = floorEntry.getValue().entity;
        if(memAddr < lastAddress || memAddr >= lastAddress + res.getSize()) {
            return null;
        }
        return res;
    }

    public int getEntityCount() {
        return memoryMap.size();
    }
}
