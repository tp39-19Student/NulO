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
    private int basePixelSize = 6;
    private int pixelSize = basePixelSize;
    private int width = -1;
    private int height = -1;
    private int simWidth = -1;
    private int simHeight = -1;


    //Border drawing
    private int extraX;
    private int extraY;
    private int simEndX;
    private int simEndY;

    //Placing cells
    private int mouseStartX;
    private int mouseStartY;

    private Lifeform brush = null;
    private boolean crazy = false;
    private int buttonPressed = -1;

    //Pattern
    private Pattern loadedPattern;
    private int patternState; // 0 - None, 1 - Loaded, 2 - Finished (Block Brush)

    //Zoom / Pan (Cell Indexes)
    private int drawXStart;
    private int drawXEnd;
    private int drawYStart;
    private int drawYEnd;
    private int panStep;

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
                simWidth = (width - (width%pixelSize))/basePixelSize;
                simHeight = (height - (height%pixelSize))/basePixelSize;

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
                        loadedPattern.place(e.getX()/pixelSize + drawXStart, e.getY()/pixelSize + drawYStart);
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
                    //Middle Click
                    int selectX = e.getX()/pixelSize + drawXStart;
                    int selectY = e.getY()/pixelSize + drawYStart;

                    MainFrame.getInstance().getConsole().command("debug " + selectX + " " + selectY);
                    return;
                }
                if (buttonPressed != MouseEvent.BUTTON1 && buttonPressed != MouseEvent.BUTTON3) return;
                Lifeform life = (buttonPressed == MouseEvent.BUTTON3)?null:brush;

                mouseStartX = e.getX() + drawXStart*pixelSize;
                mouseStartY = e.getY() + drawYStart*pixelSize;

                sim.setCell((mouseStartX) /pixelSize, (mouseStartY) /pixelSize, life, shift);

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

                int endX = e.getX() + drawXStart*pixelSize;
                int endY = e.getY() + drawYStart*pixelSize;
                int dx = (endX - mouseStartX);
                int dy = (endY - mouseStartY);


                if (Math.abs(dx) > Math.abs(dy)) {
                    int iStep = (mouseStartX <endX)?1:-1;
                    double dStep = ((dy * 1.0) / dx)*(crazy?1:iStep);
                    double y = mouseStartY;

                    for (int x = mouseStartX; x != endX; x += iStep) {
                        sim.setCell(x/pixelSize, (int) Math.floor(y/pixelSize), life, shift);
                        y += dStep;
                    }
                } else {
                    int iStep = (mouseStartY <endY)?1:-1;
                    double dStep = ((dx * 1.0) / dy)*(crazy?1:iStep);
                    double x = mouseStartX;

                    for (int y = mouseStartY; y != endY; y += iStep) {
                        sim.setCell((int) Math.floor(x/pixelSize), y/pixelSize, life, shift);
                        x += dStep;
                    }
                }
                mouseStartX = endX;
                mouseStartY = endY;
                c.repaint();
            }
        });

        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int amount = e.getWheelRotation();
                int aroundX = e.getX()/pixelSize + drawXStart;
                int aroundY = e.getY()/pixelSize + drawYStart;

                zoomAround(aroundX, aroundY, amount);
            }
        });
    }


    @Override
    protected void paintComponent(Graphics g) {
        //super.paintComponent(g);

        if (sim == null) return;
        Cell[][] data = sim.getData();

        // ====== Cells ======
        for (int i = drawYStart; i < drawYEnd; i++)
            for (int j = drawXStart; j < drawXEnd; j++) {
                data[i][j].paint(g);
            }

        // ====== Mouse Position ======
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        int mouseX = ((mousePosition.x - this.getLocationOnScreen().x)/pixelSize) + drawXStart;
        int mouseY = ((mousePosition.y - this.getLocationOnScreen().y)/pixelSize) + drawYStart;
        if (mouseX >= drawXStart && mouseX < drawXEnd && mouseY >= drawYStart && mouseY < drawYEnd) {
            g.setColor(brush.getColor());
            g.drawRect((mouseX - drawXStart)*pixelSize, (mouseY - drawYStart)*pixelSize, pixelSize-1, pixelSize-1);
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

    public boolean setBasePixelsize(int size) {
        int simWidth = (width - (width%size))/size;
        int simHeight = (height - (height%size))/size;

        if (simWidth < 2*Lifeform.MAX_RANGE + 3) return false;
        if (simHeight < 2*Lifeform.MAX_RANGE + 3) return false;

        this.basePixelSize = size;
        this.simWidth = simWidth;
        this.simHeight = simHeight;
        simEndX = simWidth*basePixelSize;
        simEndY = simHeight*basePixelSize;
        extraX = width - simEndX;
        extraY = height - simEndY;

        newSimulation();
        return true;
    }

    private void setDrawingBounds(int drawXStart, int drawYStart, int drawSize) {
        int drawXEnd = drawXStart + (width/drawSize);
        int drawYEnd = drawYStart + (height/drawSize);
        setDrawingBounds(drawXStart, drawXEnd, drawYStart, drawYEnd);
    }

    private void setDrawingBounds(int drawXStart, int drawXEnd, int drawYStart, int drawYEnd) {
        //Pan if out of bounds
        if (drawXStart < 0) {drawXEnd -= drawXStart; drawXStart = 0;}
        else if (drawXEnd > simWidth) {drawXStart -= (drawXEnd - simWidth); drawXEnd = simWidth;}
        if (drawYStart < 0) {drawYEnd -= drawYStart; drawYStart = 0;}
        else if (drawYEnd > simHeight) {drawYStart -= (drawYEnd - simHeight); drawYEnd = simHeight;}

            //Inverted
        if (((drawXStart >= drawXEnd || drawYStart >= drawYEnd))
                //Not Square
                || (((int) Math.round(simWidth * 1.0 / (drawXEnd - drawXStart)) != (int) Math.round(simHeight * 1.0 / (drawYEnd - drawYStart))))
                //Out of Bounds
                || ((drawXStart < 0 || drawYStart < 0 || drawXEnd > simWidth || drawYEnd > simHeight))
        ) {
            return;
        }

        this.drawXStart = drawXStart;
        this.drawXEnd = drawXEnd;
        this.drawYStart = drawYStart;
        this.drawYEnd = drawYEnd;

        updateCellDrawing();
        this.repaint();
    }

    private void updateCellDrawing() {
        Cell[][] data = sim.getData();
        int drawSize = (int) Math.round(width * 1.0 / (drawXEnd - drawXStart));

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                if (i < drawYStart || i >= drawYEnd || j < drawXStart || j >= drawXEnd) {
                    data[i][j].setShouldDraw(false);
                } else {
                    data[i][j].setDrawParams(drawSize, (j - drawXStart)*drawSize, (i - drawYStart)*drawSize);
                    data[i][j].setShouldDraw(true);
                }
            }
        }

        this.pixelSize = drawSize;
        this.panStep = (this.width/4)/this.pixelSize;
    }

    private void zoomAround(int indexX, int indexY, int direction) {
        int zoom = (pixelSize/basePixelSize);
        if (direction > 0) zoom = zoom / 2;
        else if (direction < 0) zoom = zoom * 2;
        else return;

        if (zoom < 1) setDrawingBounds(0, simWidth, 0, simHeight);

        else {
            double factorX = ((indexX - drawXStart) * 1.0) / (drawXEnd - drawXStart - 1);
            double factorY = ((indexY - drawYStart) * 1.0) / (drawYEnd - drawYStart - 1);
            setDrawingBounds((int)Math.round(indexX - (factorX)*simWidth/(zoom)), (int) Math.round(indexY - (factorY)*simHeight/(zoom)), zoom * basePixelSize);
        }
    }


    public void newSimulation() {
        boolean running = false;
        if (sim != null && sim.getRunning()) running = true;
        if (sim != null) sim.kill();
        this.sim = new Simulation(simWidth, simHeight, this, MainFrame.getInstance().getConsole(), sim!=null?sim.getFps():Simulation.DEFAULT_SPEED);
        MainFrame.getInstance().getConsole().setSimulation(this.sim);

        simEndX = simWidth*basePixelSize;
        simEndY = simHeight*basePixelSize;
        extraX = width - simEndX;
        extraY = height - simEndY;

        this.setDrawingBounds(0, simWidth, 0, simHeight);
        if (running) sim.play();
    }

    public int getBasePixelSize() { return basePixelSize; }
    public int getPixelSize() {return pixelSize;}
    public Simulation getSimulation() { return sim; }
    public void setLifeformBrush(Lifeform life) { this.brush = life; }
    public void setLoadedPattern(Pattern pattern) { this.patternState = 1; this.loadedPattern = pattern;  }

    public int getDrawXStart() {return drawXStart;}
    public int getDrawXEnd() {return drawXEnd;}
    public int getDrawYStart() {return drawYStart;}
    public int getDrawYEnd() {return drawYEnd;}
    public int getPanStep() {return panStep;}

    public void pan(int x, int y) {
        setDrawingBounds(drawXStart + x, drawXEnd + x, drawYStart + y, drawYEnd + y);
    }


    public boolean toggleCrazy() { crazy = !crazy; return crazy; }
}
