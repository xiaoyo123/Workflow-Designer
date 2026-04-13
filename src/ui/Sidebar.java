package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Sidebar extends JPanel {
    public interface Listener {
        void onModeSelected(String modeName);
        void onShapePressed(String shapeType);
        void onShapeReleased(String shapeType);
    }

    private static final int TOOL_ICON_SIZE = 28;
    private final Map<String, JButton> toolButtons = new LinkedHashMap<>();
    private final Listener listener;

    public Sidebar(String[] buttonNames, Set<String> shapeTools, Listener listener) {
        super(new GridLayout(6, 1, 6, 6));
        this.listener = listener;

        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(new Color(245, 245, 245));
        setPreferredSize(new Dimension(108, 0));

        for (String name : buttonNames) {
            JButton btn = createToolButton(name);
            if (shapeTools.contains(name)) {
                attachShapeToolEvents(btn, name);
            } else {
                btn.addActionListener(e -> listener.onModeSelected(name));
            }

            toolButtons.put(name, btn);
            add(btn);
        }
    }

    public void updateButtonHighlight(String activeName) {
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

    private JButton createToolButton(String name) {
        JButton btn = new JButton(name, loadIcon(name));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setIconTextGap(3);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 12f));
        btn.setMargin(new Insets(4, 4, 4, 4));
        btn.setPreferredSize(new Dimension(88, 62));
        return btn;
    }

    private void attachShapeToolEvents(JButton btn, String shapeType) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    listener.onShapePressed(shapeType);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    listener.onShapeReleased(shapeType);
                }
            }
        });
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
}
