import javax.swing.SwingUtilities;
import view.Window;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Window::new);
    }
}
