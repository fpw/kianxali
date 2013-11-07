package org.solhost.folko.dasm.xml;

import java.util.ArrayList;
import java.util.List;

public class Syntax {
    private final List<OperandDesc> operands;
    private String mnemonic;

    public Syntax() {
        this.operands = new ArrayList<>(4);
    }

    public void addOperand(OperandDesc opDesc) {
        operands.add(opDesc);
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("<syntax mnemonic=" + mnemonic);
        for(int i = 0; i < operands.size(); i++) {
            res.append(String.format(" op%d=%s", i + 1, operands.get(i).toString()));
        }
        res.append(">");
        return res.toString();
    }
}
