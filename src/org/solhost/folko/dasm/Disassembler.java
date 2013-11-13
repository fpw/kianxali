package org.solhost.folko.dasm;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.solhost.folko.dasm.decoder.Context;
import org.solhost.folko.dasm.decoder.Instruction;
import org.solhost.folko.dasm.decoder.InstructionDecoder;

public class Disassembler {
    private final ImageFile image;
    private final Set<DisassemblingListener> listeners;

    public Disassembler(ImageFile image) {
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
        InstructionDecoder decoder = ctx.createInstructionDecoder();
        ctx.setInstructionPointer(image.getCodeEntryPointMem());
        boolean stop = false;
        while(!stop) {
            long memAddr = ctx.getInstructionPointer();
            Instruction inst = decoder.decodeOpcode(ctx, image.getByteSequence(memAddr));
            if(inst != null) {
                for(DisassemblingListener listener : listeners) {
                    listener.onInstructionDecode(inst);
                }
            }

            if(inst == null || inst.stopsTrace()) {
                stop = true;
            } else {
                ctx.setInstructionPointer(memAddr + inst.getSize());
            }
        }
    }
}
