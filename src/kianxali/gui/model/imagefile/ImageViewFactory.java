package kianxali.gui.model.imagefile;

import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.ZoneView;

public class ImageViewFactory implements ViewFactory {
    public static final String ENTITY_NAME = "entity";
    public static final String ENTITY_KEY = "entity_key";
    private final ViewFactory delegate;

    public ImageViewFactory() {
        delegate = new StyledEditorKit().getViewFactory();
    }

    @Override
    public View create(Element e) {
        String kind = e.getName();
        if(kind.equals(ImageDocument.LineElementName)) {
            return new BoxView(e, ZoneView.Y_AXIS);
        } else {
            return delegate.create(e);
        }
    }
}
