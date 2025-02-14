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

    private boolean dirty;

    private final Cell[][] neighbourRings;

    public Cell(int x, int y) {
        this(x, y, null);
    }

    public Cell(int x, int y, Lifeform life) {
        this.x = x;
        this.y = y;
        this.state = 0;
        this.nextState = 0;
        this.life = life;
        this.dirty = false;

        this.neighbourRings = new Cell[Lifeform.MAX_RANGE][];

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
        int width = data.length;
        int height = data[0].length;

        int[] ringIndexes = new int[Lifeform.MAX_RANGE];

        int dx = -Lifeform.MAX_RANGE;
        for (int i = mod((this.x - Lifeform.MAX_RANGE), width); i != mod((this.x + Lifeform.MAX_RANGE + 1), width); i = mod((i+1), width)) {
            int dy = -Lifeform.MAX_RANGE;
            for (int j = mod((this.y - Lifeform.MAX_RANGE), height); j != mod((this.y + Lifeform.MAX_RANGE + 1), height); j = mod((j + 1), height)) {
                int range = Math.max(Math.abs(dx), Math.abs(dy));
                if (range != 0) this.neighbourRings[(range - 1)][ringIndexes[(range) - 1]++] = data[i][j];
                dy++;
            }
            dx++;
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
                Cell[] ring = this.neighbourRings[i];
                for (Cell neighbour: ring) {
                    if (neighbour.life != null && neighbour.state == 1) neighbourCount++;
                }
            }
            if (!this.life.s(neighbourCount)) {
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
                Cell[] ring = neighbourRings[i];
                for (Cell neighbour : ring) if (neighbour.life != null && neighbour.state == 1) neighbourCounts[i]++;
            }
            for (Map.Entry<Lifeform, Integer> mate : mates) {
                Lifeform l = mate.getKey();
                if (l.b(neighbourCounts[l.getRange() - 1])) {
                    this.nextLife = l;
                    this.nextState = 1;
                    return;
                }
            }
        }
    }

    public void updateCommit() { setCell(); }

    private void setCell() {
        if (nextLife != this.life || nextState != this.state) {
            this.state = nextState;
            if (this.state == 0) this.life = null;
            else  this.life = nextLife;
            this.dirty = true;
            notifyNeighbours();
        }
    }
    public void setCell(Lifeform life) {
        this.nextLife = life;
        if (life != null) this.nextState = 1;
        else this.nextState = 0;
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
            if (comp == 0) comp = e2.getKey().getId() - e1.getKey().getId(); //#TODO
            return comp;
        });

        return res;
    }

    private void notifyNeighbours() {
        for (int i = 0; i < Lifeform.MAX_RANGE; i++) {
            Cell[] ring = this.neighbourRings[i];
            for (Cell neighbour : ring) {
                Lifeform neighbourLife = neighbour.life;
                if (neighbourLife == null || neighbourLife.getRange() > i) neighbour.dirty = true;
            }
        }
    }

    public Color getColor() {
        if (this.state == 0) return Color.BLACK;
        try {
            return this.life.getColor(this.state - 1);
        }
        catch (NullPointerException e) {
            return Color.BLACK;
        }
    }
}
