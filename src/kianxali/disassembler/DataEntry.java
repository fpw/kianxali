package kianxali.disassembler;

import kianxali.decoder.Data;
import kianxali.decoder.DecodedEntity;
import kianxali.decoder.Instruction;
import kianxali.image.ImageFile;
import kianxali.image.Section;

// an address can be associated with:
//  * start of file
//  * start of segment, end of segment
//  * start of function, end of function
//  * instruction or data
//  * comment

public class DataEntry {
    private ImageFile startImageFile;
    private Section startSection, endSection;
    private Function startFunction, endFunction;
    private DecodedEntity entity;
    private String comment;

    public DataEntry() {
    }

    public DataEntry(ImageFile startFile) {
        this.startImageFile = startFile;
    }

    public DataEntry(Section startSection) {
        this.startSection = startSection;
    }

    public DataEntry(DecodedEntity entity) {
        this.entity = entity;
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
}
