package kianxali.gui.model.imagefile;

import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class ImageViewFactory implements ViewFactory {
    private final ViewFactory delegate;

    public ImageViewFactory() {
        delegate = new StyledEditorKit().getViewFactory();
    }

    @Override
    public View create(Element e) {
        return delegate.create(e);
    }
}
