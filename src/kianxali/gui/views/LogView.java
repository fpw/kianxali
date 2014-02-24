package kianxali.gui.views;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import kianxali.gui.Controller;
import kianxali.util.LogFormatter;

public class LogView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTextPane logPane;
    private final MutableAttributeSet logStyle;

    public LogView(Controller controller) {
        setLayout(new BorderLayout());

        this.logStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(logStyle, Font.MONOSPACED);

        this.logPane = new JTextPane();
        logPane.setEditable(false);
        add(new JScrollPane(logPane), BorderLayout.CENTER);
    }

    public Handler getLogHandler() {
        Handler res = new Handler() {
            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }

            @Override
            public void publish(LogRecord record) {
                if(record.getLevel().intValue() < getLevel().intValue()) {
                    return;
                }
                final String msg = getFormatter().format(record);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        addLine(msg);
                    }
                });
            }
        };
        res.setFormatter(new LogFormatter());
        res.setLevel(Level.INFO);
        return res;
    }

    public void addLine(String line) {
        try {
            Document doc = logPane.getDocument();
            doc.insertString(doc.getLength(), line, logStyle);
        } catch (BadLocationException e) {
            // can't use Logger here because this would call addLine again
            System.err.println("Can't add text to log: " + e.getMessage());
        }
    }

    public void clear() {
        try {
            Document doc = logPane.getDocument();
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
            // don't care
        }
    }
}
