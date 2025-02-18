package display;

import life.Lifeform;
import life.Pattern;
import logic.Cell;
import logic.Simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimulationCanvas extends JPanel {

    private Simulation sim = null;
    private int pixelSize = 6;
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

    private Pattern loadedPattern;
    int patternState; // 0 - None, 1 - Loaded, 2 - Finished (Block Brush)

    public SimulationCanvas() {
        super();
        this.loadedPattern = null;
        this.patternState = 0;
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
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (patternState == 0) loadedPattern = null;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                c.requestFocusInWindow();

                boolean shift = (e.getModifiers() & Event.SHIFT_MASK) != 0;
                buttonPressed = e.getButton();

                if (patternState == 1) {
                    if (buttonPressed == MouseEvent.BUTTON1) {
                        loadedPattern.place(e.getX()/pixelSize, e.getY()/pixelSize);
                        if (!shift) {
                            patternState = 2;
                        }
                    } else {
                        patternState = 2;
                    }

                    repaint();
                    return;
                }
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

                sim.setCell(startX /pixelSize, startY /pixelSize, life, shift);

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
                if (loadedPattern != null) {repaint(); return;}
                boolean shift = (e.getModifiers() & Event.SHIFT_MASK) != 0;
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
                        sim.setCell(x/pixelSize, (int) Math.floor(y/pixelSize), life, shift);
                        y += dStep;
                    }
                } else {
                    int iStep = (startY<endY)?1:-1;
                    double dStep = ((dx * 1.0) / dy)*(crazy?1:iStep);
                    double x = startX;

                    for (int y = startY; y != endY; y += iStep) {
                        sim.setCell((int) Math.floor(x/pixelSize), y/pixelSize, life, shift);
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
        //super.paintComponent(g);

        if (sim == null) return;
        Cell[][] data = sim.getData();

        // ====== Cells ======
        for (int i = 0; i < simHeight; i++)
            for (int j = 0; j < simWidth; j++) {
                data[i][j].paint(g);
            }

        // ====== Mouse Position ======
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        int mouseX = ((mousePosition.x - this.getLocationOnScreen().x)/pixelSize);
        int mouseY = ((mousePosition.y - this.getLocationOnScreen().y)/pixelSize);
        if (mouseX > 0 && mouseX < simWidth && mouseY > 0 && mouseY < simHeight) {
            g.setColor(brush.getColor());
            g.drawRect(mouseX*pixelSize, mouseY*pixelSize, pixelSize-1, pixelSize-1);
            data[mouseY][mouseX].repaintNextFrame();
        }

        // ====== Extra Space ======
        g.setColor(MainFrame.backgroundColor);
        if (extraX > 0) g.fillRect(simEndX, 0, extraX, height);
        if (extraY > 0) g.fillRect(0, simEndY, width, extraY);

        // ====== Pattern Preview ======
        if (patternState == 2) {
            patternState = 0;
        }
        if (patternState == 1 && this.loadedPattern != null) {
            this.loadedPattern.drawPreview(g, mouseX, mouseY);
            for (int i = Math.max(mouseY, 0); i < Math.min(mouseY + loadedPattern.getHeight(), this.simHeight); i++ )
                for (int j = Math.max(mouseX, 0); j < Math.min(mouseX + loadedPattern.getWidth(), this.simWidth); j++ ) {
                    data[i][j].repaintNextFrame();
                }
        }
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
    public void setLoadedPattern(Pattern pattern) { this.patternState = 1; this.loadedPattern = pattern;  }

    public boolean toggleCrazy() { crazy = !crazy; return crazy; }
}
