package kianxali.decoder.arch.x86.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kianxali.decoder.arch.x86.X86Mnemonic;
import kianxali.decoder.arch.x86.xml.OperandDesc.AddressType;

/**
 * Represents the syntax of an opcode, i.e. the number and
 * format of its operands.
 * @author fwi
 *
 */
public class OpcodeSyntax {
    private final OpcodeEntry entry; // syntax belongs to this entry
    private final List<OperandDesc> operands;
    private Short extension;
    private boolean modRMMustMem, modRMMustReg;
    private X86Mnemonic mnemonic;

    {
        this.operands = new ArrayList<>(4);
    }

    OpcodeSyntax(OpcodeEntry entry) {
        this.entry = entry;
    }

    OpcodeSyntax(OpcodeEntry entry, short extension) {
        this.entry = entry;
        this.extension = extension;
    }

    void setModRMMustMem(boolean must) {
        this.modRMMustMem = must;
    }

    void setModRMMustReg(boolean must) {
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

    /**
     * Returns whether the opcode is an extension of another one,
     * in which case {@link OpcodeSyntax#getExtension()} will
     * return the actual extension number.
     * @return true iff the opcode extends another one
     */
    public boolean isExtended() {
        return extension != null;
    }

    /**
     * Returns the extension number if this opcode extends
     * another one
     * @return the extension number of this opcode or null if not extending
     */
    public Short getExtension() {
        return extension;
    }

    void addOperand(OperandDesc opDesc) {
        operands.add(opDesc);
    }

    void setMnemonic(X86Mnemonic mnemonic) {
        this.mnemonic = mnemonic;
    }

    /**
     * Returns the mnemonic of this opcode
     * @return this opcode's mnemonic. Can be null, e.g. when used on a prefix
     */
    public X86Mnemonic getMnemonic() {
        return mnemonic;
    }

    /**
     * Returns whether this opcode has a register encoded in the opcode
     * byte. Use {@link OpcodeSyntax#getEncodedRegisterRelativeIndex()} to get
     * the position of the byte.
     * @return true iff the opcode byte encodes a register in the least 3 bit
     */
    public boolean hasEncodedRegister() {
        for(OperandDesc o : operands) {
            if(o.adrType == AddressType.LEAST_REG) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the operands of this opcode.
     * @return the operands as an unmodifable list, never null
     */
    public List<OperandDesc> getOperands() {
        return Collections.unmodifiableList(operands);
    }

    // negative from end of opcode
    /**
     * If the opcode byte encodes a register, this method
     * returns the index from the end of the opcode that
     * encodes the register.
     * @return the index from the end of the opcode bytes that encodes a register
     */
    public int getEncodedRegisterRelativeIndex() {
        int pos = 0;
        if(entry.secondOpcode != null) {
            pos = 1;
        }
        return pos;
    }

    /**
     * Returns the full opcode prefix (including the opcode bytes,
     * but excluding the operand bytes) for this opcode.
     * If the opcode has a mandatory prefix byte, it will not be
     * included here.
     * @return the bytes that make up this opcode, excluding mandatory prefix
     */
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

    /**
     * Returns a hex string representation of the full opcode bytes,
     * including mandatory prefixes but excluding operands.
     * @return a hex string representing this opcode
     */
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
