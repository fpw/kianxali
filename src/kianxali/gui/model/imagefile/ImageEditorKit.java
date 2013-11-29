package kianxali.gui.model.imagefile;

import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

public class ImageEditorKit extends StyledEditorKit {
    private static final long serialVersionUID = 1L;

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    @Override
    public ViewFactory getViewFactory() {
       return new ImageViewFactory();
    }
}

