package life;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LifeformGenerations extends Lifeform {
    public static Pattern rulePattern = Pattern.compile("B([0-8]*)/S([0-8]*)/C?([0-9]*)", Pattern.CASE_INSENSITIVE);

    @Override
    protected NeighbourhoodType getNeighbourhoodType() {
        return NeighbourhoodType.NEIGHBOURHOOD_MOORE;
    }

    //Rulestring: "B0..8/S0..8/3.."
    //Neighbours: 3x3 Grid
    //Multi state
    protected LifeformGenerations(String name, String ruleString, Color color) {
        super(name, ruleString, color);
    }

    @Override
    protected void initRules(String ruleString) {
        this.range = 1;
        Matcher m = rulePattern.matcher(ruleString);

        if (m.matches()) {
            String group1 = m.group(1);
            String group2 = m.group(2);
            String group3 = m.group(3);

            this.sRules = new boolean[10];
            this.bRules = new boolean[10];

            for (int i = 0; i < group1.length(); i++) bRules[group1.charAt(i) - '0'] = true;
            for (int i = 0; i < group2.length(); i++) sRules[group2.charAt(i) - '0'] = true;
            this.states = Integer.parseInt(group3) - 1;
            if (this.states < 1) this.states = 1;

            this.ruleString = "B" + m.group(1) + "/S" + m.group(2) + "/" + (this.states + 1);
        } else {
            this.ruleString = "INVALID";
        }
    }
}
