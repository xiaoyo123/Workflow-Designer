package window;
import javax.swing.*;

import canvas.AppearanceEditable;
import canvas.Canvas;
import canvas.Element;
import canvas.Groupable;
import mode.Mode;
import mode.ShapeFactory;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class Window extends JFrame {
    private static final int TOOL_ICON_SIZE = 28;
    protected final Canvas canvas;
    private JPanel sideBar;
    private final Map<String, Mode> modes = new LinkedHashMap<>();
    private final Map<String, ShapeFactory> shapeFactories = new LinkedHashMap<>();
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

        modes.putAll(EditorConfiguration.createModes(canvas));
        shapeFactories.putAll(EditorConfiguration.createShapeFactories());
        
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

            if (shapeFactories.containsKey(name)) {
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
        setAppearanceItem.addActionListener(e -> setLabelForSelection());
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
        ShapeFactory shapeFactory = shapeFactories.get(shapeType);
        if (shapeFactory == null) {
            return;
        }

        Element newShape = shapeFactory.createAt(canvasPoint.x, canvasPoint.y, depth);

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

    private void setLabelForSelection() {
        List<Element> selected = canvas.getSelectedElements();
        if (selected.size() != 1) {
            JOptionPane.showMessageDialog(this, "Please select exactly one basic object.", "Label", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Element selectedElement = selected.get(0);
        if (selectedElement instanceof Groupable) {
            JOptionPane.showMessageDialog(this, "Cannot assign label to a group. Please select a single basic object.", "Label", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!(selectedElement instanceof AppearanceEditable editable)) {
            JOptionPane.showMessageDialog(this, "Only a basic object can be assigned label.", "Label", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (AppearanceEditorDialog.show(this, editable)) {
            canvas.repaint();
        }
    }
}