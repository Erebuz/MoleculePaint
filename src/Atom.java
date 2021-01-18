import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Класс атомов, реализует графику, контекстное меню, перемещение.
 */
public class Atom extends JComponent {
    public static ArrayList<Atom> atoms = new ArrayList<>();
    public static boolean grid_enable = false;
    public static int grid_size = 5;

    private static Color repeat_color = new Color(0, 100, 0);
    private static String repeat_name = "";
    private static int repeat_size = 50;

    public String name;
    public int width, height;
    public Color color;

    private boolean delete_if_cancel_change = false;
    private Point mouseCoordinate = new Point();
    private JPopupMenu popup;
    private static String number_figure = "I";

    /**
     * Минимальный конструктор
     * @param parent Компонент-родитель, используется для подключения слушателся изменения размера окна
     */
    public Atom(JComponent parent) {
        name = repeat_name;
        width = repeat_size;
        height = repeat_size;
        color = repeat_color;

        setSize(width, height);
        addMouseListener(new AtomMouseAdapter());
        final AtomMouseMotionAdapter m = new AtomMouseMotionAdapter();
        addMouseMotionListener(m);
        ComponentAdapter frame = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                int x = getX(), y = getY();
                if(x > getParent().getWidth() - Atom.this.width) x = getParent().getWidth() - Atom.this.width;
                if(x < 0) x = 0;
                if(y > getParent().getHeight() - Atom.this.height) y = getParent().getHeight() - Atom.this.height;
                if(y < 0) y = 0;
                setLocation(x, y);
            }
        };
        parent.addComponentListener(frame);
        atoms.add(this);
    }

    /**
     * Конструктор с вызовом окна запроса параметров
     * @param b true - с окном запроса, false - вызов конструктора без параметров
     */
    public Atom(JComponent parent, boolean b) {
        this(parent);
        if(b) {
            new AtomChangeDialog();
        }
    }

    /**
     * Конструктор для загрузки из файла со всеми параметрами
     * @param name Имя атома
     * @param width Ширина
     * @param height Высота
     * @param x Координаты по x
     * @param y Координаты по y
     * @param color Цвет заливки
     */
    public Atom(JComponent parent, String name, int width, int height, int x, int y, Color color) {
        this(parent);
        this.name = name;
        this.width = width;
        this.height = height;
        this.color = color;
        setSize(width, height);
        setLocation(x, y);
    }

    /**
     * Удаляет данную фигуру, в том числе из списка объектов
     */
    public void delete() {
        Atom.this.setVisible(false);
        if(this.equals(Ligament.atomI)) {
            Ligament.atomI = null;
            Ligament.figI_for_new_lig = true;
            number_figure = "I";
        }
        atoms.remove(this);
    }

    /**
     * Функция, содержащая графику фигуры. Рекомендуется использовать width и height для создания объекта, занимающего все поле фигуры.
     * @param g Графический контекст
     */
    public void drawFigure(Graphics g) {
        g.setColor(color);
        g.fillOval(0, 0, width, height);
        g.setColor(Color.white);
        g.drawString(name, width/2 - g.getFontMetrics().stringWidth(name)/2, height/2 + 4);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        drawFigure(g2d);
    }

    public boolean isDelete_if_cancel_change() {
        return delete_if_cancel_change;
    }

    /**
     * Реализует нажатия мыши: вызов контекстного меню и сохранения переменных положения мыши для перемещения объекта
     */
    private class AtomMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            if (e.getButton() == MouseEvent.BUTTON1) {
                mouseCoordinate = e.getPoint();
            }
            if(e.getButton() == MouseEvent.BUTTON3) {
                popup = new AtomPopupMenu();
                showPopup(e);
            }
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            if(e.getButton() == MouseEvent.BUTTON3) {
                popup = new AtomPopupMenu();
                showPopup(e);
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

    /**
     * Реализует перемещение фигуры захватом мыши
     */
    private class AtomMouseMotionAdapter extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            int grid;
            if(grid_enable) grid = Atom.grid_size;
            else grid = 1;
            setLocation(dragForX(e, grid), dragForY(e, grid));
        }

        /**
         * Функция логики изменения координаты x при перемещении объекта перетягиванием
         * @param e Событие мыши
         * @param grid Размер сетки привязки (1 - если сетка не нужна)
         * @return Координата x
         */
        private int dragForX(MouseEvent e, int grid) {
            int shift;
            if (grid_enable) shift = width/grid * grid - width/2;
            else shift = 0;
            int temp = (e.getX() - mouseCoordinate.x + getLocation().x)/grid*grid + shift;
            if(temp > getParent().getWidth() - Atom.this.width) temp = getParent().getWidth() - Atom.this.width;
            if(temp < 0) temp = 0;
            return temp;
        }

        /**
         * Функция логики изменения координаты y при перемещении объекта перетягиванием
         * @param e Событие мыши
         * @param grid Размер сетки привязки (1 - если сетка не нужна)
         * @return Координата y
         */
        private int dragForY(MouseEvent e, int grid) {
            int shift;
            if (grid_enable) shift = height/grid * grid - height/2;
            else shift = 0;
            int temp = (e.getY() - mouseCoordinate.y + getLocation().y)/grid*grid + shift;
            if(temp > getParent().getHeight() - Atom.this.height) temp = getParent().getHeight() - Atom.this.height;
            if(temp <= 0) temp = 0;
            return temp;
        }
    }

    /**
     * Реализует окно изменения фигуры
     */
    public class AtomChangeDialog extends JDialog {

        int x = MouseInfo.getPointerInfo().getLocation().x - 50;
        int y = MouseInfo.getPointerInfo().getLocation().y - 50;

        public AtomChangeDialog() {
            setLayout(new GridLayout(4, 2, 5, 12));
            setTitle("Изменение \"" + name + "\"");
            setModal(true);
            setBounds(x, y, 100, 100);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    delete_if_cancel_change = true;
                    dispose();
                }
            });

            add(new JLabel("  Имя:"));
            final JTextField name_figure = new JTextField(name);
            add(name_figure);

            add(new JLabel("  Размер изображения:"));
            final JTextField figure_size = new JTextField(String.valueOf(width));
            add(figure_size);

            JButton set_color = new JButton("Выбрать цвет");
            add(set_color);

            final JPanel color_priv = new JPanel();
            color_priv.setBackground(color);
            add(color_priv);

            JButton ok = new JButton("OK");
            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    name = name_figure.getText();
                    repeat_name = name_figure.getText();
                    repeat_size = Integer.parseInt(figure_size.getText());
                    width = repeat_size;
                    height = repeat_size;
                    Atom.this.setSize(repeat_size, repeat_size);
                    Atom.this.repaint();
                    dispose();
                }
            });
            add(ok);

            JButton close = new JButton("Отмена");
            close.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    delete_if_cancel_change = true;
                    dispose();
                }
            });
            add(close);
            set_color.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    color = JColorChooser.showDialog(Atom.this, "Выберите цвет", color);
                    repeat_color = color;
                    color_priv.setBackground(color);
                }
            });

            setResizable(false);
            pack();
            setVisible(true);
        }
    }

    /**
     * Реализует контекстное меню
     */
    public class AtomPopupMenu extends JPopupMenu {
        public AtomPopupMenu(){
            JMenuItem menuItemChange = new JMenuItem("Изменить...");
            menuItemChange.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new AtomChangeDialog();
                    repaint();
                }
            });
            this.add(menuItemChange);

            JMenuItem menuItemDelete = new JMenuItem("Удалить");
            menuItemDelete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    delete();
                }
            });
            this.add(menuItemDelete);
            this.addSeparator();

            String menuName = "Новая связь: " + number_figure;
            JMenuItem menuNewLig = new JMenuItem(menuName);
            menuNewLig.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(Ligament.figI_for_new_lig) {
                        Ligament.setFigureI(Atom.this);
                        Ligament.figI_for_new_lig = false;
                        number_figure = "II";
                    } else {
                        Ligament temp = new Ligament(Ligament.atomI, Atom.this, Ligament.replace_cov);
                        temp.refreshPaintField();
                        Atom.this.getParent().add(temp, -1);
                        Atom.this.getParent().repaint();
                        Ligament.atomI = null;
                        Ligament.figI_for_new_lig = true;
                        number_figure = "I";
                    }
                }
            });
            this.add(menuNewLig);
            if(!Ligament.figI_for_new_lig) {
                JMenuItem cancel_new_lig = new JMenuItem("Отменить создание связи");
                cancel_new_lig.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Ligament.atomI = null;
                        Ligament.figI_for_new_lig = true;
                        number_figure = "I";
                    }
                });
                this.add(cancel_new_lig);
            }
        }
    }
}