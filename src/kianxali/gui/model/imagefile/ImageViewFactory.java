package kianxali.gui.model.imagefile;

import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class ImageViewFactory implements ViewFactory {
    @Override
    public View create(Element e) {
        return new PlainView(e);
    }
}
