package logic;

import display.SimulationCanvas;
import life.Lifeform;
import life.LifeformLifelike;

import java.awt.*;
import java.util.*;
import java.util.Timer;

public class Simulation
{
    private static final int defaultSpeed = 45; //FPS
    public static Lifeform test = new LifeformLifelike("test", "B3/S23", new Color(0, 124, 0));

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
        this.canvas.setLifeformBrush(test); //!!!!!!
        this.fps = defaultSpeed;
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
        }, 1000 / fps, 1000 / fps);
    }
    public void pause() {
        if (!this.running) return;
        this.running = false;
        if (timer != null) timer.cancel();
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
        if (this.running) {
            pause();
            play();
        }
    }

    private static final int[] speeds = new int[] {10, 45, 120};
    public void toggleFps() {
        for (int speed : speeds)
            if (fps < speed) {
                console.command("fps " + speed);
                return;
            }
        console.command("fps " + speeds[0]);
    }

    public int getFps() {return this.fps;}
}
