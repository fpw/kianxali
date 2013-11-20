package kianxali.gui.model.imagefile;

import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.Segment;
import kianxali.decoder.DecodedEntity;
import kianxali.util.OutputFormatter;

public class EntityView extends LabelView {
    private final String entityLine;
    private final DecodedEntity entity;
    private final OutputFormatter formatter;
    private final Segment segment;

    // TODO: ZoneView

    public EntityView(Element elem) {
        super(elem);
        entity = (DecodedEntity) elem.getAttributes().getAttribute(ImageViewFactory.ENTITY_KEY);
        formatter = (OutputFormatter) elem.getDocument().getProperty(ImageDocument.FORMATTER_KEY);
        entityLine = entity.asString(formatter);

        String line = String.format("%08X: %s", entity.getMemAddress(), entityLine);
        segment = new Segment(line.toCharArray(), 0, line.length());
    }

    @Override
    public Segment getText(int p0, int p1) {
        return segment;
    }
}
