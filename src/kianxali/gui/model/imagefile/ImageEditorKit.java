package kianxali.gui.model.imagefile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.ViewFactory;

public class ImageEditorKit extends EditorKit {
    private static final Logger LOG = Logger.getLogger("kianxali.gui.model.imagefile");
    private static final long serialVersionUID = 1L;

    @Override
    public ViewFactory getViewFactory() {
       return new ImageViewFactory();
    }

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    @Override
    public Caret createCaret() {
        LOG.finest("createCaret");
        return null;
    }

    @Override
    public Document createDefaultDocument() {
        LOG.finest("createDefaultDocument");
        return new ImageDocument();
    }

    @Override
    public Action[] getActions() {
        LOG.finest("getActions");
        return null;
    }

    @Override
    public void read(InputStream arg0, Document arg1, int arg2) throws IOException, BadLocationException {
        LOG.finest("read1");
    }

    @Override
    public void read(Reader arg0, Document arg1, int arg2) throws IOException, BadLocationException {
        LOG.finest("read2");
    }

    @Override
    public void write(OutputStream arg0, Document arg1, int arg2, int arg3) throws IOException, BadLocationException {
        LOG.finest("write1");
    }

    @Override
    public void write(Writer arg0, Document arg1, int arg2, int arg3) throws IOException, BadLocationException {
        LOG.finest("write2");
    }
}

