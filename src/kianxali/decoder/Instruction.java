package kianxali.decoder;

import java.util.List;

/**
 * This interface represents a decoded instruction. To have the
 * disassembler architecture independent, instructions must supply
 * some information to the dissasembler through this interface.
 * @author fwi
 *
 */
public interface Instruction extends DecodedEntity {
    /**
     * If this instruction stops the current execution trace,
     * this method must return true. In other words: If the
     * next instruction following this instruction <i>can't</i>
     * be the next instruction in the byte sequence, it must return
     * true. Examples are RET and JMP, but not CALL or JNZ
     * @return true iff the instruction stops the current trace
     */
    boolean stopsTrace();

    /**
     * Return true if this instruction is a function call
     * @return true iff the instruction is a function call
     */
    boolean isFunctionCall();

    /**
     * Return true if this instruction is an unconditional jump.
     * If this is true, the destination should be returned
     * in {@link Instruction#getBranchAddresses()}.
     * @return true iff this instruction is an uncinditonal jump
     */
    boolean isUnconditionalJump();

    /**
     * Returns a string representation of the mnemonic (excluding operands)
     * @return a string containing the mnemonic
     */
    String getMnemonic();

    /**
     * Return a list of operands for this instruction
     * @return a list of operands, may not be null
     */
    List<Operand> getOperands();

    /**
     * Return a list of operands that are used as source
     * @return a list of source operands
     */
    List<Operand> getSrcOperands();

    /**
     * Return a list of operands that are used as destination
     * @return a list of destination operands
     */
    List<Operand> getDestOperands();

    /**
     * Return a list of possible branch addresses
     * @return a list containing branch addresses, may not be null
     */
    List<Long> getBranchAddresses();

    /**
     * Return a list of data references that are referenced
     * by this instruction
     * @return a list of data references, may not be null
     */
    List<Data> getAssociatedData();

    /**
     * Return a list of numbers that are likely to refer
     * to memory addresses of data.
     * @return a list of likely data addresses
     */
    List<Long> getProbableDataPointers();

    /**
     * Return the full instruction including operands as an array of bytes
     * @return a byte array containing the full instruction
     */
    short[] getRawBytes();

    /**
     * Return a short string description of the instruction
     * @return a short string describing the instruction
     */
    String getDescription();
}
