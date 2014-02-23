package kianxali.gui.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

public class StatusView extends JComponent {
    private static final long serialVersionUID = 1L;
    private static final int NUM_STRIPES = 500;
    private enum DecodeType { UNKNOWN, CODE, DATA };
    private final DecodeType[] decodeStatus;
    private long dataLength;
    private int cursorIndex;

    public StatusView() {
        this.decodeStatus = new DecodeType[NUM_STRIPES + 1];
        initNewData(0);

        setMinimumSize(new Dimension(0, 25));
        setPreferredSize(getMinimumSize());
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

    public void initNewData(long fileSize) {
        this.dataLength = fileSize;
        for(int i = 0; i < NUM_STRIPES; i++) {
            decodeStatus[i] = DecodeType.UNKNOWN;
        }
    }

    private void setType(int index, DecodeType type) {
        DecodeType old = decodeStatus[index];
        if(old != type) {
            decodeStatus[index] = type;
            repaint();
        }
    }

    public void onDiscoverCode(long offset, long length) {
        for(long o = offset; o < offset + length; o++) {
            int index = (int) (o * NUM_STRIPES / dataLength);
            setType(index, DecodeType.CODE);
        }
    }

    public void onDiscoverData(long offset, long length) {
        for(long o = offset; o < offset + length; o++) {
            int index = (int) (o * NUM_STRIPES / dataLength);
            setType(index, DecodeType.DATA);
        }
    }

    public void setCursorAddress(long offset) {
        int index = (int) (offset * NUM_STRIPES / dataLength);
        cursorIndex = index;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int stripeWidth = (int) Math.max(1, Math.floor((double) getWidth() / NUM_STRIPES));
        int stripeHeight = getHeight();

        int x = 0;
        for(int i = 0; i < NUM_STRIPES; i++) {
            switch(decodeStatus[i]) {
            case CODE:  g.setColor(Color.blue); break;
            case DATA:  g.setColor(Color.yellow); break;
            default:    g.setColor(Color.black);
            }
            g.fillRect(x, 0, stripeWidth, stripeHeight);
            if(i == cursorIndex) {
                g.setColor(Color.green);
                g.fillRect(x, 0, 2, stripeHeight);
            }
            x += stripeWidth;
        }
    }
}
