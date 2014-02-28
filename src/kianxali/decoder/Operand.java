package kianxali.decoder;

import kianxali.util.OutputFormatter;

/**
 * Represents an operand for an instruction
 * @author fwi
 *
 */
public interface Operand {
    /**
     * Returns whether the operand is a source or destination operand
     * @return the usage type of the operand
     */
    UsageType getUsage();

    /**
     * Tries to coerce this operand into a number representation
     * @return a number if the operand can be deterministically converted to a number, null otherwise
     */
    Number asNumber();

    /**
     * Returns a string representation of the operand
     * @param options the formatter to be used to format the operand
     * @return a string describing this operand
     */
    String asString(OutputFormatter options);
}
