package kianxali.gui.model.imagefile;

import javax.swing.text.PlainDocument;

import kianxali.image.ImageFile;

public class ImageDocument extends PlainDocument {
    private static final long serialVersionUID = 1L;
    private ImageFile image;

    public void setImageFile(ImageFile image) {
        this.image = image;
    }

    public ImageFile getImage() {
        return image;
    }
}
