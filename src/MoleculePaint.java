import javax.swing.*;

/**
 * Установка LookAndFeel и запуск программы
 */
public class MoleculePaint {

    public static void main(String[] args) {
        String systemLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(systemLookAndFeelClassName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new MainFrame();
    }
}