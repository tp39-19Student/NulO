package life;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LifeformLifelike extends Lifeform {
    public static Pattern rulePattern = Pattern.compile("B?([0-8]*)/S?([0-8]*)", Pattern.CASE_INSENSITIVE);

    @Override
    protected NeighbourhoodType getNeighbourhoodType() {
        return NeighbourhoodType.NEIGHBOURHOOD_MOORE;
    }

    //Rulestring: "B0..8/S0..8"
    //Neighbours: 3x3 Grid
    //Single state
    protected LifeformLifelike(String name, String ruleString, Color color) {
        super(name, ruleString, color);
    }

    @Override
    protected void initRules(String ruleString) {
        this.range = 1;
        this.states = 1;
        boolean sbNotation = ruleString.charAt(0) != 'B';
        Matcher m = rulePattern.matcher(ruleString);

        if (m.matches()) {
            String group1 = sbNotation?m.group(2):m.group(1);
            String group2 = sbNotation?m.group(1):m.group(2);

            this.sRules = new boolean[10];
            this.bRules = new boolean[10];

            for (int i = 0; i < group1.length(); i++) bRules[group1.charAt(i) - '0'] = true;
            for (int i = 0; i < group2.length(); i++) sRules[group2.charAt(i) - '0'] = true;

            this.ruleString = "B" + m.group(1) + "/S" + m.group(2);
        } else {
            this.ruleString = "";
        }
    }

    @Override
    protected void standardizeRulestring() {
        StringBuilder str = new StringBuilder();

        str.append("B");
        for (int i = 0; i < 10; i++) {if (bRules[i]) str.append(i);}

        str.append("/");

        str.append("S");
        for (int i = 0; i < 10; i++) {if (sRules[i]) str.append(i);}

        this.ruleString = str.toString();
    }
}
