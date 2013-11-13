package org.solhost.folko.dasm.gui;

import javax.swing.SwingUtilities;

public final class Dasm {
    // Utility class without constructor
    private Dasm() {

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DasmGUI gui = new DasmGUI();
                gui.setVisible(true);
            }
        });
    }
}
