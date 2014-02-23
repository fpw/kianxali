package kianxali.gui.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.JXList;

import kianxali.disassembler.Function;
import kianxali.gui.Controller;
import kianxali.gui.models.FunctionList;

public class FunctionListView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JXList list;
    private final Controller controller;

    public FunctionListView(Controller controller) {
        this.controller = controller;
        this.list = new JXList();
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    onDoubleClick(index);
                }
            }
        });
        list.setCellRenderer(new Renderer());

        setLayout(new BorderLayout());
        add(new JScrollPane(list), BorderLayout.CENTER);
    }

    public void setModel(FunctionList funListModel) {
        list.setModel(funListModel);
    }

    private void onDoubleClick(int index) {
        Function fun = (Function) list.getModel().getElementAt(index);
        controller.onFunctionDoubleClick(fun);
    }

    private class Renderer implements ListCellRenderer<Function> {
        private final DefaultListCellRenderer delegate;

        public Renderer() {
            this.delegate = new DefaultListCellRenderer();
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Function> l, Function value, int index, boolean isSelected, boolean cellHasFocus) {
            String str = value.getName();
            return delegate.getListCellRendererComponent(l, str, index, isSelected, cellHasFocus);
        }
    }
}

