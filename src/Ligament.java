import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Класс связей между атомами, реализует графику, логику поведения, контекстное меню
 */
public class Ligament extends JComponent {
    public static ArrayList<Ligament> ligaments = new ArrayList<>();

    public Atom atom1, atom2;
    public Color color = new Color(255,255,255);
    public boolean cov;

    public static boolean replace_cov;
    public static boolean figI_for_new_lig = true;
    public static Atom atomI;

    private int x1, x2, y1, y2;
    private final JPopupMenu popup = new JPopupMenu();

    /**
     * Конструктор связи
     * @param atom1 первая фигура в связке
     * @param atom2 вторая фигура в связке
     * @param cov тип изображения связи. true - ковалентная, false - нековалентная
     */
    public Ligament(Atom atom1, Atom atom2, boolean cov) {
        this.atom1 = atom1;
        this.atom2 = atom2;
        this.cov = cov;
        refreshPaintField();

        JMenuItem menuItemChange = new JMenuItem("Изменить");
        menuItemChange.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                change_cov();
                repaint();
            }
        });
        popup.add(menuItemChange);

        JMenuItem menuItemDelete = new JMenuItem("Удалить");
        menuItemDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                delete();
            }
        });
        popup.add(menuItemDelete);

        ComponentAdapter figuresAdapter = new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                super.componentMoved(e);
                refreshPaintField();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
                delete();
            }
        };

        atom1.addComponentListener(figuresAdapter);
        atom2.addComponentListener(figuresAdapter);
        addMouseListener(new LigMouseAdapter());
        ligaments.add(this);
    }

    /**
     * Удаляет связь
     */
    public void delete() {
        Ligament.this.setVisible(false);
        ligaments.remove(this);
    }

    /**
     * Меняет ковалентность связи на противоположенную
     */
    private void change_cov() {
        cov = !cov;
        replace_cov = cov;
    }

    /**
     * Метод для запоминания первой фигуры при создании связи
     * @param atom Объект Atom
     */
    public static void setFigureI(Atom atom) {
        atomI = atom;
    }

    /**
     * Обновляет поле рисования для объекта по координатам связанных фигур
     */
    public void refreshPaintField() {
        int width, height;
        if(atom1.getLocation().x < atom2.getLocation().x) {
            width = atom2.width;
        } else {
            width = atom1.width;
        }
        if(atom1.getLocation().y < atom2.getLocation().y) {
            height = atom2.height;
        } else {
            height = atom1.height;
        }
        setSize(Math.abs(atom1.getLocation().x - atom2.getLocation().x) + width, Math.abs(atom1.getLocation().y - atom2.getLocation().y) + height);
        setLocation(Math.min(atom1.getLocation().x, atom2.getLocation().x), Math.min(atom1.getLocation().y, atom2.getLocation().y));
    }

    /**
     * Отрисовывает линию связи в зависимости от взаимного расположения фигур
     */
    private void drawLig(Graphics2D g) {
        if (atom1.getLocation().x < atom2.getLocation().x) {
            if(atom1.getLocation().y < atom2.getLocation().y) {
                x1 = atom1.width / 2;
                y1 = atom1.height / 2;
                x2 = getWidth() - atom2.width / 2;
                y2 = getHeight() - atom2.height / 2;
            } else {
                x1 = atom1.width / 2;
                y1 = getHeight() - atom1.height / 2;
                x2 = getWidth() - atom2.width / 2;
                y2 = atom2.height / 2;
            }
        } else {
            if(atom1.getLocation().y < atom2.getLocation().y) {
                x1 = getWidth() - atom1.width / 2;
                y1 = atom1.height / 2;
                x2 = atom2.width / 2;
                y2 = getHeight() - atom2.height / 2;
            } else {
                x1 = getWidth() - atom1.width / 2;
                y1 = getHeight() - atom1.height / 2;
                x2 = atom2.width / 2;
                y2 = atom2.height / 2;
            }
        }
        g.drawLine(x1, y1, x2, y2);
        g.setColor(color);
        g.fillOval(Math.min(x1, x2) + Math.abs(x2 - x1)/2 - 8, Math.min(y1, y2) + Math.abs(y2 - y1)/2 - 8, 16, 16);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(10.0f));
        drawLig(g2d);
        if (cov) {
            g2d.setColor(new Color(0, 0, 0));
            g2d.setStroke(new BasicStroke(4.0f));
            drawLig(g2d);
        }
    }

    /**
     *
     */
    private class LigMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            int clickedSize = 25;
            if(e.getButton() == MouseEvent.BUTTON3 //Если нажатие происходит по центру объекта в квадрате
                    && e.getX() < Math.min(x1, x2) + Math.abs(x2 - x1)/2 - 7 + clickedSize
                    && e.getX() > Math.min(x1, x2) + Math.abs(x2 - x1)/2 - 7 - clickedSize
                    && e.getY() < Math.min(y1, y2) + Math.abs(y2 - y1)/2 - 7 + clickedSize
                    && e.getY() > Math.min(y1, y2) + Math.abs(y2 - y1)/2 - 7 - clickedSize
                    ) {
                showPopup(e);
            } else {
                MouseListener[] ml = getParent().getMouseListeners();
                e.translatePoint(getX(), getY());
                ml[0].mousePressed(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            int clickedSize = 25;
            if(e.getButton() == MouseEvent.BUTTON3 //Если нажатие происходит по центру объекта в квадрате
                    && e.getX() < Math.min(x1, x2) + Math.abs(x2 - x1)/2 - 7 + clickedSize
                    && e.getX() > Math.min(x1, x2) + Math.abs(x2 - x1)/2 - 7 - clickedSize
                    && e.getY() < Math.min(y1, y2) + Math.abs(y2 - y1)/2 - 7 + clickedSize
                    && e.getY() > Math.min(y1, y2) + Math.abs(y2 - y1)/2 - 7 - clickedSize
            ) {
                showPopup(e);
            } else {
                MouseListener[] ml = getParent().getMouseListeners();
//                e.translatePoint(getX(), getY());
                ml[0].mouseReleased(e);
            }
        }

        /**
         * Показывает контекстное меню, если нажатие является тригером
         * @param e Событие мыши
         */
        private void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }
}