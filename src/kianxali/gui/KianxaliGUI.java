package kianxali.gui;

import javax.swing.JFrame;

public class KianxaliGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private final ImageView imageView;

    public KianxaliGUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        imageView = new ImageView();
        add(imageView);

        pack();
    }
}
