package org.solhost.folko.dasm.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.solhost.folko.dasm.instructions.x86.CPUMode;

public class OpcodeHandler {
    public enum Mode {R, P, E, S};
    private final boolean isTwoByte;
    private final short opcode;
    private final Map<CPUMode, Map<Short, List<OpcodeOpts>>> extensions;
    private final Map<CPUMode, List<OpcodeOpts>> modeOpts;

    public OpcodeHandler(boolean isTwoByte, short opcode) {
        this.extensions = new HashMap<>();
        this.modeOpts = new HashMap<>();
        this.isTwoByte = isTwoByte;
        this.opcode = opcode;
    }

    public void addBaseOptions(CPUMode mode, OpcodeOpts newOpts) {
        List<OpcodeOpts> opts = modeOpts.get(mode);
        if(opts == null) {
            opts = new ArrayList<>(4);
            modeOpts.put(mode, opts);
        }
        opts.add(newOpts);
    }

    public void addExtensionOptions(CPUMode mode, short ext, OpcodeOpts newOpts) {
        Map<Short, List<OpcodeOpts>> exts = extensions.get(mode);
        if(exts == null) {
            exts = new HashMap<>();
            extensions.put(mode, exts);
        }
        List<OpcodeOpts> opts = exts.get(ext);
        if(opts == null) {
            opts = new ArrayList<>(4);
            exts.put(ext, opts);
        }
        opts.add(newOpts);
    }

    public boolean isTwoByte() {
        return isTwoByte;
    }

    public short getOpcode() {
        return opcode;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if(isTwoByte) {
            res.append(String.format("<opcode 0F%2X", opcode));
        } else {
            res.append(String.format("<opcode %2X", opcode));
        }

        for(CPUMode mode : modeOpts.keySet()) {
            res.append(" base=" +  modeOpts.get(mode).toString());
        }

        for(CPUMode mode : extensions.keySet()) {
            for(short ext : extensions.get(mode).keySet()) {
                res.append(" ext" + ext + "=" + extensions.get(mode).get(ext).toString());
                res.append("\n");
            }
        }
        res.append(">");

        return res.toString();
    }
}
