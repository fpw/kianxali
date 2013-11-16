package kianxali.gui.model.imagefile;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ImageView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JEditorPane editor;

    public ImageView() {
        setLayout(new BorderLayout());
        editor = new JEditorPane();
        editor.setEditable(false);
        editor.setEditorKit(new ImageEditorKit());
        add(new JScrollPane(editor), BorderLayout.CENTER);
    }

    public ImageDocument getDocument() {
        return (ImageDocument) editor.getDocument();
    }

    public void setDocument(ImageDocument document) {
        editor.setDocument(document);
    }
}
