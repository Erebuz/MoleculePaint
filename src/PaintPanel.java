import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Панель, реализующая область для рисования в приложении
 */
public class PaintPanel extends JPanel {

    private Image image;
    private final JPopupMenu popup = new JPopupMenu();
    private Point mouse_cord;

    /**
     * Конструктор с фоновой картинкой. Если картинки нет - черный фон
     * @param image_name Имя картинки в папке image
     */
    public PaintPanel(String image_name) {
        setLayout(null);
        setBorder(BorderFactory.createLineBorder(Color.black));

        String path_str = System.getProperty("user.dir");
        Path path = Paths.get(path_str);
        path = path.resolve(Paths.get("image", image_name)).normalize();
        try {
            this.image = ImageIO.read(new File(path.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JMenuItem menuNewAtom = new JMenuItem("Создать атом");
        menuNewAtom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Atom atom = new Atom(PaintPanel.this);
                atom.setLocation(mouse_cord.x - 10, mouse_cord.y - 10);
                add(atom, 0);
                atom.repaint();
            }
        });
        popup.add(menuNewAtom);
        JMenuItem menuNewAtomWithChange = new JMenuItem("Создать атом...");
        menuNewAtomWithChange.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Atom atom = new Atom(PaintPanel.this, true);
                atom.setLocation(mouse_cord.x - 10, mouse_cord.y - 10);
                add(atom, 0);
                if(atom.isDelete_if_cancel_change()) {
                    atom.delete();
                }
                atom.repaint();
            }
        });
        popup.add(menuNewAtomWithChange);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                mouse_cord = e.getPoint();
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                showPopup(e);
            }

            /**
             * Показывает контекстное меню, если нажатие является тригером
             * @param e Событие мыши
             */
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    @Override
    public void paintComponent (Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.white);
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

}
