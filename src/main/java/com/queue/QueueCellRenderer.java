package com.queue;

import javax.swing.*;
import java.awt.*;

public class QueueCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof IFileData) {
            IFileData fileData = (IFileData) value;
            setText(fileData.getFile().getName());
        }
        return this;
    }
}
