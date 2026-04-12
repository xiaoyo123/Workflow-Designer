import javax.swing.SwingUtilities;

import window.Window;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Window::new);
    }
}
