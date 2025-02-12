package logic;

import life.Lifeform;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Cell
{

    private final int x;
    private final int y;
    private Lifeform life;

    private Lifeform nextLife;

    private boolean dirty;

    private Cell[][] neighbourRings;

    public Cell(int x, int y) {
        this(x, y, null);
    }

    public Cell(int x, int y, Lifeform life) {
        this.x = x;
        this.y = y;
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
            int neighbourCount = 0;
            for (int i = 0; i < this.life.getRange(); i++) {
                Cell[] ring = this.neighbourRings[i];

                for (Cell neighbour: ring) {
                    if (neighbour.life != null) neighbourCount++;
                }
            }

            if (!this.life.s(neighbourCount)) {
                this.nextLife = null;
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
                for (Cell neighbour : ring) if (neighbour.life != null) neighbourCounts[i]++;
            }

            for (Map.Entry<Lifeform, Integer> mate : mates) {
                Lifeform l = mate.getKey();
                if (l.b(neighbourCounts[l.getRange() - 1])) {
                    this.nextLife = l;
                    return;
                }
            }
        }
    }

    public void updateCommit() {
        setCell(this.nextLife);
    }

    public void setCell(Lifeform life) {
        if (life != this.life) {
            this.life = life;
            this.nextLife = life;
            this.dirty = true;
            notifyNeighbours();
        }
    }

    private List<Map.Entry<Lifeform, Integer>> findMates() {
        HashMap<Lifeform, Integer> map = new HashMap<>();
        for (int i = 0; i < Lifeform.MAX_RANGE; i++) {
            Cell[] ring = this.neighbourRings[i];
            for (Cell neighbour : ring) {
                Lifeform neighbourLife = neighbour.life;
                if (neighbourLife != null && neighbourLife.getRange() > i) {
                    map.put(neighbourLife, map.getOrDefault(neighbourLife, 0) + 1);
                }
            }
        }

        return map.entrySet().stream().sorted((e1, e2)-> {return e1.getValue() - e2.getValue();}).collect(Collectors.toList());
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
        if (this.life == null) return Color.BLACK;
        return this.life.getColor();
    }
}
