package kianxali.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import kianxali.decoder.Data;
import kianxali.gui.models.StringList;

public class StringListView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Controller controller;
    private final JList<Data> list;

    public StringListView(Controller controller) {
        this.controller = controller;
        this.list = new JList<Data>();

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

    public void setModel(StringList model) {
        list.setModel(model);
    }

    private void onDoubleClick(int index) {
        Data data = list.getModel().getElementAt(index);
        controller.onStringDoubleClicked(data);
    }

    private class Renderer implements ListCellRenderer<Data> {
        private final DefaultListCellRenderer delegate;

        public Renderer() {
            this.delegate = new DefaultListCellRenderer();
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Data> l, Data value, int index, boolean isSelected, boolean cellHasFocus) {
            String str = (String) value.getRawContent();
            return delegate.getListCellRendererComponent(l, str, index, isSelected, cellHasFocus);
        }
    }
}
