package ui;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class Topbar extends JMenuBar {
    public Topbar(Runnable onExit, Runnable onGroup, Runnable onUngroup, Runnable onLabel) {
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> onExit.run());
        fileMenu.add(exitItem);

        JMenuItem groupItem = new JMenuItem("Group");
        groupItem.addActionListener(e -> onGroup.run());

        JMenuItem ungroupItem = new JMenuItem("Ungroup");
        ungroupItem.addActionListener(e -> onUngroup.run());

        JMenuItem labelItem = new JMenuItem("Label");
        labelItem.addActionListener(e -> onLabel.run());

        editMenu.add(groupItem);
        editMenu.add(ungroupItem);
        editMenu.add(labelItem);

        add(fileMenu);
        add(editMenu);
    }
}
