package logic;

import display.SimulationCanvas;
import life.Lifeform;
import life.LifeformLifelike;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

public class Simulation
{
    private static final int defaultSpeed = 45; //FPS
    public static Lifeform test = new LifeformLifelike("test", "B3/S23", new Color(0, 124, 0));

    private final int width;
    private final int height;
    private boolean running;
    private int fps;

    private Timer timer;
    private Lifeform[][] data;

    private final SimulationCanvas canvas;
    private final Console console;

    //private HashMap<Lifeform, Integer> lifeformCount;

    public Simulation(int width, int height, SimulationCanvas canvas, Console console) {
        this.width = width;
        this.height = height;
        this.canvas = canvas;
        this.console = console;
        this.data = new Lifeform[width][height];
        /*
        this.lifeformCount = new HashMap<>();
        for (Lifeform l : Lifeform.getAll()) {
            lifeformCount.put(l, 0);
        }
        */


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

    public Lifeform[][] getData() {
        return data;
    }

    public void setCell(int x, int y, Lifeform life) {
        if (x < 0 || y < 0 || x >= data.length || y >= data[0].length) return;
        //if (data[x][y] != test) System.out.println("Drawing coordinates: " + x + ", " + y);
        //Lifeform current = data[x][y];
        if (life == data[x][y]) return;

        //if (life == null) lifeformCount.put(current, lifeformCount.get(current) - 1);
        //else lifeformCount.put(life, lifeformCount.get(life) + 1);

        data[x][y] = life;
    }

    public void nextFrame() {
        Lifeform[][] newdata = new Lifeform[width][height];
        int scanRange = 0;
        Set<Map.Entry<Lifeform, Integer>> living = this.getCount();
        if (living.isEmpty()) return;
        for (Map.Entry<Lifeform, Integer> e : getCount()) {
            if (e.getKey().getRange() > scanRange) scanRange = e.getKey().getRange();
        }

        //For now, assuming range always 1
        for (int x = 0; x < this.width; x++)
            for (int y = 0; y < this.height; y++) {
                List<Map.Entry<Lifeform,Integer>> neighbours = scan(x, y, 1);
                Lifeform l = data[x][y];
                int count = neighbours.stream().mapToInt(Map.Entry::getValue).sum();
                if (l != null) {
                    if (!l.s(count)) {
                        newdata[x][y] = null;
                        //lifeformCount.put(l, lifeformCount.get(l) - 1);
                    }
                    else newdata[x][y] = l;
                } else {
                    newdata[x][y] = null;
                    for (Map.Entry<Lifeform, Integer> e : neighbours) {
                        Lifeform candidate = e.getKey();
                        if (candidate.b(count)) {
                            newdata[x][y] = candidate;
                            //lifeformCount.put(candidate, lifeformCount.get(candidate) + 1);
                            break;
                        }
                    }
                }
            }

        data = newdata;
        canvas.repaint();
    }

    private int limitWrap(int value, int limit) {
        if (value >= limit) return value - limit;
        if (value < 0) return value + limit;
        else return value;
    }

    public List<Map.Entry<Lifeform, Integer>> scan(int x, int y, int range) {
        range = 1;
        HashMap<Lifeform, Integer> ret = new HashMap<>();
        for (int i = limitWrap(x-range, width); i != limitWrap(x+range+1, width); i = limitWrap(i+1, width))
            for (int j = limitWrap(y-range, height); j != limitWrap(y+range+1, height); j = limitWrap(j+1, height)) {
                if (i == x && j == y) continue;
                Lifeform l = data[i][j];
                if (l == null) continue;
                if (ret.containsKey(l)) {
                    ret.put(l, ret.get(l) + 1);
                } else {
                    ret.put(l, 1);
                }
            }
        return ret.entrySet().stream().sorted().collect(Collectors.toList());
    }

    public Set<Map.Entry<Lifeform, Integer>> getCount() {
        HashMap<Lifeform, Integer> counts = new HashMap<>();
        for (Lifeform l : Lifeform.getAll()) {
            counts.put(l, 0);
        }

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                Lifeform l = data[x][y];
                if (l != null) counts.put(l, counts.get(l) + 1);
            }

        return counts.entrySet().stream().filter(e -> e.getValue() != 0).collect(Collectors.toSet());
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
        for (int i = 0; i < speeds.length; i++) if (fps < speeds[i]) {
            console.command("fps " + speeds[i]);
            return;
        }
        console.command("fps " + speeds[0]);
    }

    public int getFps() {return this.fps;}
    public Lifeform getCell(int x, int y) {return data[x][y];}

    public String cellDebug(int x, int y) {
        Lifeform cellLife = data[x][y];
        List<Map.Entry<Lifeform, Integer>> res = scan(x, y, 1);
        StringBuilder str = new StringBuilder();
        str.append("===== Data for cell (").append(x).append(", ").append(y).append("):\n");
        str.append("Cell life: ").append(cellLife!=null?cellLife.toString():"None").append("\nNeighbours:\n");
        for (Map.Entry<Lifeform, Integer> entry : res) {
            str.append(entry.getKey().getTitle()).append(" - Count: ").append(entry.getValue()).append("\n");
        }
        str.append("Total neighbours: ").append(res.stream().mapToInt(Map.Entry::getValue).sum());
        return str.toString();
    }
}
