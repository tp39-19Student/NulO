package logic;

import display.MainFrame;
import display.SimulationCanvas;
import life.Lifeform;

import javax.swing.Timer;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        this(width, height, canvas, console, DEFAULT_SPEED);
    }
    public Simulation(int width, int height, SimulationCanvas canvas, Console console, int fps) {
        this.width = width;
        this.height = height;
        this.canvas = canvas;
        this.console = console;

        int pixelsize = canvas.getBasePixelSize();

        this.data = new Cell[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                data[i][j] = new Cell(j, i, pixelsize);
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                data[i][j].initNeighbourRings(data);

        this.running = false;
        MainFrame.getInstance().updatePlayPause(this.running);
        this.fps = fps;
        updateSpeedLabel();

        Simulation s = this;
        timer = new Timer(1000 / this.fps, e -> {
           s.nextFrame();
        });
        timer.setDelay(1000 / this.fps);
    }

    public void setCell(int x, int y, Lifeform life, boolean force) {
        setCell(x, y, life, -1, force);
    }

    public void setCell(int x, int y, Lifeform life, int state, boolean force) {
        if (x < 0 || y < 0 || x >= data[0].length || y >= data.length) return;
        data[y][x].setCell(life, state, force);
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

    public int setFps(int fps) {
        this.fps = 1000 / (1000/fps);
        updateSpeedLabel();
        boolean restart = false;
        if (this.running) { pause(); restart = true; }
        timer.setDelay(1000 / this.fps);
        if (restart) play();
        return this.fps;
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

    public List<Map.Entry<Lifeform, Integer>> getCounts() {
        HashMap<Lifeform, Integer> map = new HashMap<>();
        for (Cell[] row: data)
            for (Cell cell: row) {
                Lifeform life = cell.getLife();
                if (life != null) map.put(life, map.getOrDefault(life, 0) + 1);
            }

        return map.entrySet().stream().sorted((e1, e2) -> e2.getValue() - e1.getValue()).collect(Collectors.toList());
    }

    public String cellDebug(int x, int y) { return this.data[y][x].cellDebug(); }

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
