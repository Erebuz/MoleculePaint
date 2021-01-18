import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

/**
 * Создание окна для приложения
 */
public class MainFrame extends JFrame {

    private final String help_txt = "Создание и изменение объектов в редакторе МолекуляторTM выполняется с помощью выпадающего меню по щелчку правой кнопкой мыши.\n" +
                              "При нажатии на пустое поле редактора, меню имеет два пункта:\n" +
                              "    - \"Новый атом\"    - создает копию последнего созданного атома.\n" +
                              "    - \"Новый атом...\" - вызвает окно создания атома с выбором имени, размера и цвета.\n" +
                              "\n" +
                              "При нажатии на атом меню имеет два раздела:\n" +
                              "    - Изменение или удаление этого атома. При удалении атома удаляются и все ассоциированные с ним связи.\n" +
                              "    - Создание связи между атомами.\n" +
                              "     Для создания связи выберете первый атом связи, нажам на соответствующий пункт выпадающего меню,\n" +
                              "     после чего выберете второй атом аналогичным образом, или отмените создание связи.\n" +
                              "     Между выбором атомов при создании связи допускаются другие действия.\n" +
                              "     Связь создается того же типа, что и последняя созданная.\n" +
                              "\n" +
                              "Для вызова меню связи, щелкните правой клавишей в центре связи (центр отмечен кружком). Меню связи содержит два пункта:\n" +
                              "    - \"Изменить\" - меняет тип связи (ковалентный или нековалентный).\n" +
                              "    - \"Удалить\" - удаляет связь.\n" +
                              "\n" +
                              "В главном меню, меню \"Окно\" имеет две функции:\n" +
                              "    - \"Очистить\" - очищает поле от всех объектов. ОСТОРОЖНО, РАБОТАЕТ С ТЕКУЩИМ ФАЙЛОМ.\n" +
                              "    - \"Сетка\" - вызывает окно для включения невидимой сетки заданой размерности для размещения фигур.\n";

    private final String about_txt = "Программа создана в качестве курсовой работы по дисциплине \"Кроссплатформенное программирование\"\n" +
                               "студентом группы ИБ-81вп Покладовым Д.\n" +
                               "декабрь 2020.\n";

    private File file;
    private final PaintPanel paintPanel;

    /**
     * Конструктор основного окна
     */
    public MainFrame() {
        super("Молекулятор");
        // Устанавливаем настройки основного окна
        int width = 800;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width / 2 - width / 2;
        int height = 600;
        int y = screenSize.height / 2 - height / 2;
        setBounds(x, y, width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu());
        menuBar.add(windowMenu());
        menuBar.add(referenceMenu());
        setJMenuBar(menuBar);

        // Добавляем область для рисования
        PaintPanel paintPanel = new PaintPanel("molecule.png");
        add(paintPanel);
        paintPanel.setBounds(0, 0, width, height);
        setVisible(true);
        this.paintPanel = paintPanel;
    }

