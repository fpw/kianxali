package kianxali.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

import kianxali.gui.models.ImageDocument;
import kianxali.gui.models.ImageEditorKit;

public class ImageView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Controller controller;
    private final JEditorPane editor;
    private final StatusView statusView;
    private final JScrollPane scrollPane;

    public ImageView(final Controller controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        statusView = new StatusView();
        add(statusView, BorderLayout.NORTH);

        editor = new JEditorPane();
        editor.setEditable(false);
        editor.setEditorKit(new ImageEditorKit());
        editor.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) { }

            public void mouseMoved(MouseEvent e) {
                int index = editor.viewToModel(e.getPoint());
                mouseOverIndex(index);
            }
        });
        editor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    int index = editor.viewToModel(e.getPoint());
                    controller.onDisassemblyLeftClick(index);
                }
            }
        });

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
                // TODO: Hack or supposed to be like this?
                editor.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
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

    private void mouseOverIndex(int index) {
        Document doc = editor.getDocument();
        if(index < 0 || !(doc instanceof ImageDocument)) {
            return;
        }
        ImageDocument imageDoc = (ImageDocument) doc;

        Element elem = imageDoc.getCharacterElement(index);
        if(elem == null) {
            return;
        }

        if(elem.getAttributes().getAttribute(ImageDocument.RefAddressKey) != null) {
            editor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            editor.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
