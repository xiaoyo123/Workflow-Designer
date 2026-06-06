package ui;

import javax.swing.*;
import controller.CanvasController;
import element.Element;
import element.link.LinkType;
import element.object.BasicObject;
import element.object.ObjectType;
import mode.*;
import java.awt.*;
import java.util.List;

public class Window extends JFrame {
    private record ToolDefinition(String name, LinkType linkType, ObjectType objectType) {
        boolean isShape() {
            return objectType != null;
        }
    }

    private static final ToolDefinition[] TOOLS = {
        new ToolDefinition("Select", null, null),
        new ToolDefinition("Association", LinkType.ASSOCIATION, null),
        new ToolDefinition("Generalization", LinkType.GENERALIZATION, null),
        new ToolDefinition("Composition", LinkType.COMPOSITION, null),
        new ToolDefinition("Rect", null, ObjectType.RECT),
        new ToolDefinition("Oval", null, ObjectType.OVAL)
    };

    private final CanvasController controller;
    private final Canvas canvas;
    private final Sidebar sidebar;
    private String activeModeName = "Select";

    public Window() {
        controller = new CanvasController();
        canvas     = new Canvas(controller);
        controller.setCanvas(canvas);

        setTitle("Oops UML Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLayout(new BorderLayout());

        sidebar = new Sidebar(toolNames(), 
            shapeToolNames(),
            new Sidebar.Listener() {
                @Override
                public void onModeSelected(String name) { switchMode(name); }
                @Override
                public void onShapePressed(String name) { 
                    ObjectType type = toolByName(name).objectType();
                    sidebar.updateButtonHighlight(name);
                    canvas.beginExternalCreate(type, () -> {
                        sidebar.updateButtonHighlight(activeModeName);
                    });
                }
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
        ToolDefinition tool = toolByName(name);

        Mode mode = tool.linkType() != null
            ? new LinkMode(controller, tool.linkType())
            : new SelectMode(controller);
        canvas.setMode(mode);
        activeModeName = name;
        sidebar.updateButtonHighlight(name);
    }

    private static String[] toolNames() {
        String[] names = new String[TOOLS.length];
        for (int i = 0; i < TOOLS.length; i++) {
            names[i] = TOOLS[i].name();
        }
        return names;
    }

    private static java.util.Set<String> shapeToolNames() {
        java.util.Set<String> names = new java.util.LinkedHashSet<>();
        for (ToolDefinition tool : TOOLS) {
            if (tool.isShape()) {
                names.add(tool.name());
            }
        }
        return names;
    }

    private static ToolDefinition toolByName(String name) {
        for (ToolDefinition tool : TOOLS) {
            if (tool.name().equals(name)) {
                return tool;
            }
        }
        return TOOLS[0];
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
        if (!(target instanceof BasicObject element)) {
            JOptionPane.showMessageDialog(this,
                "Only a basic object can be assigned label.",
                "Label", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (StyleDialog.show(this, element)) {
            canvas.repaint();
        }
    }
}
