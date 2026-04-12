package window;
import javax.swing.*;

import canvas.Labelable;
import canvas.Element;
import mode.Mode;
import mode.ShapeFactory;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class Window extends JFrame {
    private static final String[] TOOL_NAMES = {"Select", "Association", "Generalization", "Composition", "Rect", "Oval"};
    protected final Canvas canvas;
    private final Sidebar sidebar;
    private final Map<String, Mode> modes = new LinkedHashMap<>();
    private final Map<String, ShapeFactory> shapeFactories = new LinkedHashMap<>();
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

        modes.putAll(EditorConfiguration.createModes(canvas));
        shapeFactories.putAll(EditorConfiguration.createShapeFactories());

        sidebar = new Sidebar(TOOL_NAMES, shapeFactories.keySet(), new Sidebar.Listener() {
            @Override
            public void onModeSelected(String modeName) {
                setMode(modeName);
            }

            @Override
            public void onShapePressed(String shapeType) {
                beginTemporaryCreate(shapeType);
            }

            @Override
            public void onShapeReleased(String shapeType) {
                if (temporaryCreateActive) {
                    createShapeIfReleasedOnCanvas(shapeType);
                    endTemporaryCreate();
                }
            }
        });
        add(sidebar, BorderLayout.WEST);

        setJMenuBar(new Topbar(
            this::dispose,
            canvas::groupSelectedElements,
            canvas::ungroupSelectedElement,
            this::setLabelForSelection
        ));

        setMode("Select");
        setVisible(true);
        canvas.requestFocus();
    }

    private void setMode(String name) {
        if (temporaryCreateActive) {
            return;
        }

        Mode mode = modes.get(name);
        if (mode != null) {
            canvas.setMode(mode);
            activeModeName = name;
            sidebar.updateButtonHighlight(name);
        }
    }

    private void beginTemporaryCreate(String shapeType) {
        previousModeName = activeModeName;
        temporaryCreateActive = true;
        temporaryShapeType = shapeType;
        sidebar.updateButtonHighlight(shapeType);
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
            sidebar.updateButtonHighlight(restoreMode);
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

    private void setLabelForSelection() {
        List<Element> selected = canvas.getSelectedElements();
        if (selected.size() != 1) {
            JOptionPane.showMessageDialog(this, "Please select exactly one basic object.", "Label", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Element selectedElement = selected.get(0);
        if (!(selectedElement instanceof Labelable editable)) {
            JOptionPane.showMessageDialog(this, "Only a basic object can be assigned label.", "Label", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (AppearanceEditorDialog.show(this, editable)) {
            canvas.repaint();
        }
    }
}