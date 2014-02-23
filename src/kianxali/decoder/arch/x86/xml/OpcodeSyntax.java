package kianxali.decoder.arch.x86.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kianxali.decoder.arch.x86.X86Mnemonic;
import kianxali.decoder.arch.x86.xml.OperandDesc.AddressType;

public class OpcodeSyntax {
    private final OpcodeEntry entry; // syntax belongs to this entry
    private final List<OperandDesc> operands;
    private Short extension;
    private boolean modRMMustMem, modRMMustReg;
    private X86Mnemonic mnemonic;

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

    public void setModRMMustMem(boolean must) {
        this.modRMMustMem = must;
    }

    public void setModRMMustReg(boolean must) {
        this.modRMMustReg = must;
    }

    public boolean isModRMMustMem() {
        return modRMMustMem;
    }

    public boolean isModRMMustReg() {
        return modRMMustReg;
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

    void addOperand(OperandDesc opDesc) {
        operands.add(opDesc);
    }

    void setMnemonic(X86Mnemonic mnemonic) {
        this.mnemonic = mnemonic;
    }

    public X86Mnemonic getMnemonic() {
        return mnemonic;
    }

    public boolean hasEncodedRegister() {
        for(OperandDesc o : operands) {
            if(o.adrType == AddressType.LEAST_REG) {
                return true;
            }
        }
        return false;
    }

    public List<OperandDesc> getOperands() {
        return Collections.unmodifiableList(operands);
    }

    // negative from end of opcode
    public int getEncodedRegisterRelativeIndex() {
        int pos = 0;
        if(entry.secondOpcode != null) {
            pos = 1;
        }
        return pos;
    }

    public short[] getPrefix() {
        short[] res = new short[4];
        int i = 0;
        // if(entry.prefix != null) {
        //    res[i++] = entry.prefix;
        // }
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
        short[] prefix = getPrefix();
        StringBuilder res = new StringBuilder();
        if(entry.prefix != null) {
            res.append(String.format("%02X", entry.prefix));
        }
        for(short b : prefix) {
            res.append(String.format("%02X", b));
        }
        return res.toString();
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(String.format("<syntax mode=%s opcode=%s mnemonic=%s", entry.mode, getPrefixAsHexString(), mnemonic));
        for(int i = 0; i < operands.size(); i++) {
            res.append(String.format(" op%d=%s", i + 1, operands.get(i).toString()));
        }
        res.append(">");
        return res.toString();
    }
}
