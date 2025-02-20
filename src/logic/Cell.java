package logic;

import life.Lifeform;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cell
{

    private final int x;
    private final int y;
    private Lifeform life;
    private int state;
    private int nextState;

    private Lifeform nextLife;

    private final int drawSize;
    private final int drawX;
    private final int drawY;


    private Color nextColor;
    private boolean dirty;

    private final Cell[][] neighbourRings;

    private int change;
    private final int[] livingNeighbourCounts;

    public Cell(int x, int y, int pixelsize) {
        this(x, y, pixelsize, null);
    }

    public Cell(int x, int y, int pixelsize, Lifeform life) {
        this.x = x;
        this.y = y;

        this.drawSize = pixelsize;
        this.drawX = this.x * this.drawSize;
        this.drawY = this.y * this.drawSize;

        this.state = 0;
        this.nextState = 0;
        this.life = life;
        this.dirty = false;
        this.nextColor = Color.BLACK;

        this.neighbourRings = new Cell[Lifeform.MAX_RANGE][];

        this.livingNeighbourCounts = new int[Lifeform.MAX_RANGE];

        for (int i = 0; i < Lifeform.MAX_RANGE; i++) {
            this.neighbourRings[i] = new Cell[8*(i+1)];
        }
    }

    private int mod(int value, int max) {
        if (value >= max) return value - max;
        if (value < 0) return value + max;
        else return value;
    }

    public void initNeighbourRings(Cell[][] data) {
        int width = data[0].length;
        int height = data.length;

        int[] ringIndexes = new int[Lifeform.MAX_RANGE];

        int dy = -Lifeform.MAX_RANGE;
        for (int i = mod((this.y - Lifeform.MAX_RANGE), height); i != mod((this.y + Lifeform.MAX_RANGE + 1), height); i = mod((i+1), height)) {
            int dx = -Lifeform.MAX_RANGE;
            for (int j = mod((this.x - Lifeform.MAX_RANGE), width); j != mod((this.x + Lifeform.MAX_RANGE + 1), width); j = mod((j + 1), width)) {
                int range = Math.max(Math.abs(dx), Math.abs(dy));
                if (range != 0) this.neighbourRings[(range - 1)][ringIndexes[(range) - 1]++] = data[i][j];
                dx++;
            }
            dy++;
        }
    }

    public void updateCalculate() {
        if (!this.dirty) return;
        this.dirty = false;

        // S Logic
        if (this.life != null) {
            if (this.state > 1) {
                this.nextState = (this.state + 1) % (this.life.getStates() + 1);
                return;
            }

            int neighbourCount = 0;
            for (int i = 0; i < this.life.getRange(); i++) {
                neighbourCount += livingNeighbourCounts[i];
            }
            if (!this.life.s(neighbourCount)) {
                this.change = -1;
                this.nextState = (this.state + 1) % (this.life.getStates() + 1);
            }
        }

        // B Logic
        else {
            List<Map.Entry<Lifeform, Integer>> mates = findMates();
            if (mates.isEmpty()) return;
            int[] neighbourCounts = new int[Lifeform.MAX_RANGE];

            for (int i = 0; i < Lifeform.MAX_RANGE; i++) {
                if (i != 0) neighbourCounts[i] = neighbourCounts[i-1];
                neighbourCounts[i] += livingNeighbourCounts[i];
            }

            for (Map.Entry<Lifeform, Integer> mate : mates) {
                Lifeform l = mate.getKey();
                if (l.b(neighbourCounts[l.getRange() - 1])) {
                    this.nextLife = l;
                    this.change = 1;
                    return;
                }
            }
        }
    }

    public void updateCalculateOld() {
        if (!this.dirty) return;
        this.dirty = false;

        // S Logic
        if (this.life != null) {
            if (this.state > 1) {
                this.nextState = (this.state + 1) % (this.life.getStates() + 1);
                return;
            }

            int neighbourCount = 0;
            int neighbourCountTestSum = 0;
            for (int i = 0; i < this.life.getRange(); i++) {
                neighbourCountTestSum += livingNeighbourCounts[i];
                Cell[] ring = this.neighbourRings[i];
                for (Cell neighbour: ring) {
                    if (neighbour.life != null && neighbour.state == 1) neighbourCount++;
                }
            }
            if (neighbourCount != neighbourCountTestSum) {
                System.out.println("MISMATCH");
            }
            if (!this.life.s(neighbourCount)) {
                this.change = -1;
                this.nextState = (this.state + 1) % (this.life.getStates() + 1);
            }
        }

        // B Logic
        else {
            List<Map.Entry<Lifeform, Integer>> mates = findMates();
            if (mates.isEmpty()) return;
            int[] neighbourCounts = new int[Lifeform.MAX_RANGE];
            int[] neighbourCountTestSum = new int[Lifeform.MAX_RANGE];


            for (int i = 0; i < Lifeform.MAX_RANGE; i++) {
                if (i != 0) neighbourCounts[i] = neighbourCounts[i-1];
                if (i != 0) neighbourCountTestSum[i] = neighbourCountTestSum[i-1];
                neighbourCountTestSum[i] += livingNeighbourCounts[i];

                Cell[] ring = neighbourRings[i];
                for (Cell neighbour : ring) if (neighbour.life != null && neighbour.state == 1) neighbourCounts[i]++;
            }
            for (int i = 0; i < Lifeform.MAX_RANGE; i++) {
                if (neighbourCounts[i] != neighbourCountTestSum[i]) {
                    System.out.println("MISMATCH");
                }
            }

            for (Map.Entry<Lifeform, Integer> mate : mates) {
                Lifeform l = mate.getKey();
                if (l.b(neighbourCounts[l.getRange() - 1])) {
                    this.nextLife = l;
                    //this.nextState = 1;
                    this.change = 1;
                    return;
                }
            }
        }
    }

    public void updateCommit() { setCell(); }

    private void setCell() {
        if (nextLife != null || nextState != this.state) {
            this.dirty = true;

            if (nextLife != null) {
                this.state = this.nextState = 1;
                this.life = nextLife;
                this.nextLife = null;
            } else {
                this.state = this.nextState;
                if (this.state == 0) { this.life = null; }
            }

            notifyNeighbours(this.change);
            this.change = 0;
            this.nextColor = getColor();
        }
    }

    public void setCell(Lifeform life) {
        setCell(life, false);
    }
    public void setCell(Lifeform life, boolean force) {
        if (!force) {
            if (life != null && this.life != null) return;
            if (life == this.life) return;
        }
        this.nextLife = life;
        if (life == null) {
            this.nextState = 0;
            this.change = (this.state == 1)?-1:0;
        } else this.change = ((this.state != 1)?1:0);
        setCell();
    }

    private List<Map.Entry<Lifeform, Integer>> findMates() {
        HashMap<Lifeform, Integer> map = new HashMap<>();
        for (int i = 0; i < Lifeform.MAX_RANGE; i++) {
            Cell[] ring = this.neighbourRings[i];
            for (Cell neighbour : ring) {
                Lifeform neighbourLife = neighbour.life;
                if (neighbourLife != null && neighbourLife.getRange() > i && neighbour.state == 1) {
                    map.put(neighbourLife, map.getOrDefault(neighbourLife, 0) + 1);
                }
            }
        }

        List<Map.Entry<Lifeform, Integer>> res = new ArrayList<>(map.entrySet());
        res.sort((e1, e2) -> {
            int comp = e2.getValue() - e1.getValue();
            if (comp == 0) comp = e2.getKey().getId() - e1.getKey().getId();
            return comp;
        });

        return res;
    }

    private void notifyNeighbours(int change) {
        for (int i = 0; i < Lifeform.MAX_RANGE; i++) {
            Cell[] ring = this.neighbourRings[i];
            for (Cell neighbour : ring) {
                Lifeform neighbourLife = neighbour.life;
                neighbour.livingNeighbourCounts[i] += change;
                if (neighbourLife == null || neighbourLife.getRange() > i) neighbour.dirty = true;
            }
        }
    }

    public Color getColor() {
        if (this.state == 0) return Color.BLACK;
        return this.life.getColor(this.state - 1);
    }

    public void paint(Graphics g) {
        if (this.nextColor != null) {
            g.setColor(this.nextColor);
            this.nextColor = null;
            g.fillRect(drawX, drawY, drawSize, drawSize);
        }
    }

    public void repaintNextFrame() {
        this.nextColor = this.getColor();
    }

    public String cellDebug() {
        StringBuilder str = new StringBuilder();
        str.append("===== Cell (x = ").append(this.x).append(", y = ").append(this.y).append(") =====\n");
        str.append("Lifeform: ");
        if (this.life == null) str.append("None\n");
        else str.append("#").append(this.life.getId()).append(' ').append(this.life.getName()).append(" (").append(this.life.getRuleString()).append(")\n");

        str.append("== Neighbour Counts ==\n");
        int range = -1;
        int neighbourCount = 0;
        if (this.life != null) range = this.life.getRange();
        for (int i = 0; i < Lifeform.MAX_RANGE; i++) {
            neighbourCount += this.livingNeighbourCounts[i];
            if (i == (range - 1)) str.append("> ");
            str.append("Range ").append(i + 1).append(": ").append(neighbourCount).append('\n');
        }

        return str.toString();
    }

    public Lifeform getLife() { return this.life; }
}
