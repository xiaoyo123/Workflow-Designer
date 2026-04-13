package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import element.object.isBasicObject;

public final class AppearanceEditorDialog {
    private AppearanceEditorDialog() {
    }

    public static boolean show(Component parent, isBasicObject editable) {
        JTextField labelField = new JTextField(editable.getLabelName(), 16);
        ColorOption[] options = new ColorOption[] {
            new ColorOption("Light Gray", new Color(240, 240, 240)),
            new ColorOption("White", Color.WHITE),
            new ColorOption("Red", new Color(255, 182, 182)),
            new ColorOption("Orange", new Color(255, 214, 153)),
            new ColorOption("Yellow", new Color(255, 245, 157)),
            new ColorOption("Green", new Color(179, 229, 180)),
            new ColorOption("Blue", new Color(187, 222, 251)),
            new ColorOption("Purple", new Color(225, 190, 231))
        };

        JComboBox<ColorOption> colorBox = new JComboBox<>(options);
        int initialIndex = 0;
        for (int i = 0; i < options.length; i++) {
            if (options[i].color.equals(editable.getFillColor())) {
                initialIndex = i;
                break;
            }
        }
        colorBox.setSelectedIndex(initialIndex);

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.add(new JLabel("Label:"));
        panel.add(labelField);
        panel.add(new JLabel("Color:"));
        panel.add(colorBox);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Label", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return false;
        }

        editable.setLabelName(labelField.getText());
        ColorOption selectedColorOption = (ColorOption) colorBox.getSelectedItem();
        if (selectedColorOption != null) {
            editable.setFillColor(selectedColorOption.color);
        }
        return true;
    }

    private static class ColorOption {
        private final String name;
        private final Color color;

        private ColorOption(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
