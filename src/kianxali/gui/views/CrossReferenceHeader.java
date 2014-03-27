package kianxali.gui.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    private final Map<Line2D, LineEntry> visibleLines;
    private final Controller controller;
    private final JTextComponent component;
    private final Stroke thickStroke = new BasicStroke(3.5f);
    private final Stroke thinStroke = new BasicStroke(1.5f);
    private int lastHeight;
    private ImageDocument imageDoc;
    private boolean isHighlighting;

    private class LineEntry implements Comparable<LineEntry> {
        long fromAddr, toAddr;
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

    public CrossReferenceHeader(final Controller controller, final JTextComponent component) {
        this.visibleLines = new HashMap<>();
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

        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) { }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                Line2D line = findLine(p);
                if(line != null) {
                    isHighlighting = true;
                    repaint();
                } else if(isHighlighting) {
                    isHighlighting = false;
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                Line2D line = findLine(p);
                if(line != null) {
                    LineEntry entry = visibleLines.get(line);
                    if(entry != null) {
                        controller.onGotoRequest(entry.fromAddr);
                    }
                }
            }
        });
    }

    private Line2D findLine(Point p) {
        for(Line2D line : visibleLines.keySet()) {
            if(line.intersects(new Rectangle2D.Float(p.x - 1, p.y - 1, 2, 2))) {
                return line;
            }
        }
        return null;
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
                } catch(BadLocationException e) {
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
        visibleLines.clear();
        if(imageDoc == null) {
            return;
        }

        // get visible addresses
        Rectangle clip = g.getClipBounds();
        int startOffset = component.viewToModel(new Point(0, clip.y));
        int endOffset = component.viewToModel(new Point(0, clip.y + clip.height));
        g2.setColor(Color.GRAY);
        g2.fillRect(clip.x, clip.y, clip.width, clip.height);
        NavigableSet<LineEntry> lineEntries = buildReferences(startOffset, endOffset);
        shiftReferences(lineEntries);
        for(LineEntry entry : lineEntries) {
            int x = clip.width - 8 - entry.shiftX * 4;
            visibleLines.put(new Line2D.Float(x, entry.fromY, clip.width, entry.fromY), entry);
            visibleLines.put(new Line2D.Float(x, entry.fromY, x, entry.toY), entry);
            visibleLines.put(new Line2D.Float(x, entry.toY, clip.width, entry.toY), entry);
            visibleLines.put(new Line2D.Float(clip.width - 4, entry.toY - 4, clip.width + 4, entry.toY), entry);
            visibleLines.put(new Line2D.Float(clip.width - 4, entry.toY + 4, clip.width + 4, entry.toY), entry);
        }

        // check which lines must be highlighted
        LineEntry highlight = null;
        Point mouse = getMousePosition();
        if(mouse != null) {
            Line2D line = findLine(mouse);
            if(line != null) {
                highlight = visibleLines.get(line);
            }
        }
        for(Line2D line : visibleLines.keySet()) {
            LineEntry entry = visibleLines.get(line);
            if(highlight == entry) {
                g2.setStroke(thickStroke);
            } else {
                g2.setStroke(thinStroke);
            }
            g2.setColor(DISTANT_COLORS[entry.shiftX % DISTANT_COLORS.length]);
            g2.draw(line);
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
                            if(!controller.getImageFile().isCodeAddress(branchAddr)) {
                                continue;
                            }
                            int toY = getYPosition(imageDoc.getOffsetForAddress(branchAddr));
                            LineEntry line = new LineEntry(thisY, toY);
                            line.fromAddr = memAddr;
                            line.toAddr = branchAddr;
                            res.add(line);
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
                                LineEntry line = new LineEntry(fromY, thisY);
                                line.fromAddr = fromEntry.getAddress();
                                line.toAddr = memAddr;
                                res.add(line);
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
            } catch(BadLocationException e) {
                LOG.warning("Invalid location: " + curOffset);
                break;
            }
        }

        // filter double entries
        for(Iterator<LineEntry> it = res.iterator(); it.hasNext();) {
            LineEntry entry = it.next();
            boolean remove = false;
            for(LineEntry otherEntry : res.tailSet(entry, false)) {
                if(otherEntry.fromAddr == entry.fromAddr && otherEntry.toAddr == entry.toAddr) {
                    remove = true;
                    break;
                }
            }
            if(remove) {
                it.remove();
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
