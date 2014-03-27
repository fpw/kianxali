package kianxali.disassembler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import kianxali.decoder.Data;
import kianxali.decoder.DecodedEntity;
import kianxali.decoder.Instruction;
import kianxali.loader.ImageFile;
import kianxali.loader.Section;

/**
 * This class represents all information that can be associated with a memory address,
 * such as the start of a function, actual instructions, data, comments made by the user
 * etc.
 * The setter methods are package private because the entries should be made through the
 * {@link DisassemblyData} class so the listeners get informed.
 * @author fwi
 *
 */
public class DataEntry {
    private final long address;
    private ImageFile startImageFile;
    private Section startSection, endSection;
    private Function startFunction, endFunction;
    private DecodedEntity entity;
    private Data attachedData;
    private String comment;
    private final Set<DataEntry> references;

    DataEntry(long address) {
        this.address = address;
        references = new HashSet<>();
    }

    /**
     * Returns the memory address of this entry
     * @return the memory address of this entry
     */
    public long getAddress() {
        return address;
    }

    void attachData(Data data) {
        this.attachedData = data;
    }

    /**
     * Get the data object that is associated with this entry,
     * e.g. for lea eax, offset "some string" it will return the
     * data entry for "some string"
     * @return the data object associated with this entry or null
     */
    public Data getAttachedData() {
        return attachedData;
    }

    void clearAttachedData() {
        attachedData = null;
    }

    void clearReferences() {
        references.clear();
    }

    void addReferenceFrom(DataEntry src) {
        references.add(src);
    }

    boolean removeReference(DataEntry src) {
        return references.remove(src);
    }

    /**
     * Get all from-references to this entry, i.e. all locations that
     * refer to this address.
     * @return a set of entries that refer to this address
     */
    public Set<DataEntry> getReferences() {
        return Collections.unmodifiableSet(references);
    }

    /**
     * Checks if this entry is a data entry
     * @return true if {@link DataEntry#getEntity()} is of type {@link Data}
     */
    public boolean hasData() {
        return entity instanceof Data;
    }

    /**
     * Checks if this entry is an instruction entry
     * @return true if {@link DataEntry#getEntity()} is of type {@link Instruction}
     */
    public boolean hasInstruction() {
        return entity instanceof Instruction;
    }

    void setStartImageFile(ImageFile startImageFile) {
        this.startImageFile = startImageFile;
    }

    void setStartSection(Section startSection) {
        this.startSection = startSection;
    }

    void setStartFunction(Function startFunction) {
        this.startFunction = startFunction;
    }

    void setEntity(DecodedEntity entity) {
        this.entity = entity;
    }

    void setEndFunction(Function endFunction) {
        this.endFunction = endFunction;
    }

    void setEndSection(Section endSection) {
        this.endSection = endSection;
    }

    void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns the image file if this address is the start of an image file
     * @return the image file associated with this address if it starts the image, or null
     */
    public ImageFile getStartImageFile() {
        return startImageFile;
    }

    /**
     * Returns the section if this address is the start of a section
     * @return the section started by this address or null
     */
    public Section getStartSection() {
        return startSection;
    }

    /**
     * Returns the function if this address is the start of a function
     * @return the function started by this address or null
     */
    public Function getStartFunction() {
        return startFunction;
    }

    /**
     * Returns the entity (instruction or data) stored at the address
     * @return the entity contained in this entry or null
     */
    public DecodedEntity getEntity() {
        return entity;
    }

    /**
     * Returns the user comment associated with this entry
     * @return the user comment stored at this entry or null
     */
    public String getComment() {
        return comment;
    }

    /**
     * Returns the function that ends on this address
     * @return the function that ends on this address or null
     */
    public Function getEndFunction() {
        return endFunction;
    }

    /**
     * Returns the section if this address is the end of a section
     * @return the section ended by this address or null
     */
    public Section getEndSection() {
        return endSection;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (address ^ (address >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        DataEntry other = (DataEntry) obj;
        if(address != other.address) {
            return false;
        }
        return true;
    }
}
