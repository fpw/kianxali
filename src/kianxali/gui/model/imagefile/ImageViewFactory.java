package kianxali.gui.model.imagefile;

import java.util.logging.Logger;

import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class ImageViewFactory implements ViewFactory {
    private static final Logger LOG = Logger.getLogger("kianxali.gui.model.imagefile");

    @Override
    public View create(Element e) {
        LOG.finest("factory: create " + e);
        return new PlainView(e);
    }
}
