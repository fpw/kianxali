package kianxali.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class KianxaliGUI extends JFrame {
    private static final Logger LOG = Logger.getLogger("kianxali.gui");

    private static final long serialVersionUID = 1L;
    private final ImageView imageView;
    private final Controller controller;

    public KianxaliGUI(Controller controller) {
        super("Kianxali");
        this.controller = controller;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 480));

        setupLookAndFeel();
        setupMenu();

        setLayout(new BorderLayout());
        imageView = new ImageView();
        add(imageView, BorderLayout.CENTER);

        pack();
    }

    private void setupLookAndFeel() {
        if(System.getProperty("os.name").equals("Mac OS X")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // doesn't matter, just use default laf
            LOG.info("Couldn't set platform look and feel");
        }
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem fileOpen = new JMenuItem("Open");
        fileOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.onOpenFileRequest();
            }
        });
        fileMenu.add(fileOpen);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }

    public void showError(String caption, String message) {
        JOptionPane.showMessageDialog(this, message, caption, JOptionPane.ERROR_MESSAGE);
    }

    public void showFileOpenDialog() {
        JFileChooser chooser = new JFileChooser("./");
        int res = chooser.showOpenDialog(this);
        if(res == JFileChooser.APPROVE_OPTION) {
            controller.onFileOpened(chooser.getSelectedFile());
        }
    }

    public ImageView getImageView() {
        return imageView;
    }
}
