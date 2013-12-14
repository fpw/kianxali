package kianxali.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

public class KianxaliGUI extends JFrame {
    private static final Logger LOG = Logger.getLogger("kianxali.gui");

    private static final long serialVersionUID = 1L;
    private final JDesktopPane desktop;
    private final ImageView imageView;
    private final FunctionListView functionView;
    private final StringListView stringView;
    private final Controller controller;

    public KianxaliGUI(Controller controller) {
        super("Kianxali");
        this.controller = controller;

        desktop = new JDesktopPane();
        add(desktop);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1000, 500));

        setupLookAndFeel();
        setupMenu();

        JTabbedPane tabPane = new JTabbedPane();
        functionView = new FunctionListView(controller);
        tabPane.addTab("Functions", functionView);
        stringView = new StringListView(controller);
        tabPane.addTab("Strings", stringView);

        JInternalFrame functionFrame = new JInternalFrame("Entities", true, false, true, true);
        functionFrame.setLocation(new Point(0, 0));
        functionFrame.setSize(new Dimension(200, 480));
        functionFrame.add(tabPane);
        functionFrame.setVisible(true);
        desktop.add(functionFrame);

        JInternalFrame imageFrame = new JInternalFrame("Disassembly", true, false, true, true);
        imageFrame.setLayout(new BorderLayout());
        imageFrame.setLocation(new Point(200, 0));
        imageFrame.setSize(new Dimension(800, 480));
        imageView = new ImageView(controller);
        imageFrame.add(imageView, BorderLayout.CENTER);
        imageFrame.setVisible(true);
        desktop.add(imageFrame);

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
            controller.onFileOpened(chooser.getSelectedFile().toPath());
        }
    }

    public ImageView getImageView() {
        return imageView;
    }

    public FunctionListView getFunctionListView() {
        return functionView;
    }

    public StringListView getStringListView() {
        return stringView;
    }
}
