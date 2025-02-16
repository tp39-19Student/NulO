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

    //Border drawing
    private int extraX;
    private int extraY;
    private int simEndX;
    private int simEndY;

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

                simEndX = simWidth*pixelSize;
                simEndY = simHeight*pixelSize;
                extraX = width - simEndX;
                extraY = height - simEndY;

                c.newSimulation();
                MainFrame.getInstance().setSelectedLifeform(Lifeform.GOL);
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
                    //TODO - Print cell to console
                    int selectX = e.getX()/pixelSize;
                    int selectY = e.getY()/pixelSize;

                    return;
                }
                if (buttonPressed != MouseEvent.BUTTON1 && buttonPressed != MouseEvent.BUTTON3) return;
                Lifeform life = (buttonPressed == MouseEvent.BUTTON3)?null:brush;

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
                if (buttonPressed != MouseEvent.BUTTON1 && buttonPressed != MouseEvent.BUTTON3) return;
                Lifeform life = (buttonPressed == MouseEvent.BUTTON3)?null:brush;

                int endX = e.getX();
                int endY = e.getY();
                int dx = (endX - startX);
                int dy = (endY - startY);


                if (Math.abs(dx) > Math.abs(dy)) {
                    int iStep = (startX<endX)?1:-1;
                    double dStep = ((dy * 1.0) / dx)*(crazy?1:iStep);
                    double y = startY;

                    for (int x = startX; x != endX; x += iStep) {
                        sim.setCell(x/pixelSize, (int) Math.floor(y/pixelSize), life);
                        y += dStep;
                    }
                } else {
                    int iStep = (startY<endY)?1:-1;
                    double dStep = ((dx * 1.0) / dy)*(crazy?1:iStep);
                    double x = startX;

                    for (int y = startY; y != endY; y += iStep) {
                        sim.setCell((int) Math.floor(x/pixelSize), y/pixelSize, life);
                        x += dStep;
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

        // ====== Cells ======
        if (sim != null) {
            Cell[][] data = sim.getData();
            for (int i = 0; i < simWidth; i++)
                for (int j = 0; j < simHeight; j++) {
                    g.setColor(data[i][j].getColor());
                    g.fillRect(i*pixelSize, j*pixelSize, pixelSize, pixelSize);
                }
        }

        // ====== Mouse Selection ======
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        int mouseX = mousePosition.x - this.getLocationOnScreen().x;
        int mouseY = mousePosition.y - this.getLocationOnScreen().y;
        if (mouseX > 0 && mouseX < width && mouseY > 0 && mouseY < height) {
            g.setColor(Color.GRAY);
            g.drawRect((mouseX/pixelSize)*pixelSize, (mouseY/pixelSize)*pixelSize, pixelSize, pixelSize);
        }

        // ====== Extra Space ======
        g.setColor(MainFrame.backgroundColor);
        if (extraX > 0) g.fillRect(simEndX, 0, extraX, height);
        if (extraY > 0) g.fillRect(0, simEndY, width, extraY);
    }

    public boolean setPixelSize(int size) {
        int simWidth = (width - (width%size))/size;
        int simHeight = (height - (height%size))/size;

        if (simWidth < 2*Lifeform.MAX_RANGE + 3) return false;
        if (simHeight < 2*Lifeform.MAX_RANGE + 3) return false;

        this.pixelSize = size;
        this.simWidth = simWidth;
        this.simHeight = simHeight;
        simEndX = simWidth*pixelSize;
        simEndY = simHeight*pixelSize;
        extraX = width - simEndX;
        extraY = height - simEndY;

        newSimulation();
        return true;
    }



    public void newSimulation() {
        if (sim != null) sim.kill();
        this.sim = new Simulation(simWidth, simHeight, this, MainFrame.getInstance().getConsole());
        MainFrame.getInstance().getConsole().setSimulation(this.sim);
        this.repaint();
    }

    public int getPixelSize() { return pixelSize; }
    public Simulation getSimulation() { return sim; }
    public void setLifeformBrush(Lifeform life) { this.brush = life; }

    public boolean toggleCrazy() { crazy = !crazy; return crazy; }
}
