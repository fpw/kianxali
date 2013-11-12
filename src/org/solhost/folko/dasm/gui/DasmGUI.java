package org.solhost.folko.dasm.gui;

import javax.swing.JFrame;

public class DasmGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private final ImageView imageView;

    public DasmGUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        imageView = new ImageView();
        add(imageView);

        pack();
    }
}
