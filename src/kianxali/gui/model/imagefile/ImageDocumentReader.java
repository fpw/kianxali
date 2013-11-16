package kianxali.gui.model.imagefile;

import java.util.List;
import java.util.Map.Entry;

import javax.swing.text.SimpleAttributeSet;

import kianxali.decoder.DecodedEntity;
import kianxali.disassembler.DisassemblyData;
import kianxali.image.ImageFile;
import kianxali.image.Section;
import kianxali.util.OutputFormatter;

public class ImageDocumentReader {
    private final ImageDocument doc;
    private final OutputFormatter formatter;

    public ImageDocumentReader(ImageDocument doc) {
        this.doc = doc;
        this.formatter = new OutputFormatter();
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
        // doc.insertEntity(0, 12, "mov ebp, esp", attrs);
    }

    public void readDisassembly(DisassemblyData disassemblyData) {
        for(Entry<Long, DecodedEntity> entry : disassemblyData.getEntities()) {
            doc.insertEntity(0, entry.getKey(), entry.getValue().asString(formatter) + "\n", null);
        }
    }
}
