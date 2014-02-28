package kianxali.gui.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

import kianxali.decoder.DecodedEntity;
import kianxali.decoder.Instruction;
import kianxali.disassembler.DataEntry;
import kianxali.disassembler.DisassemblyData;
import kianxali.gui.Controller;
import kianxali.gui.models.ImageDocument;

public class CrossReferenceHeader extends JPanel implements DocumentListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger("kianxali.gui.views");
    private static final Color[] DISTANT_COLORS = {
        Color.GREEN, Color.ORANGE, Color.YELLOW,
        Color.RED,   Color.BLUE,   Color.BLACK,
        Color.WHITE, Color.CYAN,   Color.MAGENTA,
        Color.PINK};

    private final Controller controller;
    private final JTextComponent component;
    private int lastHeight;
    private ImageDocument imageDoc;

    private class LineEntry implements Comparable<LineEntry> {
        int fromY, toY, shiftX;

        public LineEntry(int from, int to) {
            this.fromY = from;
            this.toY = to;
        }

        @Override
        public int compareTo(LineEntry o) {
            if(fromY != o.fromY) {
                return Integer.compare(fromY, o.fromY);
            } else {
                return Integer.compare(toY, o.toY);
            }
        }
    }

    public CrossReferenceHeader(Controller controller, final JTextComponent component) {
        this.controller = controller;
        this.component = component;

        // not adding the document listener yet because the initial document is never an ImageDocument

        component.addPropertyChangeListener("document", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Document doc = component.getDocument();
                if(doc instanceof ImageDocument) {
                    imageDoc = (ImageDocument) doc;
                    imageDoc.addDocumentListener(CrossReferenceHeader.this);
                } else {
                    imageDoc = null;
                }
                documentChanged();
            }
        });

        // without this height, weird painting problems will occur :-/
        // but due to clipping, it shouldn't be too bad
        setPreferredSize(new Dimension(50, Integer.MAX_VALUE));
    }

    private void documentChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    int endPos = component.getDocument().getLength();
                    Rectangle rect = component.modelToView(endPos);
                    if(rect != null && rect.y != lastHeight) {
                        repaint();
                        lastHeight = rect.y;
                    }
                } catch (BadLocationException e) {
                    // doesn't matter
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g2);
        if(imageDoc == null) {
            return;
        }

        // get visible addresses
        Rectangle clip = g.getClipBounds();
        int startOffset = component.viewToModel(new Point(0, clip.y));
        int endOffset = component.viewToModel(new Point(0, clip.y + clip.height));
        g2.setColor(Color.GRAY);
        g2.fillRect(clip.x, clip.y, clip.width, clip.height);
        g2.setStroke(new BasicStroke(1.5f));
        NavigableSet<LineEntry> references = buildReferences(startOffset, endOffset);
        shiftReferences(references);
        for(LineEntry line : references) {
            int x = clip.width - 8 - line.shiftX * 4;
            g2.setColor(DISTANT_COLORS[line.shiftX % DISTANT_COLORS.length]);
            g2.drawLine(x, line.fromY, clip.width, line.fromY);
            g2.drawLine(x, line.fromY, x, line.toY);
            g2.drawLine(x, line.toY, clip.width, line.toY);
            // arrow tip
            g2.drawLine(clip.width - 4, line.toY - 4, clip.width + 4, line.toY);
            g2.drawLine(clip.width - 4, line.toY + 4, clip.width + 4, line.toY);
        }
    }

    private void shiftReferences(NavigableSet<LineEntry> references) {
        for(LineEntry entry : references) {
            // count the number of lines that must be skipped
            for(LineEntry other : references) {
                if(other == entry) {
                    continue;
                }
                if(other.fromY <= entry.toY && other.fromY >= entry.fromY) {
                    entry.shiftX++;
                }
            }
        }
    }

    // from screen y position to list of other screen y positions
    private NavigableSet<LineEntry> buildReferences(int startOffset, int endOffset) {
        NavigableSet<LineEntry> res = new TreeSet<>();
        DisassemblyData data = controller.getDisassemblyData();
        if(data == null) {
            return res;
        }

        int curOffset = startOffset;
        while(curOffset <= endOffset) {
            try {
                Long memAddr = imageDoc.getAddressForOffset(curOffset);
                if(memAddr == null) {
                    break;
                }
                int thisY = getYPosition(curOffset);

                // add to-references
                DecodedEntity srcEntity = data.getEntityOnExactAddress(memAddr);
                if(srcEntity instanceof Instruction) {
                    Instruction inst = (Instruction) srcEntity;
                    List<Long> branchAddrs = inst.getBranchAddresses();
                    if(!branchAddrs.isEmpty() && !inst.isFunctionCall()) {
                        for(Long branchAddr : inst.getBranchAddresses()) {
                            int toY = getYPosition(imageDoc.getOffsetForAddress(branchAddr));
                            res.add(new LineEntry(thisY, toY));
                        }
                    }
                }

                // add from-references
                DataEntry entry = data.getInfoOnExactAddress(memAddr);
                if(entry != null && entry.getEntity() instanceof Instruction) {
                    for(DataEntry fromEntry : entry.getReferences()) {
                        if(fromEntry.getEntity() instanceof Instruction) {
                            Instruction inst = (Instruction) fromEntry.getEntity();
                            if(!inst.isFunctionCall()) {
                                int fromY = getYPosition(imageDoc.getOffsetForAddress(fromEntry.getAddress()));
                                res.add(new LineEntry(fromY, thisY));
                            }
                        }
                    }
                }


                // determine next model position
                int rowEnd = Utilities.getRowEnd(component, curOffset);
                if(rowEnd == -1) {
                    break;
                } else {
                    curOffset = rowEnd + 1;
                }
            } catch (BadLocationException e) {
                LOG.warning("Invalid location: " + curOffset);
                break;
            }
        }
        return res;
    }

    private int getYPosition(int offset) throws BadLocationException {
        Rectangle rect = component.modelToView(offset);
        return rect.y + rect.height / 2;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        documentChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        documentChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        documentChanged();
    }
}
