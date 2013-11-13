package org.solhost.folko.dasm;

import java.util.Collections;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.solhost.folko.dasm.decoder.Context;
import org.solhost.folko.dasm.decoder.DecodedEntity;
import org.solhost.folko.dasm.decoder.Instruction;
import org.solhost.folko.dasm.decoder.Decoder;
import org.solhost.folko.dasm.images.ImageFile;

public class Disassembler {
    private final ImageFile image;
    private final Set<DisassemblingListener> listeners;
    private final Map<Long, DecodedEntity> decodedLocations;
    private final Queue<Long> pendingInstructionAddresses;

    public Disassembler(ImageFile image) {
        this.decodedLocations = new TreeMap<>();
        this.pendingInstructionAddresses = new PriorityQueue<>();
        this.listeners = new CopyOnWriteArraySet<>();
        this.image = image;
    }

    public void addDisassemblingListener(DisassemblingListener listener) {
        listeners.add(listener);
    }

    public void removeDisassemblingListener(DisassemblingListener listener) {
        listeners.remove(listener);
    }

    public void disassemble() throws Exception {
        Context ctx = image.createContext();
        Decoder decoder = ctx.createInstructionDecoder();
        pendingInstructionAddresses.add(image.getCodeEntryPointMem());
        while(pendingInstructionAddresses.size() > 0) {
            long memAddr = pendingInstructionAddresses.poll();
            disassembleTrace(memAddr, ctx, decoder);
        }
    }

    public DecodedEntity getEntity(long memAddr) {
        return decodedLocations.get(memAddr);
    }

    public Map<Long, DecodedEntity> getEntities() {
        return Collections.unmodifiableMap(decodedLocations);
    }

    private void disassembleTrace(long memAddr, Context ctx, Decoder decoder) {
        while(true) {
            if(decodedLocations.containsKey(memAddr) || !isValidAddress(memAddr)) {
                break;
            }

            ctx.setInstructionPointer(memAddr);
            Instruction inst = null;
            try {
                inst = decoder.decodeOpcode(ctx, image.getByteSequence(memAddr));
            } catch(Exception e) {
                System.err.println(String.format("Disassemble error at %08X: %s", memAddr, inst));
                throw e;
            }
            if(inst != null) {
                decodedLocations.put(memAddr, inst);
                checkNewTraces(inst);
                memAddr += inst.getSize();
            }

            for(DisassemblingListener listener : listeners) {
                try {
                    listener.onEntityChange(memAddr);
                } catch(Exception e) {
                    System.err.println("Error processing: " + inst.toString());
                    throw e;
                }
            }

            if(inst == null || inst.stopsTrace()) {
                break;
            }
        }
    }

    // checks whether the instruction's operands could start a new trace
    private void checkNewTraces(Instruction inst) {
        Long addr = inst.getBranchAddress();
        if(addr != null && !decodedLocations.containsKey(addr)) {
            pendingInstructionAddresses.add(addr);
        }
    }

    private boolean isValidAddress(long memAddr) {
        return image.getSectionForMemAddress(memAddr) != null;
    }
}
