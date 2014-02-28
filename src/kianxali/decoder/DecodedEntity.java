package kianxali.decoder;

import kianxali.util.OutputFormatter;

/**
 * An entity is a decoded instruction or data with a fixed
 * memory address. Every memory address can become either
 * data or instruction.
 * @author fwi
 *
 */
public interface DecodedEntity {
    /**
     * Returns the memory address of this entity
     * @return the memory address of this entity
     */
    long getMemAddress();

    /**
     * Returns the size of this entity in bytes
     * @return the size in bytes
     */
    int getSize();

    /**
     * Converts this entity into a string representation
     * @param format the formatter to use
     * @return a string describing the entity
     */
    String asString(OutputFormatter format);
}
