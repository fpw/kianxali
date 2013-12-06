package kianxali.gui.model.imagefile;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import kianxali.gui.Controller;

public class ImageView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Controller controller;
    private final JEditorPane editor;
    private final StatusView statusView;
    private final JScrollPane scrollPane;

    public ImageView(Controller controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        statusView = new StatusView();
        add(statusView, BorderLayout.NORTH);

        editor = new JEditorPane();
        editor.setEditable(false);
        editor.setEditorKit(new ImageEditorKit());

        scrollPane = new JScrollPane(editor);
        scrollPane.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                onScrollChange();
            }
        });
        add(scrollPane, BorderLayout.CENTER);
    }

    public void scrollTo(long memAddr) throws BadLocationException {
        Document doc = editor.getDocument();
        if(doc instanceof ImageDocument) {
            JViewport viewport = (JViewport) editor.getParent();
            Integer pos = ((ImageDocument) doc).getOffsetForAddress(memAddr);
            if(pos != null) {
                Rectangle rect = editor.modelToView(pos);
                rect.y += viewport.getExtentSize().height - rect.height;
                editor.scrollRectToVisible(rect);
            }
        }
    }

    private void onScrollChange() {
        Document doc = editor.getDocument();
        if(doc instanceof ImageDocument) {
            int scroll = scrollPane.getVerticalScrollBar().getValue();
            int pos = editor.viewToModel(new Point(0, scroll));
            Long addr = ((ImageDocument) doc).getAddressForOffset(pos);
            if(addr != null) {
                controller.onScrollChange(addr);
            }
        }

    }

    public StatusView getStatusView() {
        return statusView;
    }

    public ImageDocument getDocument() {
        return (ImageDocument) editor.getDocument();
    }

    public void setDocument(ImageDocument document) {
        editor.setDocument(document);
        editor.getCaret().setVisible(true);
    }
}
