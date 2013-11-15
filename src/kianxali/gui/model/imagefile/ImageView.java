package kianxali.gui.model.imagefile;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import kianxali.image.ImageFile;

public class ImageView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JEditorPane editor;

    public ImageView() {
        editor = new JEditorPane();
        editor.setEditorKit(new ImageEditorKit());
        add(editor);
    }

    public void setImageFile(ImageFile image) {
        ImageDocument doc = (ImageDocument) editor.getDocument();
        doc.setImageFile(image);
    }
}
