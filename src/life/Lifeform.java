package life;

import display.MainFrame;
import java.awt.*;
import java.util.ArrayList;
import java.util.regex.Matcher;

abstract public class Lifeform {
    abstract protected void initRules(String ruleString);
    abstract protected NeighbourhoodType getNeighbourhoodType();

    public static final int MAX_RANGE = 5;

    protected enum NeighbourhoodType {
        NEIGHBOURHOOD_MOORE,
        NEIGHBOURHOOD_VON_NEUMANN
    }


    protected int id;
    protected static int idTracker = 0;
    protected static ArrayList<Lifeform> all = new ArrayList<>();

    protected String name;
    protected String ruleString;
    protected int states;
    protected int range;

    protected Color[] stateColors;

    protected boolean[] sRules;
    protected boolean[] bRules;

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
        if (this.states == 1) return;

        int red = baseColor.getRed();
        int green = baseColor.getGreen();
        int blue = baseColor.getBlue();

        int total = red + green + blue;

        int min = total / this.states;
        int step = (total - min) / (this.states - 1);

        double factor;
        for (int i = 1; i < this.states; i++) {
            factor = (total - i*step) * 1.0 / total;
            Color newColor =  new Color((int)(red*factor), (int)(green*factor), (int)(blue*factor));
            stateColors[i] = newColor;
        }
    }

    public static Lifeform getById(int id) {
        if (id < 0 || id >= all.size()) return null;
        return all.get(id);
    }

    public boolean s(int neighbours) {
        return sRules[neighbours];
    }

    public boolean b(int neighbours) {
        return bRules[neighbours];
    }

    public int getId() { return this.id; }
    public String getName() { return this.name; }
    public int getRange() { return this.range; }
    public String getRuleString() { return this.ruleString; }
    public static Lifeform[] getAll() { return all.toArray(new Lifeform[0]); }
    public int getStates() { return this.states; }

    public Color getColor(int state) { return this.stateColors[state]; }

    @Override
    public String toString() {
        return "#" + this.id + ' ' + this.name + ": " + this.ruleString;
    }

    public static Lifeform create(String name, String ruleString, Color color) {
        ruleString = ruleString.replace(" ", "");
        String ruleStringNoCommas = ruleString.replace(",", "");
        Matcher m;

        if ((m = LifeformLifelike.rulePattern.matcher(ruleStringNoCommas)).matches())
            return new LifeformLifelike(name, ruleStringNoCommas, color);
        if ((m = LifeformGenerations.rulePattern.matcher(ruleStringNoCommas)).matches()) {
            if (Integer.parseInt(m.group(3)) <= 2)
                return new LifeformLifelike(name, ruleStringNoCommas.substring(0, ruleStringNoCommas.lastIndexOf('/')), color);
                else return new LifeformGenerations(name, ruleStringNoCommas, color);
        }
        if ((m = LifeformLTL.rulePattern.matcher(ruleString)).matches()) {
            return new LifeformLTL(name, ruleString, color);
        }


        return null;
    }

    public static Lifeform GOL = Lifeform.create("Conway's Life", "B3/S23", new Color(0, 124, 0));
    public static Lifeform PEDL = Lifeform.create("Pedestrian Life", "B38/S23", new Color(116, 116, 116));
    public static Lifeform DAN = Lifeform.create("Day and Night", "B3678/S34678", new Color(218, 137, 86));
    public static Lifeform FLCK = Lifeform.create("Flock", "B3/S12", new Color(210, 194, 59));

    public static Lifeform CLRT = Lifeform.create("Color test", "B278/S3456/5", new Color(73, 1, 1));

    public static Lifeform LWD = Lifeform.create("Life without Death", "B3/S012345678", new Color(96, 8, 83));
    public static Lifeform STW6 = Lifeform.create("Like Starwars", "B278/S3456/6", new Color(57, 122, 230));
    public static Lifeform FRWK = Lifeform.create("Fireworks", "B13/S2/C21", new Color(163, 29, 244));


    public static Lifeform BUGS = Lifeform.create("Bosco's Rule", "R5,C2,S33-57,B34-45", new Color(237, 95, 34));
    public static Lifeform MAJ = Lifeform.create("Majority", "R4,C2,S40-80,B41-81", new Color(211, 211, 211));

    public static Lifeform FLME = Lifeform.create("Flame", "R2, C5, S8-10, B5-7", new Color(255, 228, 78));
}
