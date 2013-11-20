package kianxali.gui.model.imagefile;

import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.Segment;

import kianxali.decoder.DecodedEntity;

public class AddressView extends LabelView {
    private final Segment adrSegment;

    public AddressView(Element elem) {
        super(elem);
        DecodedEntity entity = (DecodedEntity) elem.getAttributes().getAttribute(ImageViewFactory.ENTITY_KEY);
        long address = entity.getMemAddress();

        String line = String.format("%08X", address);
        adrSegment = new Segment(line.toCharArray(), 0, line.length());
    }

    @Override
    public Segment getText(int p0, int p1) {
        return adrSegment;
    }

}
