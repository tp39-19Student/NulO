package display;

import life.Lifeform;
import logic.Cell;
import logic.Simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimulationCanvas extends JPanel {

    private Simulation sim = null;
    private int pixelSize = 8;
    private int width = -1;
    private int height = -1;
    private int simWidth = -1;
    private int simHeight = -1;

    private int startX;
    private int startY;

    private Lifeform brush = null;
    private boolean crazy = false;

    private int buttonPressed = -1;

    public SimulationCanvas() {
        super();
        this.setBackground(Color.BLACK);
        SimulationCanvas c = this;
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                width = c.getWidth();
                height = c.getHeight();
                simWidth = (width - (width%pixelSize))/pixelSize;
                simHeight = (height - (height%pixelSize))/pixelSize;
                c.newSimulation();
                MainFrame.getInstance().setSelectedLifeform(Simulation.GOL);
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseEntered(e);
                c.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                c.requestFocusInWindow();
                buttonPressed = e.getButton();
                if (buttonPressed == MouseEvent.BUTTON2) {
                    int selectX = e.getX()/pixelSize;
                    int selectY = e.getY()/pixelSize;

                    //#TODO
                    MainFrame.getInstance().getConsole().println("Reworking");
                    return;
                }
                if (buttonPressed != MouseEvent.BUTTON1 && buttonPressed != MouseEvent.BUTTON3) return;
                Lifeform life = (buttonPressed == MouseEvent.BUTTON1)?brush:null;

                startX = e.getX();
                startY = e.getY();
                sim.setCell(startX /pixelSize, startY /pixelSize, life);

                c.repaint();
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                c.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                //System.out.println(buttonPressed);

                if (buttonPressed != MouseEvent.BUTTON1 && buttonPressed != MouseEvent.BUTTON3) return;
                Lifeform life = (buttonPressed == MouseEvent.BUTTON1)?brush:null;

                int endX = e.getX();
                int endY = e.getY();


                int dx = (endX - startX);
                int dy = (endY - startY);

                if (Math.abs(dx) > Math.abs(dy)) {
                    int istep = (startX<endX)?1:-1;
                    double dstep = ((dy * 1.0) / dx)*(crazy?1:istep);

                    float y = startY;
                    for (int x = startX; x != endX; x += istep) {
                        sim.setCell(x/pixelSize, Math.round(y/pixelSize), life);
                        y += dstep;
                    }
                } else {
                    int istep = (startY<endY)?1:-1;
                    double dstep = ((dx * 1.0) / dy)*(crazy?1:istep);

                    float x = startX;
                    for (int y = startY; y != endY; y += istep) {
                        sim.setCell(Math.round(x/pixelSize), y/pixelSize, life);
                        x += dstep;
                    }
                }


                startX = endX;
                startY = endY;
                c.repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        int mouseX = mousePosition.x - this.getLocationOnScreen().x;
        int mouseY = mousePosition.y - this.getLocationOnScreen().y;
        if (sim != null) {
            Cell[][] data = sim.getData();
            for (int i = 0; i < simWidth; i++)
                for (int j = 0; j < simHeight; j++) {
                    g.setColor(data[i][j].getColor());
                    g.fillRect(i*pixelSize, j*pixelSize, pixelSize, pixelSize);
                }
        }
        if (mouseX > 0 && mouseX < width && mouseY > 0 && mouseY < height) {
            g.setColor(Color.GRAY);
            g.drawRect((mouseX/pixelSize)*pixelSize, (mouseY/pixelSize)*pixelSize, pixelSize, pixelSize);
        }
    }

    public void setPixelSize(int size) {
        this.pixelSize = size;
        simWidth = (width - (width%pixelSize))/pixelSize;
        simHeight = (height - (height%pixelSize))/pixelSize;
        newSimulation();
    }

    public boolean toggleCrazy() {
        crazy = !crazy;
        return crazy;
    }

    public void newSimulation() {
        if (sim != null) sim.kill();
        this.sim = new Simulation(simWidth, simHeight, this, MainFrame.getInstance().getConsole());
        MainFrame.getInstance().getConsole().setSimulation(this.sim);
        this.repaint();
    }

    public int getPixelSize() {
        return pixelSize;
    }

    public Simulation getSimulation() {
        return sim;
    }

    public void setLifeformBrush(Lifeform life) {
        this.brush = life;
    }
}
