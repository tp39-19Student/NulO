package life;

import display.MainFrame;
import java.awt.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
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
    protected static LinkedHashMap<Integer, Lifeform> all = new LinkedHashMap<>();

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
        this.id = generateId(this.ruleString);
        if (color == null) color = colorFromId();
        initColors(color);

        if (all.containsKey(this.id)) {
            if (!this.name.isEmpty()) all.get(this.id).setName(this.name);
        } else all.put(this.id, this);
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
    public static Lifeform[] getAll() { return all.values().toArray(new Lifeform[0]); }
    public int getStates() { return this.states; }

    public Color getColor(int state) { return this.stateColors[state]; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "<html><font color = '"+ baseColorRGB() +"'>██</font><font color = 'aqua'>" + ' ' + ((!this.name.isEmpty())?this.name:("Imported (#" + this.id + ")")) + ":</font> <font color='silver'>" + this.ruleString + "</font></html>";
    }

    private String baseColorRGB() {
        Color baseColor = this.stateColors[0];
        return "rgb(" + baseColor.getRed() + ", " + baseColor.getGreen() + ", " + baseColor.getBlue() + ")";
    }

    public static Lifeform create(String name, String ruleString) {
        return create(name, ruleString, null);
    }

    public static Lifeform create(String name, String ruleString, Color color) {
        ruleString = ruleString.replace(" ", "");
        String ruleStringNoCommas = ruleString.replace(",", "");
        Matcher m;

        Lifeform res;

        if ((m = LifeformLifelike.rulePattern.matcher(ruleStringNoCommas)).matches()){
            res = new LifeformLifelike(name, ruleStringNoCommas, color);
        }

        else if ((m = LifeformGenerations.rulePattern.matcher(ruleStringNoCommas)).matches()) {
            if (Integer.parseInt(m.group(3)) <= 2)
                res = new LifeformLifelike(name, ruleStringNoCommas.substring(0, ruleStringNoCommas.lastIndexOf('/')), color);
            else
                res = new LifeformGenerations(name, ruleStringNoCommas, color);
        }

        else if ((m = LifeformLTL.rulePattern.matcher(ruleString)).matches()) {
            res = new LifeformLTL(name, ruleString, color);
        }

        else return null;
        return  (res.ruleString.isEmpty())?null:res;
    }

    private static int generateId(String ruleString) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(ruleString.getBytes());
            return Math.abs(new BigInteger(hash).intValue());
        } catch (NoSuchAlgorithmException ignored) {}
        return -1;
    }

    private Color colorFromId() {
        int red = (this.id >> 24) & 0xFF;
        int green = (this.id >> 16) & 0xFF;
        int blue = (this.id >> 8) & 0xFF;

        if (red == 0 && green == 0 && blue == 0) red = green = blue = 1;

        double lum = 0.299 * red + 0.587 * green + 0.114*blue; // 0 - 255
        double factor = -1;

        if (lum < 80) factor = 80/lum;
        else if (lum > 230) factor = 230/lum;

        if (factor > 0) {
            red = (int)(red*factor);
            blue = (int)(blue*factor);
            green = (int)(green*factor);

            if (red > 255) red = 255;
            if (blue > 255) blue = 255;
            if (green > 255) green = 255;
        }

        return new Color(red, green, blue);
    }

    public static Lifeform GOL = Lifeform.create("Conway's Life", "B3/S23");
    public static Lifeform PEDL = Lifeform.create("Pedestrian Life", "B38/S23");
    public static Lifeform DAN = Lifeform.create("Day and Night", "B3678/S34678");
    public static Lifeform FLCK = Lifeform.create("Flock", "B3/S12");
    public static Lifeform LWD = Lifeform.create("Life without Death", "B3/S012345678");

    public static Lifeform STW6 = Lifeform.create("Like Starwars", "B278/S3456/6");
    public static Lifeform FRWK = Lifeform.create("Fireworks", "B13/S2/C21");
    public static Lifeform FLME = Lifeform.create("Flame", "R2, C5, S8-10, B5-7");

    public static Lifeform BUGS = Lifeform.create("Bosco's Rule", "R5,C2,S33-57,B34-45");
    public static Lifeform MAJ = Lifeform.create("Majority", "R4,C2,S40-80,B41-81");

}
