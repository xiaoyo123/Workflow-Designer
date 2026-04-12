package window;
import javax.swing.*;

import canvas.Canvas;
import canvas.Element;
import canvas.link.Association;
import canvas.link.Composition;
import canvas.link.Generalization;
import canvas.object.Composite;
import canvas.object.BasicObject;
import canvas.object.Oval;
import canvas.object.Rect;
import mode.LinkMode;
import mode.Mode;
import mode.SelectMode;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class Window extends JFrame {
    private static final int TOOL_ICON_SIZE = 28;
    protected final Canvas canvas;
    private JPanel sideBar;
    private final Map<String, Mode> modes = new LinkedHashMap<>();
    private final Map<String, JButton> toolButtons = new LinkedHashMap<>();
    private String activeModeName = "Select";
    private String previousModeName = "Select";
    private boolean temporaryCreateActive = false;
    private String temporaryShapeType = null;

    public Window() {
        setTitle("Oops UML Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLayout(new BorderLayout());

        canvas = new Canvas();
        canvas.setFocusable(true);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                canvas.requestFocus();
            }
        });
        add(canvas, BorderLayout.CENTER);

        setupSideBar();
        setupMenuBar();

        setMode("Select");
        setVisible(true);
        canvas.requestFocus();
    }

    private void setupSideBar() {
        sideBar = new JPanel(new GridLayout(6, 1, 6, 6));
        sideBar.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        sideBar.setBackground(new Color(245, 245, 245));

        String[] btnNames = {"Select", "Association", "Generalization", "Composition", "Rect", "Oval"};

        modes.put("Select", new SelectMode(canvas));
        modes.put("Association", new LinkMode(canvas, Association::new));
        modes.put("Generalization", new LinkMode(canvas, Generalization::new));
        modes.put("Composition", new LinkMode(canvas, Composition::new));
        
        for (String name : btnNames) {
            JButton btn = new JButton(name, loadIcon(name));
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setHorizontalTextPosition(SwingConstants.CENTER);
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btn.setIconTextGap(3);
            btn.setFocusPainted(false);
            btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 12f));
            btn.setMargin(new Insets(4, 4, 4, 4));
            btn.setPreferredSize(new Dimension(88, 62));

            if ("Rect".equals(name) || "Oval".equals(name)) {
                String shapeType = name;
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            beginTemporaryCreate(shapeType);
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e) && temporaryCreateActive) {
                            createShapeIfReleasedOnCanvas(shapeType);
                            endTemporaryCreate();
                        }
                    }
                });
            } else {
                btn.addActionListener(e -> setMode(name));
            }

            toolButtons.put(name, btn);
            sideBar.add(btn);
        }

        sideBar.setPreferredSize(new Dimension(108, 0));
        add(sideBar, BorderLayout.WEST);

        updateButtonHighlight("Select");
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> dispose());
        fileMenu.add(exitItem);

        JMenuItem groupItem = new JMenuItem("Group");
        groupItem.addActionListener(e -> canvas.groupSelectedElements());
        JMenuItem ungroupItem = new JMenuItem("Ungroup");
        ungroupItem.addActionListener(e -> canvas.ungroupSelectedElement());
        JMenuItem setAppearanceItem = new JMenuItem("Label");
        setAppearanceItem.addActionListener(e -> setLabelAndColorForSelection());
        editMenu.add(groupItem);
        editMenu.add(ungroupItem);
        editMenu.add(setAppearanceItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        setJMenuBar(menuBar);
    }

    private void setMode(String name) {
        if (temporaryCreateActive) {
            return;
        }

        Mode mode = modes.get(name);
        if (mode != null) {
            canvas.setMode(mode);
            activeModeName = name;
            updateButtonHighlight(name);
        }
    }

    private void beginTemporaryCreate(String shapeType) {
        previousModeName = activeModeName;
        temporaryCreateActive = true;
        temporaryShapeType = shapeType;
        updateButtonHighlight(shapeType);
    }

    private void endTemporaryCreate() {
        if (!temporaryCreateActive) {
            return;
        }

        temporaryCreateActive = false;
        temporaryShapeType = null;
        String restoreMode = modes.containsKey(previousModeName) ? previousModeName : "Select";
        Mode mode = modes.get(restoreMode);
        if (mode != null) {
            canvas.setMode(mode);
            activeModeName = restoreMode;
            updateButtonHighlight(restoreMode);
        }
    }

    private void createShapeIfReleasedOnCanvas(String shapeType) {
        if (temporaryShapeType == null || !temporaryShapeType.equals(shapeType)) {
            return;
        }

        Point mouseScreen = MouseInfo.getPointerInfo() != null ? MouseInfo.getPointerInfo().getLocation() : null;
        if (mouseScreen == null) {
            return;
        }

        Point canvasPoint = new Point(mouseScreen);
        SwingUtilities.convertPointFromScreen(canvasPoint, canvas);
        if (!canvas.contains(canvasPoint)) {
            return;
        }

        int depth = canvas.getFrontDepth() - 1;
        Element newShape = "Oval".equals(shapeType)
            ? new Oval(canvasPoint.x, canvasPoint.y, depth)
            : new Rect(canvasPoint.x, canvasPoint.y, depth);

        canvas.addElement(newShape);
    }

    private void updateButtonHighlight(String activeName) {
        Color defaultBg = UIManager.getColor("Button.background");
        Color defaultFg = UIManager.getColor("Button.foreground");
        if (defaultBg == null) {
            defaultBg = Color.LIGHT_GRAY;
        }
        if (defaultFg == null) {
            defaultFg = Color.BLACK;
        }

        for (Map.Entry<String, JButton> entry : toolButtons.entrySet()) {
            JButton btn = entry.getValue();
            boolean active = entry.getKey().equals(activeName);
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setBackground(active ? Color.BLACK : defaultBg);
            btn.setForeground(active ? Color.WHITE : defaultFg);
        }
    }

    private ImageIcon loadIcon(String name) {
        String fileName = name + ".png";

        java.net.URL resource = getClass().getResource("/img/" + fileName);
        Image image;

        if (resource != null) {
            image = new ImageIcon(resource).getImage();
        } else {
            image = new ImageIcon("src/img/" + fileName).getImage();
        }

        Image scaled = image.getScaledInstance(TOOL_ICON_SIZE, TOOL_ICON_SIZE, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void setLabelAndColorForSelection() {
        List<Element> selected = canvas.getSelectedElements();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one object first.", "Label", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<BasicObject> editableObjects = new ArrayList<>();
        boolean containsComposite = false;
        for (Element shape : selected) {
            if (shape instanceof BasicObject object) {
                editableObjects.add(object);
            } else if (shape instanceof Composite) {
                containsComposite = true;
            }
        }

        if (editableObjects.isEmpty()) {
            String message = containsComposite
                ? "Composite cannot be assigned label/color."
                : "Please select at least one object first.";
            JOptionPane.showMessageDialog(this, message, "Label", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (containsComposite) {
            JOptionPane.showMessageDialog(this, "Composite cannot be assigned label/color. Only basic objects will be updated.", "Label", JOptionPane.INFORMATION_MESSAGE);
        }

        BasicObject first = editableObjects.get(0);
        String initialLabel = resolveInitialLabel(first);
        Color initialColor = resolveInitialColor(first);

        JTextField labelField = new JTextField(initialLabel, 16);
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
            if (options[i].color.equals(initialColor)) {
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

        int result = JOptionPane.showConfirmDialog(this, panel, "Label", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String input = labelField.getText();
        ColorOption selectedColorOption = (ColorOption) colorBox.getSelectedItem();
        Color chosenColor = selectedColorOption != null ? selectedColorOption.color : new Color(240, 240, 240);

        for (BasicObject object : editableObjects) {
            object.setLabelName(input);
            object.setFillColor(chosenColor);
        }
        canvas.repaint();
    }

    private String resolveInitialLabel(Element shape) {
        if (shape instanceof BasicObject) {
            return ((BasicObject) shape).getLabelName();
        }

        if (shape instanceof Composite) {
            return "";
        }
        return "";
    }

    private Color resolveInitialColor(Element shape) {
        if (shape instanceof BasicObject) {
            return ((BasicObject) shape).getFillColor();
        }

        if (shape instanceof Composite) {
            return new Color(240, 240, 240);
        }
        return new Color(240, 240, 240);
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