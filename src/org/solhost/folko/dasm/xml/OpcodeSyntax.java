package org.solhost.folko.dasm.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.solhost.folko.dasm.xml.OpcodeOperand.AddressType;

public class OpcodeSyntax {
    private final OpcodeEntry entry; // syntax belongs to this entry
    private final List<OpcodeOperand> operands;
    private Short extension;
    private String mnemonic;

    {
        this.operands = new ArrayList<>(4);
    }

    public OpcodeSyntax(OpcodeEntry entry) {
        this.entry = entry;
    }

    public OpcodeSyntax(OpcodeEntry entry, short extension) {
        this.entry = entry;
        this.extension = extension;
    }

    public OpcodeEntry getOpcodeEntry() {
        return entry;
    }

    public boolean isExtended() {
        return extension != null;
    }

    public Short getExtension() {
        return extension;
    }

    void addOperand(OpcodeOperand opDesc) {
        operands.add(opDesc);
    }

    void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public boolean hasEncodedRegister() {
        for(OpcodeOperand o : operands) {
            if(o.adrType == AddressType.LEAST_REG) {
                return true;
            }
        }
        return false;
    }

    public List<OpcodeOperand> getOperands() {
        return Collections.unmodifiableList(operands);
    }

    public int getEncodedRegisterPrefixIndex() {
        int pos = 0;
        if(entry.prefix != null) {
            pos++;
        }
        if(entry.twoByte) {
            pos++;
        }
        return pos;
    }

    public short[] getPrefix() {
        short res[] = new short[4];
        int i = 0;
        if(entry.prefix != null) {
            res[i++] = entry.prefix;
        }
        if(entry.twoByte) {
            res[i++] = 0x0F;
        }
        res[i++] = entry.opcode;
        if(entry.secondOpcode != null) {
            res[i++] = entry.secondOpcode;
        }
        return Arrays.copyOf(res, i);
    }

    public String getPrefixAsHexString() {
        short prefix[] = getPrefix();
        StringBuilder res = new StringBuilder();
        for(short b : prefix) {
            res.append(String.format("%02X", b));
        }
        return res.toString();
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(String.format("<syntax opcode=%s mnemonic=%s", getPrefixAsHexString(), mnemonic));
        for(int i = 0; i < operands.size(); i++) {
            res.append(String.format(" op%d=%s", i + 1, operands.get(i).toString()));
        }
        res.append(">");
        return res.toString();
    }
}
