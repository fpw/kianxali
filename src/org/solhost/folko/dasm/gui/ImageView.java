package org.solhost.folko.dasm.gui;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

public class ImageView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JEditorPane editor;

    public ImageView() {
        editor = new JEditorPane();
        add(editor);
    }
}
