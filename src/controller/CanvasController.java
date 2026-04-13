package controller;

import element.Composite;
import element.Element;
import element.Port;
import element.link.LinkType;
import element.link.BasicLink;
import element.object.BasicObject;
import element.object.ObjectType;
import element.object.Rect;
import element.object.isBasicObject;
import element.object.Oval;
import ui.Canvas;

import java.awt.Color;
import java.util.*;

public class CanvasController {
    private final List<Element> elements         = new ArrayList<>();
    private final List<Element> selectedElements = new ArrayList<>();
    private Canvas canvas;

    public void setCanvas(Canvas canvas) { this.canvas = canvas; }
    public Canvas getCanvas()            { return canvas; }

    // ── Use Case A ──

    public void createObject(ObjectType type, int x, int y) {
        int depth = getNextDepth();
        Element e = switch (type) {
            case RECT -> new Rect(x, y, depth);
            case OVAL -> new Oval(x, y, depth);
        };
        elements.add(e);
        repaint();
    }

    // ── Use Case B ──

    public void createLink(Port startPort, Port endPort, LinkType type) {
        if (startPort == null || endPort == null) return;
        if (startPort.getOwner() == endPort.getOwner()) return;
        int depth = getFrontDepth() - 1;
        BasicLink link = new BasicLink(startPort, endPort, type, depth) {};
        elements.add(link);
        repaint();
    }

    public Port getPortAt(int x, int y) {
        return elements.stream()
            .filter(e -> e instanceof isBasicObject)
            .map(e -> (isBasicObject) e)
            .map(c -> c.getPortAt(x, y))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    // ── Use Case C ──

    public void selectAt(int x, int y) {
        clearSelection();
        getTopElementAt(x, y).ifPresent(e -> {
            e.setSelected(true);
            selectedElements.add(e);
            bringToFront(e);
        });
        repaint();
    }

    public void areaSelect(int x1, int y1, int x2, int y2) {
        clearSelection();
        int left = Math.min(x1,x2), top  = Math.min(y1,y2);
        int right= Math.max(x1,x2), bottom = Math.max(y1,y2);
        elements.stream()
            .filter(Element::isSelectable)
            .filter(e -> e.isContainedIn(left, top, right, bottom))
            .forEach(e -> { e.setSelected(true); selectedElements.add(e); });
        repaint();
    }

    public void clearSelection() {
        selectedElements.forEach(e -> e.setSelected(false));
        selectedElements.clear();
        repaint();
    }

    // ── Use Case D ──

    public void group() {
        if (selectedElements.size() < 2) return;
        int depth = selectedElements.stream()
                        .mapToInt(Element::getDepth).min().orElse(0);
        Composite composite = new Composite(new ArrayList<>(selectedElements), depth);
        elements.removeAll(selectedElements);
        elements.add(composite);
        clearSelection();
        composite.setSelected(true);
        selectedElements.add(composite);
        repaint();
    }

    public void ungroup() {
        if (selectedElements.size() != 1) return;
        Element target = selectedElements.get(0);
        if (!target.isComposite()) return;
        Composite composite = (Composite) target;
        List<Element> members = new ArrayList<>(composite.getMembers());
        elements.remove(composite);
        elements.addAll(members);
        clearSelection();
        members.forEach(e -> { e.setSelected(true); selectedElements.add(e); });
        repaint();
    }

    // ── Use Case E ──

    public void moveSelected(int dx, int dy) {
        Set<Element> toMove = new LinkedHashSet<>();
        selectedElements.forEach(e -> e.collectMovedElements(toMove));
        toMove.forEach(e -> e.move(dx, dy));
        repaint();
    }

    // ── Use Case G ──

    public void updateLabel(Element element, String name, Color color) {
        if (!(element instanceof isBasicObject e)) return;
        e.setLabelName(name);
        e.setFillColor(color);
        repaint();
    }

    // ── 工具方法 ──

    public Optional<Element> getTopElementAt(int x, int y) {
        return elements.stream()
            .filter(Element::isSelectable)
            .filter(e -> e.contains(x, y))
            .min(Comparator.comparingInt(Element::getDepth));
    }

    public void bringToFront(Element element) {
        if (element == null) return;
        element.setDepth(getFrontDepth() - 1);
        repaint();
    }

    private int getFrontDepth() {
        return elements.stream().mapToInt(Element::getDepth).min().orElse(0);
    }

    private int getNextDepth() {
        return elements.stream().mapToInt(Element::getDepth).max().orElse(0) + 1;
    }

    private void repaint() { if (canvas != null) canvas.repaint(); }

    // ── 預覽方法 ──

    public void setPreviewRect(int x1, int y1, int x2, int y2) {
        if (canvas != null) canvas.setPreviewRect(x1, y1, x2, y2);
    }
    public void clearPreviewRect() {
        if (canvas != null) canvas.clearPreviewRect();
    }
    public void setPreviewLink(int x1, int y1, int x2, int y2) {
        if (canvas != null) canvas.setPreviewLink(x1, y1, x2, y2);
    }
    public void clearPreviewLink() {
        if (canvas != null) canvas.clearPreviewLink();
    }

    public List<Element> getElements() {
        return Collections.unmodifiableList(elements);
    }
    public List<Element> getSelectedElements() {
        return Collections.unmodifiableList(selectedElements);
    }
}