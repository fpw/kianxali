package kianxali;

import javax.swing.SwingUtilities;

import kianxali.gui.KianxaliGUI;

public final class Kianxali {
    // this is the startup class, hence no visible constructor
    private Kianxali() {

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                KianxaliGUI gui = new KianxaliGUI();
                gui.setVisible(true);
            }
        });
    }
}
