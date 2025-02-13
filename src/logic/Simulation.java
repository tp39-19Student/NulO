package logic;

import display.MainFrame;
import display.SimulationCanvas;
import life.Lifeform;

import java.awt.*;
import java.util.*;
import java.util.Timer;

public class Simulation
{
    public static final int DEFAULT_SPEED = 45; //FPS
    public static Lifeform GOL = Lifeform.create("Conway's Life", "B3/S23", new Color(0, 124, 0));
    public static Lifeform PEDL = Lifeform.create("Pedestrian Life", "B38/S23", new Color(116, 116, 116));
    public static Lifeform DAN = Lifeform.create("Day and Night", "B3678/S34678", new Color(218, 137, 86));
    public static Lifeform FLCK = Lifeform.create("Flock", "B3/S12", new Color(210, 194, 59));
    public static Lifeform LWD = Lifeform.create("Life without Death", "B3/S012345678", new Color(96, 8, 83));



    private final int width;
    private final int height;
    private boolean running;
    private int fps;

    private Timer timer;
    private final Cell[][] data;

    private final SimulationCanvas canvas;
    private final Console console;

    //private HashMap<Lifeform, Integer> lifeformCount;

    public Simulation(int width, int height, SimulationCanvas canvas, Console console) {
        this.width = width;
        this.height = height;
        this.canvas = canvas;
        this.console = console;

        this.data = new Cell[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                data[i][j] = new Cell(i, j);
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                data[i][j].initNeighbourRings(data);

        this.running = false;
        MainFrame.getInstance().updatePlayPause(this.running);
        this.fps = DEFAULT_SPEED;
        updateSpeedLabel();
    }

    public void play() {
        if (this.running) return;
        Simulation s = this;
        this.running = true;
        this.timer = new Timer(true);
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                s.nextFrame();
            }
        }, 0, 1000 / fps);
        MainFrame.getInstance().updatePlayPause(this.running);
    }
    public void pause() {
        if (!this.running) return;
        this.running = false;
        if (timer != null) timer.cancel();
        MainFrame.getInstance().updatePlayPause(this.running);
    }

    public void togglePlayPause() {
        if (this.running) pause();
        else play();
    }

    public void kill() {
        this.running = false;
        if (timer != null) timer.cancel();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Cell[][] getData() {
        return data;
    }

    public void setCell(int x, int y, Lifeform life) {
        if (x < 0 || y < 0 || x >= data.length || y >= data[0].length) return;
        data[x][y].setCell(life);
    }

    public void nextFrame() {
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                data[i][j].updateCalculate();

        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                data[i][j].updateCommit();

        canvas.repaint();
    }

    public void setFps(int fps) {
        this.fps = fps;
        updateSpeedLabel();
        if (this.running) {
            pause();
            play();
        }
    }
    public void setFps(String fps) {
        int f = -1;
        try {
            f = Integer.parseInt(fps);
        } catch (NumberFormatException e) {}

        if (f > 0 && f <= 1000) setFps(f);
        else updateSpeedLabel();
    }

    private void updateSpeedLabel() {
        MainFrame.getInstance().updateSpeedLabel(this.fps + "");
    }

    private static final int[] speeds = new int[] {10, 45, 120};
    public void toggleFps() {
        for (int speed : speeds)
            if (fps < speed) {
                setFps(speed);
                return;
            }
        setFps(speeds[0]);
    }
    public void fpsUp() {
        for (int speed : speeds) {
            if (fps < speed) {
                setFps(speed);
                return;
            }
        }
    }
    public void fpsDown() {
        for (int i = speeds.length - 1; i >= 0; i--) {
            if (fps > speeds[i]) {
                setFps(speeds[i]);
                return;
            }
        }
    }

    public int getFps() {return this.fps;}
    public boolean getRunning() {return  this.running;}
}
