package display;

import life.Lifeform;
import life.Pattern;
import logic.Console;
import logic.Simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {
    private static final int height = Toolkit.getDefaultToolkit().getScreenSize().height;
    private static final int width = Toolkit.getDefaultToolkit().getScreenSize().width;

    public static final Color backgroundColor = new Color(55, 55, 111);

    private static final Color patternToolColor = new Color(115, 103, 208);
    private static final Color newColor = new Color(149, 221, 147);

    private static final Color lifeformListColor = new Color(39, 39, 71);
    private static final Color lifeformListSelectColor = backgroundColor.brighter();

    private static final Color inactiveColor = new Color(72, 72, 72);
    private static final Color quitColor = new Color(177, 55, 55);

    private static final Color playColor = new Color(49, 128, 32);
    private static final Color pauseColor = quitColor;
    private static final Color speedChangeColor = lifeformListSelectColor;


    private static final float fontSize = 10 + (width/100);
    private static final Font mainFont = new Font(Font.MONOSPACED, Font.PLAIN, (int)fontSize);

    private static MainFrame mainFrame = null;
    private static final int margin = 10;

    private final SimulationCanvas canvas;
    private final ConsoleDisplay console;

    private final JTextField speedLabel;
    private final JButton playPauseButton;
    private final JList<Lifeform> brushList;
    private final DefaultListModel<Lifeform> brushListModel;

    private MainFrame() {
        super();
        JFrame window = this;

        this.getContentPane().setBackground(backgroundColor);

        this.setUndecorated(true);
        this.setResizable(false);
        this.setPreferredSize(new Dimension(width, height));
        this.pack();
        this.setLocationRelativeTo(null);

        this.setLayout(new GridBagLayout());
        GridBagConstraints c;

        // ======= Simulation Canvas =======
        this.canvas = new SimulationCanvas();
        c = new GridBagConstraints(
                0,                                                  //gridX
                0,                                                  //gridY
                1,                                                  //gridWidth
                8,                                                  //gridHeight
                1.0,                                                //weightX
                1.0,                                                //weightY
                GridBagConstraints.FIRST_LINE_START,                //anchor
                GridBagConstraints.BOTH,                            //fill
                new Insets(margin, margin,margin, margin),        //insets
                0,                                                  //ipadX
                0                                                   //ipadY
        );
        this.add(this.canvas, c);

        // ======= Right Side =======
        JButton button;
        c = new GridBagConstraints(
                1,                                                  //gridX
                GridBagConstraints.RELATIVE,                        //gridY
                1,                                                  //gridWidth
                1,                                                  //gridHeight
                0.1,                                                //weightX
                0.125,                                                //weightY
                GridBagConstraints.CENTER,                //anchor
                GridBagConstraints.BOTH,                            //fill
                new Insets(margin, 0,0, margin),        //insets
                0,                                                  //ipadX
                0                                                   //ipadY
        );

        // ======= Pattern Buttons =======
        Font patternFont = mainFont.deriveFont(fontSize*2/3);
        JPanel buttonSet = new JPanel(new GridLayout(1, 3));
        buttonSet.setBackground(backgroundColor);

        button = makeButton("SAVED PATTERNS", patternFont, patternToolColor, null);
        buttonSet.add(button);

        button = makeButton("NEW PATTERN", patternFont, newColor, null);
        buttonSet.add(button);

        button = makeButton("IMPORT PATTERN", patternFont, inactiveColor, a->importPatternDialog());
        buttonSet.add(button);

        this.add(buttonSet, c);

        // ======= Lifeform Brush =======
        JPanel brushPanel = new JPanel(new BorderLayout(0, margin));
        brushPanel.setBackground(backgroundColor);
        brushPanel.setBorder(null);
        brushListModel = new DefaultListModel<>();
        brushList = new JList<>(brushListModel);
        brushList.setBackground(lifeformListColor);
        brushList.setBorder(null);
        brushList.setSelectionBackground(lifeformListSelectColor);
        brushList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        brushList.setFont(mainFont.deriveFont(fontSize*2/3));
        brushList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                canvas.setLifeformBrush(this.brushList.getSelectedValue());
            }
        });
        brushList.setVisibleRowCount(8);
        JScrollPane listScrollPane = new JScrollPane(brushList);
        listScrollPane.setBackground(backgroundColor);
        listScrollPane.setBorder(null);
        JScrollBar vertical = listScrollPane.getVerticalScrollBar();
        vertical.setBackground(lifeformListColor);
        brushPanel.add(listScrollPane, BorderLayout.CENTER);

        // ======= New Lifeform =======
        JPanel newLifePanel = new JPanel(new BorderLayout(5, 0));
        newLifePanel.setBackground(backgroundColor);
        Font newLifeFont = mainFont.deriveFont(fontSize*2/3);

        class JTextFieldHinted extends JTextField {
            private final String hint;
            private int fontHeight = -1;

            public JTextFieldHinted(int columns, String hint, Font font, Color foregroundColor, Color backgroundColor) {
                super(columns);
                this.hint = hint;
                this.setFont(font);
                this.setForeground(foregroundColor);
                this.setCaretColor(foregroundColor);
                this.setBackground(backgroundColor);
                this.setBorder(null);
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if (this.getText().isEmpty()) {
                    if (this.fontHeight == -1) this.fontHeight = g.getFontMetrics().getAscent();
                    g.setFont(this.getFont());
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawString(this.hint, 5, ((this.getHeight() + this.fontHeight) / 2) - 3 );
                }
            }
        }

        JTextField newLifeName = new JTextFieldHinted(15, "Lifeform Name", newLifeFont, Color.CYAN, lifeformListColor);
        newLifePanel.add(newLifeName, BorderLayout.WEST);

        JTextField newLifeRulestring = new JTextFieldHinted(30, "Lifeform Rules", newLifeFont, Color.WHITE, lifeformListColor);
        newLifePanel.add(newLifeRulestring, BorderLayout.CENTER);

        button = makeButton("ADD", newLifeFont, newColor, a -> {
            if (newLifeName.getText().isEmpty() || newLifeRulestring.getText().isEmpty()) return;
            Lifeform newLifeform = Lifeform.create(newLifeName.getText(), newLifeRulestring.getText());
            if (newLifeform != null) {
                newLifeName.setText("");
                newLifeRulestring.setText("");
                setSelectedLifeform(newLifeform);
            }
        });
        newLifePanel.add(button, BorderLayout.EAST);

        brushPanel.add(newLifePanel, BorderLayout.SOUTH);
        this.add(brushPanel, c);

        // ======= Console =======
        this.console = new ConsoleDisplay();
        GridBagConstraints consoleC = new GridBagConstraints(
                1,                                                  //gridX
                GridBagConstraints.RELATIVE,                        //gridY
                1,                                                  //gridWidth
                3,                                                  //gridHeight
                0.1,                                                //weightX
                0.625,                                                //weightY
                GridBagConstraints.CENTER,                //anchor
                GridBagConstraints.BOTH,                            //fill
                new Insets(2*margin, 0,margin, margin),        //insets
                0,                                                  //ipadX
                0                                                   //ipadY
        );
        this.add(this.console, consoleC);

        // ======= Speed Controls =======
        JPanel simControl = new JPanel(new GridLayout(1, 3));
        simControl.setBackground(backgroundColor);

        this.playPauseButton = makeButton("⏯", mainFont.deriveFont(fontSize*2), playColor, a ->
                MainFrame.getInstance().getSimulation().togglePlayPause());
        simControl.add(playPauseButton);

        JPanel speedControl = new JPanel(new GridLayout(3, 1));
        speedControl.setBackground(backgroundColor);

        button = makeButton("▲", mainFont, speedChangeColor, a -> MainFrame.getInstance().getSimulation().fpsUp());
        speedControl.add(button);

        JPanel speedValuePanel = new JPanel(new BorderLayout());
        speedValuePanel.setBackground(backgroundColor);
        speedLabel = new JTextField(Simulation.DEFAULT_SPEED + "", 4);
        speedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        speedLabel.setBackground(lifeformListColor);
        speedLabel.setBorder(null);
        speedLabel.setFont(mainFont);
        speedLabel.setForeground(Color.LIGHT_GRAY);
        speedLabel.addActionListener(e -> {
            MainFrame.getInstance().getSimulation().setFps(speedLabel.getText());
            canvas.requestFocusInWindow();
        });
        speedLabel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                MainFrame.getInstance().getSimulation().setFps(speedLabel.getText());
            }
        });
        speedValuePanel.add(speedLabel, BorderLayout.CENTER);

        JLabel genS = new JLabel(" Gen/s ");
        genS.setFont(mainFont);
        genS.setForeground(Color.lightGray);
        speedValuePanel.add(genS, BorderLayout.EAST);

        speedControl.add(speedValuePanel);

        button = makeButton("▼", mainFont, speedChangeColor, a -> MainFrame.getInstance().getSimulation().fpsDown());
        speedControl.add(button);

        simControl.add(speedControl);

        c.weighty = 0.02;
        this.add(simControl, c);
        c.weighty = 0.125;

        // ======= New, Quit Button =======
        c.insets = new Insets(5*margin, 0, margin, margin);

        buttonSet = new JPanel(new BorderLayout(margin*3, 0));
        buttonSet.setBackground(backgroundColor);

        button = makeButton("QUIT", mainFont, quitColor, a -> window.dispose());
        buttonSet.add(button, BorderLayout.CENTER);

        button = makeButton(" NEW ", mainFont, newColor.darker(), a -> canvas.newSimulation());
        buttonSet.add(button, BorderLayout.WEST);

        this.add(buttonSet, c);

        // ======= Hotkeys =======
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e-> {
            if (e.getSource() instanceof JTextField || e.getSource() instanceof JTextArea) return false;
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                int keyCode = e.getKeyCode();
                Simulation sim = canvas.getSimulation();
                switch (keyCode) {
                    case KeyEvent.VK_SPACE: sim.togglePlayPause(); break;
                    case KeyEvent.VK_TAB: sim.toggleFps(); break;
                    case KeyEvent.VK_F: sim.pause(); sim.nextFrame(); break;
                    case KeyEvent.VK_W: canvas.pan(0, -canvas.getPanStep()); break;
                    case KeyEvent.VK_A: canvas.pan(-canvas.getPanStep(), 0); break;
                    case KeyEvent.VK_S: canvas.pan(0, canvas.getPanStep()); break;
                    case KeyEvent.VK_D: canvas.pan(canvas.getPanStep(), 0); break;
                    default: return false;
                }
                e.consume();
                return true;
            }
            return false;
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                MainFrame.getInstance().getSimulation().kill();
                super.windowClosed(e);
            }
        });

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].setFullScreenWindow(this);
        this.setVisible(true);
    }

    private void importPatternDialog() {
        JDialog dialog = new JDialog(this, "Import Pattern", true);
        dialog.setLayout(new BorderLayout(0, 20));
        dialog.getContentPane().setBackground(backgroundColor);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(width/2, height/2);
        dialog.setLocationRelativeTo(null);
        dialog.setUndecorated(true);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(backgroundColor, 20));
        JTextArea patternText = new JTextArea();
        patternText.setBackground(lifeformListColor);
        patternText.setForeground(Color.WHITE);
        patternText.setFont(mainFont.deriveFont(fontSize*2/3));
        patternText.setLineWrap(false);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBackground(backgroundColor);

        JButton button;

        button = new JButton("Import");
        button.setBackground(newColor);
        button.setFont(mainFont);
        button.addActionListener(a -> {
            Pattern p = Pattern.parse(patternText.getText());
            if (p != null) this.canvas.setLoadedPattern(p);
            dialog.dispose();
        });
        buttonPanel.add(button);

        button = new JButton("Cancel");
        button.setBackground(quitColor);
        button.setFont(mainFont);
        button.setForeground(Color.LIGHT_GRAY);
        button.addActionListener(a -> dialog.dispose());
        buttonPanel.add(button);

        dialog.add(new JScrollPane(patternText), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }


    public void updateSpeedLabel(String speed) {
        this.speedLabel.setText(speed);
        this.speedLabel.revalidate();
    }

    public void updatePlayPause(boolean running) {
        if (running) playPauseButton.setBackground(pauseColor);
        else playPauseButton.setBackground(playColor);
    }

    public void updateLifeformList() {
        brushListModel.clear();
        for (Lifeform l : Lifeform.getAll()) brushListModel.addElement(l);
        brushList.revalidate();
    }

    private JButton makeButton(String text, Font font, Color backgroundColor, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setFont(font);
        //0.2126*R + 0.7152*G + 0.0722*B
        if ((backgroundColor.getRed()*0.2126 + backgroundColor.getGreen()*0.7152 + backgroundColor.getBlue()*0.0722) < 127)
            button.setForeground(Color.LIGHT_GRAY);
        //button.setForeground(foregroundColor);
        button.addActionListener(listener);
        button.setFocusPainted(false);
        return button;
    }

    public Simulation getSimulation() { return this.canvas.getSimulation(); }
    public SimulationCanvas getCanvas() { return this.canvas; }
    public Console getConsole() { return this.console.getConsole(); }
    public Lifeform getSelectedLifeform() { return this.brushList.getSelectedValue(); }
    public void setSelectedLifeform(Lifeform l) { this.brushList.setSelectedValue(Lifeform.getById(l.getId()), true); }

    public static MainFrame getInstance() {
        if (mainFrame == null) mainFrame = new MainFrame();
        return mainFrame;
    }
}
