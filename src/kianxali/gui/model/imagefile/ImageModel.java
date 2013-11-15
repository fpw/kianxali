package kianxali.gui.model.imagefile;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;

public class ImageModel extends AbstractDocument {
    private static final long serialVersionUID = 1L;

    protected ImageModel(Content data) {
        super(data);
    }

    @Override
    public Element getDefaultRootElement() {
        return null;
    }

    @Override
    public Element getParagraphElement(int pos) {
        return null;
    }
}
