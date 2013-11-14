package kianxali.gui;

import javax.swing.JPanel;
import javax.swing.JTextPane;

public class ImageView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTextPane editor;

    public ImageView() {
        editor = new JTextPane();
        add(editor);
    }
}
