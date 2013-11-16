package kianxali.image;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import kianxali.decoder.DecodableEntity;

public class LoadedImage {
    private final SortedMap<Long, DecodableEntity> memoryMap;

    public LoadedImage() {
        this.memoryMap = new TreeMap<>();
    }

    public synchronized void insert(DecodableEntity entity) {
        long memAddr = entity.getMemAddress();
        DecodableEntity old = find(memAddr);
        if(old != null) {
            memoryMap.remove(old.getMemAddress());
        }
        memoryMap.put(memAddr, entity);
    }

    public synchronized DecodableEntity getEntityOnExactAddress(long memAddr) {
        return memoryMap.get(memAddr);
    }

    public synchronized DecodableEntity find(long memAddr) {
        // first try direct lookup
        DecodableEntity res = memoryMap.get(memAddr);
        if(res != null) {
            return res;
        }

        // now check if the last instruction at lower addresses overlaps
        SortedMap<Long, DecodableEntity> head = memoryMap.headMap(memAddr);
        if(head.size() == 0) {
            return null;
        }
        long lastAddress = head.lastKey();
        res = memoryMap.get(lastAddress);
        if(memAddr < lastAddress || memAddr >= lastAddress + res.getSize()) {
            return null;
        }
        return res;
    }

    public synchronized Set<Entry<Long, DecodableEntity>> getAllEntities() {
        return Collections.unmodifiableSet(memoryMap.entrySet());
    }
}
