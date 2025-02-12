package display;

import logic.Console;
import logic.Simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/*
c = new GridBagConstraints(
        0,                                                  //gridX
        0,                                                  //gridY
        1,                                                  //gridWidth
        1,                                                  //gridHeight
        0.0,                                                //weightX
        0.0,                                                //weightY
        GridBagConstraints.CENTER,                          //anchor
        GridBagConstraints.NONE,                            //fill
        new Insets(0, 0,0, 0),                              //insets
        0,                                                  //ipadX
        0                                                   //ipadY
);
 */

public class MainFrame extends JFrame {
    private static MainFrame mainFrame = null;
    private static final int height = Toolkit.getDefaultToolkit().getScreenSize().height;
    private static final int width = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static final int margin = 10;

    private final SimulationCanvas canvas;
    private final ConsoleDisplay console;

    private MainFrame() {
        super();
        JFrame window = this;

        this.setPreferredSize(new Dimension(width, height));
        this.setUndecorated(true);

        this.pack();
        this.setLocationRelativeTo(null);

        this.setResizable(false);


        this.setLayout(new GridBagLayout());
        GridBagConstraints c;

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

        JButton button;

        c = new GridBagConstraints(
                1,                                                  //gridX
                GridBagConstraints.RELATIVE,                        //gridY
                1,                                                  //gridWidth
                1,                                                  //gridHeight
                0.4,                                                //weightX
                0.125,                                                //weightY
                GridBagConstraints.CENTER,                //anchor
                GridBagConstraints.BOTH,                            //fill
                new Insets(margin, 0,margin, margin),        //insets
                0,                                                  //ipadX
                0                                                   //ipadY
        );

        Font buttonFont = new Font(Font.MONOSPACED, Font.BOLD, 30);

        button = new JButton("RULESETS");
        button.setFont(buttonFont);
        button.setBackground(new Color(60, 112, 201));
        button.setFocusPainted(false);
        this.add(button, c);

        button = new JButton("2");
        button.setFont(buttonFont);
        button.setBackground(new Color(72, 72, 72));
        button.setFocusPainted(false);
        this.add(button, c);

        button = new JButton("3");
        button.setFont(buttonFont);
        button.setBackground(new Color(72, 72, 72));
        button.setFocusPainted(false);
        this.add(button, c);

        button = new JButton("4");
        button.setFont(buttonFont);
        button.setBackground(new Color(72, 72, 72));
        button.setFocusPainted(false);
        this.add(button, c);

        button = new JButton("QUIT");
        button.setFont(buttonFont);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(201, 60, 60));
        button.addActionListener(e -> window.dispose());
        button.setFocusPainted(false);
        this.add(button, c);

        this.console = new ConsoleDisplay();

        c = new GridBagConstraints(
                1,                                                  //gridX
                GridBagConstraints.RELATIVE,                        //gridY
                1,                                                  //gridWidth
                3,                                                  //gridHeight
                0.4,                                                //weightX
                0.625,                                                //weightY
                GridBagConstraints.CENTER,                //anchor
                GridBagConstraints.BOTH,                            //fill
                new Insets(margin, 0,margin, margin),        //insets
                0,                                                  //ipadX
                0                                                   //ipadY
        );

        this.add(this.console, c);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setBackground(new Color(55, 55, 111));

        this.setVisible(true);

        GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].setFullScreenWindow(this);
        //System.out.println(this.getInsets().toString());

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e-> {
                if (!console.inputFocused() && e.getID() == KeyEvent.KEY_PRESSED) {
                    int keyCode = e.getKeyCode();
                    //console.println("Pushed key: " + keyCode);
                    Console cmd = console.getConsole();
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

    public Simulation getSimulation() {
        return this.canvas.getSimulation();
    }
    public SimulationCanvas getCanvas() {
        return this.canvas;
    }
    public Console getConsole() {
        return this.console.getConsole();
    }

    public static MainFrame getInstance() {
        if (mainFrame == null) mainFrame = new MainFrame();
        return mainFrame;
    }
}
