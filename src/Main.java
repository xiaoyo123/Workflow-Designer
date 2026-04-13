import javax.swing.SwingUtilities;

import ui.Window;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Window::new);
    }
}
