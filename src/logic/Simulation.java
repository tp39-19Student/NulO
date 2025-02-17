package logic;

import display.MainFrame;
import display.SimulationCanvas;
import life.Lifeform;

import javax.swing.Timer;
import java.awt.*;

public class Simulation
{
    public static final int DEFAULT_SPEED = 50; //FPS

    private final int width;
    private final int height;
    private boolean running;
    private int fps;

    private final Timer timer;
    private final Cell[][] data;

    private final SimulationCanvas canvas;
    private final Console console;

    public Simulation(int width, int height, SimulationCanvas canvas, Console console) {
        this.width = width;
        this.height = height;
        this.canvas = canvas;
        this.console = console;

        this.data = new Cell[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                data[i][j] = new Cell(j, i);
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                data[i][j].initNeighbourRings(data);

        this.running = false;
        MainFrame.getInstance().updatePlayPause(this.running);
        this.fps = DEFAULT_SPEED;
        updateSpeedLabel();

        Simulation s = this;
        timer = new Timer(1000 / this.fps, e -> {
           s.nextFrame();
        });
        timer.setDelay(1000 / this.fps);
    }

    public void setCell(int x, int y, Lifeform life, boolean force) {
        if (x < 0 || y < 0 || x >= data[0].length || y >= data.length) return;
        data[y][x].setCell(life, force);
    }

    public void nextFrame() {
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                data[i][j].updateCalculate();

        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                data[i][j].updateCommit();

        canvas.repaint();
    }

    public void play() {
        if (this.running) return;
        this.running = true;
        this.timer.start();
        MainFrame.getInstance().updatePlayPause(this.running);
    }
    public void pause() {
        if (!this.running) return;
        this.running = false;
        this.timer.stop();
        MainFrame.getInstance().updatePlayPause(this.running);
    }
    public void togglePlayPause() {
        if (this.running) pause();
        else play();
    }
    public void kill() {
        this.running = false;
        this.timer.stop();
    }

    public void setFps(int fps) {
        this.fps = fps;
        updateSpeedLabel();
        boolean restart = false;
        if (this.running) { pause(); restart = true; }
        timer.setDelay(1000 / this.fps);
        if (restart) play();
    }
    public void setFps(String fps) {
        int f = -1;
        try {
            f = Integer.parseInt(fps);
        } catch (NumberFormatException ignored) {}

        if (f > 0 && f <= 1000) setFps(f);
        else updateSpeedLabel();
    }

    private static final int[] speeds = new int[] {10, 50, 250};
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
    private void updateSpeedLabel() {
        MainFrame.getInstance().updateSpeedLabel(this.fps + "");
    }

    public int getFps() {return this.fps;}
    public boolean getRunning() {return  this.running;}
    public int getHeight() { return height; }
    public int getWidth() { return width; }
    public Cell[][] getData() { return data; }
    public Color getColor(int x, int y) {
        if (x >= width || x < 0 || y >= height || y < 0) return Color.BLACK;
        return data[y][x].getColor();
    }
}
