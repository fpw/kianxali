package kianxali.gui.models;

import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import kianxali.gui.views.MnemonicView;

public class ImageViewFactory implements ViewFactory {
    private final ViewFactory delegate;

    public ImageViewFactory() {
        delegate = new StyledEditorKit().getViewFactory();
    }

    @Override
    public View create(Element e) {
        String kind = e.getName();
        switch(kind) {
        case ImageDocument.AddressElementName:
            return new BoxView(e, BoxView.Y_AXIS);
        case ImageDocument.LineElementName:
            return new ParagraphView(e);
        case ImageDocument.MnemonicElementName:
            return new MnemonicView(e);
        default:
            return delegate.create(e);
        }
    }
}
