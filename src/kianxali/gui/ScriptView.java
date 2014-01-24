package kianxali.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jsyntaxpane.DefaultSyntaxKit;

public class ScriptView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Controller controller;
    private final JEditorPane editor;

    public ScriptView(Controller ctrl) {
        this.controller = ctrl;
        DefaultSyntaxKit.initKit(); // text/ruby etc. to editors
        setLayout(new BorderLayout());

        this.editor = new JEditorPane();
        JScrollPane editScroll = new JScrollPane(editor);
        editor.setContentType("text/ruby");
        editor.setText("# Enter your Ruby code here\n" + "puts 'Hello, Kianxali'\n");
        add(editScroll, BorderLayout.CENTER);

        JButton runButton = new JButton("Run Script");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.onRunScriptRequest();
            }
        });
        add(runButton, BorderLayout.SOUTH);
    }

    public String getScript() {
        return editor.getText();
    }
}
