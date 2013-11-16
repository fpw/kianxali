package kianxali.disassembler;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import kianxali.decoder.DecodedEntity;

public class DisassemblyData {
    private final NavigableMap<Long, DecodedEntity> memoryMap;

    public DisassemblyData() {
        this.memoryMap = new TreeMap<>();
    }

    public synchronized void insertEntity(DecodedEntity entity) {
        long memAddr = entity.getMemAddress();
        DecodedEntity old = findEntity(memAddr);
        if(old != null) {
            memoryMap.remove(old.getMemAddress());
        }
        memoryMap.put(memAddr, entity);
    }

    public synchronized void clearAddress(long memAddr) {
        DecodedEntity old = findEntity(memAddr);
        if(old != null) {
            memoryMap.remove(old.getMemAddress());
        }
    }

    public synchronized DecodedEntity getEntityOnExactAddress(long memAddr) {
        return memoryMap.get(memAddr);
    }

    public synchronized DecodedEntity findEntity(long memAddr) {
        // check if the last instruction at lower addresses overlaps
        Entry<Long, DecodedEntity> floorEntry = memoryMap.floorEntry(memAddr);
        if(floorEntry == null) {
            return null;
        }
        long lastAddress = floorEntry.getKey();
        DecodedEntity res = floorEntry.getValue();
        if(memAddr < lastAddress || memAddr >= lastAddress + res.getSize()) {
            return null;
        }
        return res;
    }

    public Set<Entry<Long, DecodedEntity>> getEntities() {
        return Collections.unmodifiableSet(memoryMap.entrySet());
    }

    public int getEntityCount() {
        return memoryMap.size();
    }
}
