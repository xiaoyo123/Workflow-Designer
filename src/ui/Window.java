package ui;

import javax.swing.*;
import controller.CanvasController;
import element.Element;
import element.link.LinkType;
import element.object.ObjectType;
import element.object.isBasicObject;
import mode.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.List;

public class Window extends JFrame {
    private static final String[] TOOL_NAMES = {
        "Select", "Association", "Generalization",
        "Composition", "Rect", "Oval"
    };

    private final CanvasController controller;
    private final Canvas canvas;
    private final Sidebar sidebar;
    private String activeModeName = "Select";
    private String previousModeName = "Select";
    private boolean temporaryCreateActive = false;
    private ObjectType temporaryShapeType;
    private final AWTEventListener temporaryCreateReleaseListener = event -> {
        if (!temporaryCreateActive || !(event instanceof MouseEvent mouseEvent)) {
            return;
        }
        if (mouseEvent.getID() != MouseEvent.MOUSE_RELEASED || mouseEvent.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        handleTemporaryCreateRelease(mouseEvent);
    };

    public Window() {
        controller = new CanvasController();
        canvas     = new Canvas(controller);
        controller.setCanvas(canvas);

        setTitle("Oops UML Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLayout(new BorderLayout());

        sidebar = new Sidebar(TOOL_NAMES, 
            java.util.Set.of("Rect", "Oval"),
            new Sidebar.Listener() {
                @Override
                public void onModeSelected(String name) { switchMode(name); }
                @Override
                public void onShapePressed(String name) { beginTemporaryCreate(name); }
                @Override
                public void onShapeReleased(String name) {}
            });

        add(sidebar, BorderLayout.WEST);
        add(canvas,  BorderLayout.CENTER);

        setJMenuBar(new Topbar(
            this::dispose,
            controller::group,
            controller::ungroup,
            this::setLabelForSelection
        ));

        switchMode("Select");
        setVisible(true);
        canvas.requestFocus();
    }

    private void switchMode(String name) {
        if (temporaryCreateActive) {
            return;
        }

        Mode mode = switch (name) {
            case "Select"         -> new SelectMode(controller);
            case "Association"    -> new LinkMode(controller, LinkType.ASSOCIATION);
            case "Generalization" -> new LinkMode(controller, LinkType.GENERALIZATION);
            case "Composition"    -> new LinkMode(controller, LinkType.COMPOSITION);
            case "Rect"           -> new CreateMode(controller, ObjectType.RECT);
            case "Oval"           -> new CreateMode(controller, ObjectType.OVAL);
            default               -> new SelectMode(controller);
        };
        canvas.setMode(mode);
        activeModeName = name;
        sidebar.updateButtonHighlight(name);
    }

    private void beginTemporaryCreate(String shapeName) {
        ObjectType targetType = parseShapeType(shapeName);
        if (targetType == null) {
            return;
        }

        if (temporaryCreateActive) {
            endTemporaryCreate();
        }

        previousModeName = activeModeName;
        temporaryCreateActive = true;
        temporaryShapeType = targetType;
        sidebar.updateButtonHighlight(shapeName);
        Toolkit.getDefaultToolkit().addAWTEventListener(
            temporaryCreateReleaseListener,
            AWTEvent.MOUSE_EVENT_MASK
        );
    }

    private void handleTemporaryCreateRelease(MouseEvent e) {
        if (!temporaryCreateActive || temporaryShapeType == null) {
            return;
        }

        Point canvasPoint = new Point(e.getLocationOnScreen());
        SwingUtilities.convertPointFromScreen(canvasPoint, canvas);
        if (canvas.contains(canvasPoint)) {
            controller.createElement(temporaryShapeType, canvasPoint.x, canvasPoint.y);
        }

        endTemporaryCreate();
    }

    private void endTemporaryCreate() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(temporaryCreateReleaseListener);
        temporaryCreateActive = false;
        temporaryShapeType = null;
        String restoreMode = previousModeName;
        switchMode(restoreMode);
    }

    private ObjectType parseShapeType(String shapeName) {
        return switch (shapeName) {
            case "Rect" -> ObjectType.RECT;
            case "Oval" -> ObjectType.OVAL;
            default -> null;
        };
    }

    private void setLabelForSelection() {
        List<Element> selected = controller.getSelectedElements();
        if (selected.size() != 1) {
            JOptionPane.showMessageDialog(this,
                "Please select exactly one basic object.",
                "Label", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Element target = selected.get(0);
        if (!(target instanceof isBasicObject element)) {
            JOptionPane.showMessageDialog(this,
                "Only a basic object can be assigned label.",
                "Label", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (AppearanceEditorDialog.show(this, element)) {
            canvas.repaint();
        }
    }
}