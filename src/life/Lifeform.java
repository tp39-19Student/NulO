package life;

import display.MainFrame;
import java.awt.*;
import java.util.ArrayList;

abstract public class Lifeform {
    abstract public boolean s(int neighbours);
    abstract public boolean b(int neighbours);
    abstract protected void initRules(String ruleString);

    public static final int MAX_RANGE = 3;

    protected int id;
    protected static int idTracker = 0;
    protected static ArrayList<Lifeform> all = new ArrayList<>();

    protected String name;
    protected String ruleString;
    protected int states;
    protected int range;

    protected Color[] stateColors;

    public Lifeform(String name, String rulestring, Color color) {
        this.name = name;
        this.initRules(rulestring);
        initColors(color);
        this.id = idTracker++;
        all.add(this);
        MainFrame.getInstance().updateLifeformList();
    }

    private void initColors(Color baseColor) {
        this.stateColors = new Color[this.states];
        this.stateColors[0] = baseColor;
        for (int i = 1; i < stateColors.length; i++) {
            stateColors[i] = stateColors[i-1].darker();
        }
    }

    public static Lifeform getById(int id) {
        if (id < 0 || id >= all.size()) return null;
        return all.get(id);
    }

    public int getId() { return this.id; }
    public String getName() { return this.name; }
    public int getRange() { return this.range; }
    public String getRuleString() { return this.ruleString; }
    public static Lifeform[] getAll() { return all.toArray(new Lifeform[0]); }
    public int getStates() { return this.states; }

    public Color getColor() { return this.getColor(0); }
    public Color getColor(int state) { return this.stateColors[state]; }

    @Override
    public String toString() {
        return "#" + this.id + ' ' + this.name + ": " + this.ruleString;
    }

    public static Lifeform create(String name, String ruleString, Color color) {
        if (LifeformLifelike.rulePattern.matcher(ruleString).matches())
            return new LifeformLifelike(name, ruleString, color);
        if (LifeformGenerations.rulePattern.matcher(ruleString).matches())
            return new LifeformGenerations(name, ruleString, color);

        return null;
    }
}
