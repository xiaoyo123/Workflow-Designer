package window;

import java.util.LinkedHashMap;
import java.util.Map;

import canvas.Canvas;
import mode.LinkMode;
import mode.Mode;
import mode.SelectMode;
import mode.ShapeFactory;
import canvas.link.Association;
import canvas.link.Composition;
import canvas.link.Generalization;
import canvas.object.Oval;
import canvas.object.Rect;

public final class EditorConfiguration {
    private EditorConfiguration() {
    }

    public static Map<String, Mode> createModes(Canvas canvas) {
        Map<String, Mode> modes = new LinkedHashMap<>();
        modes.put("Select", new SelectMode(canvas));
        modes.put("Association", new LinkMode(canvas, Association::new));
        modes.put("Generalization", new LinkMode(canvas, Generalization::new));
        modes.put("Composition", new LinkMode(canvas, Composition::new));
        return modes;
    }

    public static Map<String, ShapeFactory> createShapeFactories() {
        Map<String, ShapeFactory> shapeFactories = new LinkedHashMap<>();
        shapeFactories.put("Rect", Rect::new);
        shapeFactories.put("Oval", Oval::new);
        return shapeFactories;
    }
}
