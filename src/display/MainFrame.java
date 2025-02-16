package display;

import life.Lifeform;
import logic.Console;
import logic.Simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {
    public static final Color backgroundColor = new Color(55, 55, 111);

    private static final Color inactiveColor = new Color(72, 72, 72);
    private static final Color quitColor = new Color(201, 60, 60);

    private static final Color playColor = new Color(105, 228, 83);
    private static final Color pauseColor = new Color(210, 96, 96);
    private static final Color speedChangeColor = new Color(104, 104, 143);

    private static final Color patternToolColor = new Color(115, 103, 208);
    private static final Color newColor = new Color(149, 221, 147);

    private static final Font mainFont = new Font(Font.MONOSPACED, Font.PLAIN, 30);

    private static MainFrame mainFrame = null;
    private static final int height = Toolkit.getDefaultToolkit().getScreenSize().height;
    private static final int width = Toolkit.getDefaultToolkit().getScreenSize().width;
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
        JPanel buttonSet = new JPanel(new GridLayout(1, 2));
        button = new JButton("INSERT PATTERN");
        button.setFont(mainFont);
        button.setBackground(patternToolColor);
        button.setFocusPainted(false);
        buttonSet.add(button);
        button = new JButton("NEW PATTERN");
        button.setFont(mainFont);
        button.setBackground(newColor);
        button.setFocusPainted(false);
        buttonSet.add(button);
        this.add(buttonSet, c);

        // ======= Lifeform Brush =======
        JPanel brushPanel = new JPanel(new BorderLayout());
        brushListModel = new DefaultListModel<>();
        brushList = new JList<>(brushListModel);
        brushList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        brushList.setFont(mainFont.deriveFont(18F));
        brushList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                canvas.setLifeformBrush(this.brushList.getSelectedValue());
                getConsole().println("Selected: " + this.brushListModel.get(this.brushList.getSelectedIndex()));
            }
        });
        brushPanel.add(new JScrollPane(brushList), BorderLayout.CENTER);

        // ======= New Lifeform =======
        JPanel newLifePanel = new JPanel(); //#TODO
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
        button = new JButton("⏯");
        button.setFont(mainFont.deriveFont(50F));
        button.setBackground(playColor);
        button.setFocusPainted(false);
        playPauseButton = button;
        button.addActionListener(a -> MainFrame.getInstance().getSimulation().togglePlayPause());
        simControl.add(button);

        JPanel speedControl = new JPanel(new GridLayout(3, 1));
        button = new JButton("▲");
        button.setFont(mainFont);
        button.setBackground(speedChangeColor);
        button.setFocusPainted(false);
        button.addActionListener(a -> MainFrame.getInstance().getSimulation().fpsUp());
        speedControl.add(button);
        JPanel speedValuePanel = new JPanel(new BorderLayout());
        speedLabel = new JTextField(Simulation.DEFAULT_SPEED + "", 4);
        speedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        speedLabel.setFont(mainFont);
        speedLabel.addActionListener(e -> MainFrame.getInstance().getSimulation().setFps(speedLabel.getText()));
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
        speedValuePanel.add(genS, BorderLayout.EAST);
        speedControl.add(speedValuePanel);
        button = new JButton("▼");
        button.setFont(mainFont);
        button.setBackground(speedChangeColor);
        button.setFocusPainted(false);
        button.addActionListener(a -> MainFrame.getInstance().getSimulation().fpsDown());
        speedControl.add(button);
        simControl.add(speedControl);
        c.weighty = 0.02;
        this.add(simControl, c);
        c.weighty = 0.125;

        // ======= Quit Button =======
        c.insets = new Insets(5*margin, 0, margin, margin);
        button = new JButton("QUIT");
        button.setFont(mainFont);
        button.setForeground(Color.WHITE);
        button.setBackground(quitColor);
        button.addActionListener(e -> window.dispose());
        button.setFocusPainted(false);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                MainFrame.getInstance().getSimulation().kill();
                super.windowClosed(e);
            }
        });

        this.add(button, c);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setBackground(backgroundColor);



        this.setVisible(true);
        GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].setFullScreenWindow(this);

        // ======= Hotkeys =======
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e-> {
                if (!console.inputFocused() && !speedLabel.hasFocus() && e.getID() == KeyEvent.KEY_PRESSED) {
                    int keyCode = e.getKeyCode();
                    Simulation sim = canvas.getSimulation();
                    switch (keyCode) {
                        case KeyEvent.VK_SPACE: sim.togglePlayPause(); break;
                        case KeyEvent.VK_TAB: sim.toggleFps(); break;
                        case KeyEvent.VK_F: sim.pause(); sim.nextFrame(); break;
                        default: return false;
                    }
                    e.consume();
                    return true;
                }
                return false;
            }
        );

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

    public Simulation getSimulation() { return this.canvas.getSimulation(); }
    public SimulationCanvas getCanvas() { return this.canvas; }
    public Console getConsole() { return this.console.getConsole(); }
    public Lifeform getSelectedLifeform() { return this.brushList.getSelectedValue(); }
    public void setSelectedLifeform(Lifeform l) { this.brushList.setSelectedValue(l, true); }

    public static MainFrame getInstance() {
        if (mainFrame == null) mainFrame = new MainFrame();
        return mainFrame;
    }
}
