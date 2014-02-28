package kianxali.loader;

/**
 * This interface describes a virtual memory section in an image file's memory layout.
 * @author fwi
 *
 */
public interface Section {
    /**
     * Returns the name of the memory section
     * @return the name of the memory section, can also be empty or null depending on the image file type
     */
    String getName();

    /**
     * Returns whether the section is marked as executable in the memory layout
     * @return true iff the section is marked as executable
     */
    boolean isExecutable();

    /**
     * Returns the virtual memory start address for this section
     * @return the start address of this section
     */
    long getStartAddress();

    /**
     * Returns the virtual memory end address for this section
     * @return the end address of this section (inclusive)
     */
    long getEndAddress();
}
