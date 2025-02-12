package life;

import java.awt.*;
import java.util.ArrayList;

abstract public class Lifeform {
    public static final int MAX_RANGE = 3;

    protected int id;
    protected static int idTracker = 0;
    protected static ArrayList<Lifeform> all = new ArrayList<>();
    protected String name;
    protected String ruleString;

    protected Color[] stateColors;
    protected int stateCount;
    protected int range;

    public Lifeform(String name, String rulestring, Color[] stateColors, int range) {
        this.name = name;
        this.stateColors = stateColors;
        this.stateCount = stateColors.length;
        this.range = range;
        this.initRules(rulestring);
        this.id = idTracker++;
        all.add(this);
    }


    public static Lifeform getById(int id) {
        //if (id < 0 || id >= all.size()) return null;
        return all.get(id);
    }

    public Color getColor() { return this.getColor(0); }
    public Color getColor(int state) { return this.stateColors[state]; }

    public String getName() { return this.name; }
    public int getRange() { return this.range; }
    public String getRuleString() { return this.ruleString; }
    public static Lifeform[] getAll() { return all.toArray(new Lifeform[0]); }

    abstract public boolean s(int neighbours);
    abstract public boolean b(int neighbours);

    abstract protected void initRules(String ruleString);

    @Override
    public String toString() {
        return "#" + this.id + " - " + this.name + '\n' +
                this.ruleString;
    }

    public String getTitle() {
        return "#" + this.id + " - " + this.name;
    }
}
