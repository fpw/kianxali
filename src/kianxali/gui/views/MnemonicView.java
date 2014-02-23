package kianxali.gui.views;

import java.awt.Shape;

import javax.swing.text.Element;
import javax.swing.text.LabelView;

import kianxali.decoder.Instruction;
import kianxali.gui.models.ImageDocument;

public class MnemonicView extends LabelView {
    private Instruction instruction;

    public MnemonicView(Element elem) {
        super(elem);
        Object inst = elem.getAttributes().getAttribute(ImageDocument.InstructionKey);
        if(inst instanceof Instruction) {
            instruction = (Instruction) inst;
        }
    }

    @Override
    public String getToolTipText(float x, float y, Shape allocation) {
        if(instruction != null) {
            return instruction.getDescription();
        } else {
            return null;
        }
    }
}
