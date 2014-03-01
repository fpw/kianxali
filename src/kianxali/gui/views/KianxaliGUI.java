package kianxali.gui.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import kianxali.gui.Controller;

public class KianxaliGUI extends JFrame {
    private static final Logger LOG = Logger.getLogger("kianxali.gui");

    private static final long serialVersionUID = 1L;
    private final JDesktopPane desktop;
    private final ImageView imageView;
    private final FunctionListView functionView;
    private final StringListView stringView;
    private final ScriptView scriptView;
    private final LogView logView;
    private final Controller controller;

    public KianxaliGUI(Controller controller) {
        super("Kianxali");
        this.controller = controller;

        desktop = new JDesktopPane();
        add(desktop);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1120, 730));

        setupLookAndFeel();
        setupMenu();

        JTabbedPane tabPane = new JTabbedPane();
        functionView = new FunctionListView(controller);
        tabPane.addTab("Functions", functionView);
        stringView = new StringListView(controller);
        tabPane.addTab("Strings", stringView);
        scriptView = new ScriptView(controller);
        tabPane.addTab("Script", scriptView);

        JInternalFrame functionFrame = new JInternalFrame("Entities", true, false, true, true);
        functionFrame.setLocation(new Point(0, 0));
        functionFrame.setSize(new Dimension(300, 680));
        functionFrame.add(tabPane);
        functionFrame.setVisible(true);
        desktop.add(functionFrame);

        JInternalFrame imageFrame = new JInternalFrame("Disassembly", true, false, true, true);
        imageFrame.setLayout(new BorderLayout());
        imageFrame.setLocation(new Point(300, 0));
        imageFrame.setSize(new Dimension(800, 480));
        imageView = new ImageView(controller);
        imageFrame.add(imageView, BorderLayout.CENTER);
        imageFrame.setVisible(true);
        desktop.add(imageFrame);

        JInternalFrame logFrame = new JInternalFrame("Log", true, false, true, true);
        logFrame.setLayout(new BorderLayout());
        logFrame.setLocation(new Point(300, 480));
        logFrame.setSize(new Dimension(800, 200));
        logView = new LogView(controller);
        logFrame.add(logView, BorderLayout.CENTER);
        logFrame.setVisible(true);
        desktop.add(logFrame);

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
            LOG.info("Couldn't set system look and feel");
        }
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem fileOpen = new JMenuItem("Open");
        fileOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.onOpenFileRequest();
            }
        });
        fileMenu.add(fileOpen);

        JMenuItem fileSave = new JMenuItem("Save patched version");
        fileSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.onSavePatchedRequest();
            }
        });
        fileMenu.add(fileSave);

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.onExitRequest();
            }
        });
        fileMenu.add(exit);

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem gotoAddr = new JMenuItem("Goto location");
        gotoAddr.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String where = JOptionPane.showInputDialog("Goto where (hex mem address)?");
                if(where != null) {
                    controller.onGotoRequest(where);
                }
            }
        });
        gotoAddr.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.CTRL_MASK));
        editMenu.add(gotoAddr);

        JMenuItem clearLog = new JMenuItem("Clear log");
        clearLog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.onClearLogRequest();
            }
        });
        editMenu.add(clearLog);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(KianxaliGUI.this, "<html>Kianxali<br>© 2014 by Folke Will</html>", "About Kianxali", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        helpMenu.add(about);


        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
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

    public void showSavePatchedDialog() {
        JFileChooser chooser = new JFileChooser("./");
        int res = chooser.showSaveDialog(this);
        if(res == JFileChooser.APPROVE_OPTION) {
            controller.onPatchedSave(chooser.getSelectedFile().toPath());
        }
    }

    public ImageView getImageView() {
        return imageView;
    }

    public ScriptView getScriptView() {
        return scriptView;
    }

    public FunctionListView getFunctionListView() {
        return functionView;
    }

    public StringListView getStringListView() {
        return stringView;
    }

    public LogView getLogView() {
        return logView;
    }
}
