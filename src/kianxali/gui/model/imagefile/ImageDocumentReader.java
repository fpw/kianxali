package kianxali.gui.model.imagefile;

import java.util.List;

import javax.swing.text.SimpleAttributeSet;

import kianxali.image.ImageFile;
import kianxali.image.Section;

public class ImageDocumentReader {
    private final ImageDocument doc;

    public ImageDocumentReader(ImageDocument doc) {
        this.doc = doc;
    }

    public void read(ImageFile imageFile) {
        doc.insertImageFile(imageFile.getSections().size(), new SimpleAttributeSet());
        List<Section> sections = imageFile.getSections();
        for(int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttribute(ImageDocument.OFFSET_ATTRIBUTE, section.getStartAddress());
            doc.insertSection(i, "section " + i + ": " + section.getName(), attrs);
        }
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute(ImageDocument.OFFSET_ATTRIBUTE, 12);
        doc.insertEntity(0, 12, "mov ebp, esp", attrs);
    }
}