    /**
     * Создание меню окна
     * @return JMenu Окно
     */
    private JMenu windowMenu() {
        JMenu jMenu = new JMenu("Окно");
        JMenuItem clear_menu = new JMenuItem("Очистить");
        clear_menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delete_all();
            }
        });
        JMenuItem grid_menu = new JMenuItem("Сетка...");
        grid_menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new GridDialog();
            }
        });

        jMenu.add(clear_menu);
        jMenu.add(grid_menu);
        return jMenu;
    }

    /**
     * Создание меню Справка
     * @return JMenu Справка
     */
    private JMenu referenceMenu() {
        JMenu jMenu = new JMenu("Справка");
        JMenuItem create = new JMenuItem("Помощь");
        create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MainFrame.this, help_txt);
            }
        });
        JMenuItem about = new JMenuItem("О программе");
        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MainFrame.this, about_txt);
            }
        });
        jMenu.add(create);
        jMenu.add(about);
        return jMenu;
    }

    /**
     * Создание меню Файл
     * @return JMenu Файл
     */
    private JMenu fileMenu() {
        JMenu jMenu = new JMenu("Файл");
        JMenuItem new_file = new JMenuItem("Новая схема");
        new_file.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                file = null;
                delete_all();
            }
        });
        JMenuItem open = new JMenuItem("Открыть...");
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open_dialog();
            }
        });
        JMenuItem save = new JMenuItem("Сохранить");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(file != null) {
                    saveFile(file);
                } else {
                    save_as_dialog();
                }
            }
        });
        JMenuItem save_as = new JMenuItem("Сохранить как...");
        save_as.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save_as_dialog();
            }
        });
        JMenuItem exit = new JMenuItem("Выход");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        jMenu.add(new_file);
        jMenu.add(open);
        jMenu.add(save);
        jMenu.add(save_as);
        jMenu.addSeparator();
        jMenu.add(exit);
        return jMenu;
    }

    /**
     * Функция, удаляющая все атомы и все связи с экрана
     */
    private void delete_all() {
        while (Atom.atoms.size() != 0) {
            Atom.atoms.get(0).delete();
        }
        while (Ligament.ligaments.size() != 0) {
            Ligament.ligaments.get(0).delete();
        }
    }

    /**
     * Создание окна сохранения файла
     */
    private void save_as_dialog(){
        JFileChooser file_open = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.xml", "xml");
        file_open.setFileFilter(filter);
        int chooser = file_open.showDialog(null, "Сохранить");
        if (chooser == JFileChooser.APPROVE_OPTION) {
            file = file_open.getSelectedFile();
            saveFile(file);
        }
    }

    /**
     * Функция сохранения файла
     * @param file Абсолютное имя файла
     */
    private void saveFile(File file) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            //root тег
            Element rootElement = doc.createElement("data");
            doc.appendChild(rootElement);

            //обертки для объектов фигур и связей
            Element figures = doc.createElement("figures");
            rootElement.appendChild(figures);
            Element ligaments = doc.createElement("ligaments");
            rootElement.appendChild(ligaments);

            //Создание элементов с атрибутами, хранящих данные объектов
            int i=0;
            for(Atom atom : Atom.atoms) {
                Element temp_fig = doc.createElement("figure");
                temp_fig.setAttribute("ID", String.valueOf(i++));
                temp_fig.setAttribute("name", atom.name);
                temp_fig.setAttribute("width", String.valueOf(atom.width));
                temp_fig.setAttribute("height", String.valueOf(atom.height));
                temp_fig.setAttribute("x", String.valueOf(atom.getLocation().x));
                temp_fig.setAttribute("y", String.valueOf(atom.getLocation().y));
                temp_fig.setAttribute("rgb", String.valueOf(atom.color.getRGB()));
                figures.appendChild(temp_fig);
            }
            for(Ligament ligament : Ligament.ligaments) {
                Element temp_lig = doc.createElement("ligament");
                temp_lig.setAttribute("figureI_ID", String.valueOf(Atom.atoms.indexOf(ligament.atom1)));
                temp_lig.setAttribute("figureII_ID", String.valueOf(Atom.atoms.indexOf(ligament.atom2)));
                temp_lig.setAttribute("covalence", String.valueOf(ligament.cov));
                ligaments.appendChild(temp_lig);
            }

            //Вывод в файл с редактированием
            StreamResult output = new StreamResult(file);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, output);
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создание окна загрузки файла
     */
    private void open_dialog() {
        JFileChooser file_open = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.xml", "xml");
        file_open.setFileFilter(filter);
        int chooser = file_open.showDialog(null, "Открыть");
        if (chooser == JFileChooser.APPROVE_OPTION) {
            delete_all();
            file = file_open.getSelectedFile();
            try {
                loadFile(file);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Функция загрузки файла
     * @param file Абсолютное имя файла
     */
    private void loadFile(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        NodeList figures = document.getDocumentElement().getElementsByTagName("figure");
        NodeList ligaments = document.getDocumentElement().getElementsByTagName("ligament");
        Atom temp_atom;
        Ligament temp_ligament;
        for(int i = 0; i < figures.getLength(); i++) {
            Node figure = figures.item(i);
            NamedNodeMap att = figure.getAttributes();
            String name = att.getNamedItem("name").getNodeValue();
            int width = Integer.parseInt(att.getNamedItem("width").getNodeValue());
            int height = Integer.parseInt(att.getNamedItem("height").getNodeValue());
            int x = Integer.parseInt(att.getNamedItem("x").getNodeValue());
            int y = Integer.parseInt(att.getNamedItem("y").getNodeValue());
            Color color = new Color(Integer.parseInt(att.getNamedItem("rgb").getNodeValue()));
            temp_atom = new Atom(paintPanel, name, width, height, x, y, color);
            paintPanel.add(temp_atom);
        }

        for(int i = 0; i < ligaments.getLength(); i++) {
            Node ligament = ligaments.item(i);
            NamedNodeMap att = ligament.getAttributes();
            int figureI_ID = Integer.parseInt(att.getNamedItem("figureI_ID").getNodeValue());
            int figureII_ID = Integer.parseInt(att.getNamedItem("figureII_ID").getNodeValue());
            boolean cov = att.getNamedItem("covalence").getNodeValue().equals("true");
            temp_ligament = new Ligament(Atom.atoms.get(figureI_ID), Atom.atoms.get(figureII_ID), cov);
            paintPanel.add(temp_ligament);
        }
        paintPanel.repaint();
    }

    /**
     * Окно включения и ввода параметров сетки для привязки положения фигур
     */
    private class GridDialog extends JDialog {
        private boolean en_grid;

        private GridDialog(){
            setLayout(new GridLayout(3, 2, 5, 12));
            setTitle("Сетка");
            setModal(true);
            int x = MouseInfo.getPointerInfo().getLocation().x - 50;
            int y = MouseInfo.getPointerInfo().getLocation().y - 50;
            setBounds(x, y, 100, 100);

            JCheckBox enable_grid = new JCheckBox("Включить сетку", en_grid);
            enable_grid.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    en_grid = !en_grid;
                }
            });
            add(enable_grid);
            add(new JLabel(""));
            add(new JLabel("Размер сетки"));
            final JTextField grid_size = new JTextField(String.valueOf(Atom.grid_size));
            add(grid_size);

            JButton ok = new JButton("OK");
            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Atom.grid_enable = en_grid;
                    Atom.grid_size = Integer.parseInt(grid_size.getText());
                    dispose();
                }
            });
            add(ok);

            JButton close = new JButton("Отмена");
            close.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            add(close);

            setResizable(false);
            pack();
            setVisible(true);
        }
    }
}
