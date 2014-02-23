package kianxali.disassembler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import kianxali.decoder.Data;
import kianxali.decoder.DecodedEntity;
import kianxali.decoder.Instruction;
import kianxali.loader.ImageFile;
import kianxali.loader.Section;

// an address can be associated with:
//  * references to other entries
//  * start of file
//  * start of segment, end of segment
//  * start of function, end of function
//  * instruction or data
//  * user comment
//  * data comment

public class DataEntry {
    private final long address;
    private ImageFile startImageFile;
    private Section startSection, endSection;
    private Function startFunction, endFunction;
    private DecodedEntity entity;
    private Data attachedData;
    private String comment;
    private final Set<DataEntry> references;

    public DataEntry(long address) {
        this.address = address;
        references = new HashSet<>();
    }

    public long getAddress() {
        return address;
    }

    public void attachData(Data data) {
        this.attachedData = data;
    }

    public Data getAttachedData() {
        return attachedData;
    }

    public void clearAttachedData() {
        attachedData = null;
    }

    public void addReferenceFrom(DataEntry src) {
        references.add(src);
    }

    public void removeReference(DataEntry src) {
        references.remove(src);
    }

    public Set<DataEntry> getReferences() {
        return Collections.unmodifiableSet(references);
    }

    public boolean hasData() {
        return entity instanceof Data;
    }

    public boolean hasInstruction() {
        return entity instanceof Instruction;
    }

    public void setStartImageFile(ImageFile startImageFile) {
        this.startImageFile = startImageFile;
    }

    public void setStartSection(Section startSection) {
        this.startSection = startSection;
    }

    public void setStartFunction(Function startFunction) {
        this.startFunction = startFunction;
    }

    public void setEntity(DecodedEntity entity) {
        this.entity = entity;
    }

    public void setEndFunction(Function endFunction) {
        this.endFunction = endFunction;
    }

    public void setEndSection(Section endSection) {
        this.endSection = endSection;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ImageFile getStartImageFile() {
        return startImageFile;
    }

    public Section getStartSection() {
        return startSection;
    }

    public Function getStartFunction() {
        return startFunction;
    }

    public DecodedEntity getEntity() {
        return entity;
    }

    public String getComment() {
        return comment;
    }

    public Function getEndFunction() {
        return endFunction;
    }

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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DataEntry other = (DataEntry) obj;
        if (address != other.address) {
            return false;
        }
        return true;
    }
}
