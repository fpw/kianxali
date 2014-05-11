package kianxali.gui.views;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;

import org.jdesktop.swingx.JXEditorPane;

import kianxali.disassembler.DataEntry;
import kianxali.disassembler.Function;
import kianxali.gui.Controller;
import kianxali.gui.models.ImageDocument;
import kianxali.gui.models.ImageEditorKit;

public class ImageView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Controller controller;
    private final JXEditorPane editor;
    private final StatusView statusView;
    private final JScrollPane scrollPane;

    public ImageView(final Controller controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        statusView = new StatusView();
        add(statusView, BorderLayout.NORTH);

        editor = new JXEditorPane() {
            private static final long serialVersionUID = -481545877802655847L;

            @Override
            public boolean getScrollableTracksViewportWidth() {
                // disable line wrap, source: http://tips4java.wordpress.com/2009/01/25/no-wrap-text-pane/
                return getUI().getPreferredSize(this).width <= getParent().getSize().width;
            }
        };
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
                int index = editor.viewToModel(e.getPoint());
                if(SwingUtilities.isLeftMouseButton(e)) {
                    controller.onDisassemblyLeftClick(index);
                } else if(SwingUtilities.isRightMouseButton(e)) {
                    onRightClick(index, e.getPoint());
                }
            }
        });

        ToolTipManager.sharedInstance().registerComponent(editor);

        scrollPane = new JScrollPane(editor);
        scrollPane.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                onScrollChange();
            }
        });
        scrollPane.setRowHeaderView(new CrossReferenceHeader(controller, editor));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setCaretPos(int offset) {
        editor.setCaretPosition(offset);
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
                editor.setCaretPosition(pos);
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
        scrollPane.getRowHeader().repaint();
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

    private void onRightClick(final int index, Point p) {
        boolean hasEntries = false;
        if(index < 0) {
            return;
        }
        Document doc = editor.getDocument();
        if(!(doc instanceof ImageDocument)) {
            return;
        }

        ImageDocument imageDoc = (ImageDocument) doc;

        Element elem = imageDoc.getCharacterElement(index);
        if(elem == null) {
            return;
        }

        JPopupMenu menu = new JPopupMenu("Actions");

        // Right clicking on mnemonic
        if(elem.getName() == ImageDocument.MnemonicElementName) {
            menu.add("Convert to NOP").addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    controller.onConvertToNOP(index);
                }
            });
            hasEntries = true;
        }

        // General right clicking on something that has a memory address
        Object memAddrObj = elem.getAttributes().getAttribute(ImageDocument.MemAddressKey);
        if(memAddrObj instanceof Long) {
            long memAddr = (Long) memAddrObj;
            final DataEntry data = controller.getDisassemblyData().getInfoOnExactAddress(memAddr);
            if(data != null) {
                // Function renaming
                if(data.getStartFunction() != null) {
                    final Function fun = data.getStartFunction();
                    menu.add("Rename " + fun.getName()).addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            String newName = JOptionPane.showInputDialog("New name for " + fun.getName() + ": ");
                            if(newName != null) {
                                controller.onFunctionRenameReq(fun, newName);
                            }
                        }
                    });
                    hasEntries = true;
                }

                // Commenting
                menu.add("Change Comment").addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String comment = JOptionPane.showInputDialog("Comment: ", data.getComment());
                        if(comment != null) {
                            controller.onCommentChangeReq(data, comment);
                        }
                    }
                });
                hasEntries = true;
            }
        }

        if(hasEntries) {
            menu.show(editor, p.x, p.y);
        }
    }

    public StatusView getStatusView() {
        return statusView;
    }

    public ImageDocument getDocument() {
        if(editor.getDocument() instanceof ImageDocument) {
            return (ImageDocument) editor.getDocument();
        } else {
            return null;
        }
    }

    public void setDocument(ImageDocument document) {
        if(document != null) {
            editor.setDocument(document);
        } else {
            editor.setDocument(new DefaultStyledDocument());
        }
        editor.getCaret().setVisible(true);
    }
}
