package life;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LifeformLTL extends Lifeform{
    public static Pattern rulePattern = Pattern.compile("R([0-9]*),C([0-9]*),S((?:[0-9]+(?:-[0-9]+)?,?)*),B((?:[0-9]+(?:-[0-9]+)?,?)*)(N[MN])?", Pattern.CASE_INSENSITIVE);
    private NeighbourhoodType neighbourhoodType;

    @Override
    protected NeighbourhoodType getNeighbourhoodType() {
        return this.neighbourhoodType;
    }

    @Override
    protected void initRules(String ruleString) {
        Matcher m = rulePattern.matcher(ruleString);
        if (m.matches()) {
            this.range = Integer.parseInt(m.group(1));
            if (range < 0) range = 1;
            if (range > Lifeform.MAX_RANGE) range = Lifeform.MAX_RANGE;

            this.states = Integer.parseInt(m.group(2)) - 1;
            if (this.states < 1) this.states = 1;

            initSB(m.group(3));
            initSB(m.group(4));

            //#TODO
            this.neighbourhoodType = NeighbourhoodType.NEIGHBOURHOOD_MOORE;
            this.ruleString = ruleString;
        } else {
            this.ruleString = "";
        }
    }

    private void initSB(String rangeString) {
        int neighbourCount =  (2 * this.range) + 1;
        neighbourCount = (neighbourCount * neighbourCount) + 1; // +1 for self, not implemented in this type

        boolean firstPass;
        if (this.sRules == null) {
            this.sRules = new boolean[neighbourCount];
            firstPass = true;
        } else {
            this.bRules = new boolean[neighbourCount];
            firstPass = false;
        }

        for (String s : rangeString.split(",")) {
            if (s.isEmpty()) continue;
            if (s.contains("-")) {
                String[] t = s.split("-");
                int start = Integer.parseInt(t[0]);
                int end = Integer.parseInt(t[1]);
                if (start > end) {int x = start; start = end; end = x;}

                if (start < 0) start = 0;
                if (end >= neighbourCount) end = (neighbourCount - 1);

                for (int i = start; i <= end; i++) if (firstPass) {
                    this.sRules[i] = true;
                } else {
                    this.bRules[i] = true;
                }

            } else {
                int index = Integer.parseInt(s);
                if (index > 0 && index < neighbourCount) if (firstPass) {
                    this.sRules[index] = true;
                } else {
                    this.bRules[index] = true;
                }
            }
        }
    }

    @Override
    protected void standardizeRulestring() {
        StringBuilder str = new StringBuilder();

        str.append("R").append(this.range).append(",");
        str.append("C").append(this.states + 1).append(",");

        str.append("S");

        int leftCursor = 0;
        int rightCursor;
        boolean first = true;

        while (leftCursor < sRules.length) {
            while (leftCursor < sRules.length && !sRules[leftCursor]) leftCursor++;
            if (leftCursor == sRules.length) break;

            rightCursor = leftCursor + 1;
            while(rightCursor < sRules.length && sRules[rightCursor]) rightCursor++;
            rightCursor--;

            if (first) {first = false;}
            else {str.append(",");}

            if (rightCursor == leftCursor) str.append(leftCursor);
            else if (rightCursor == leftCursor + 1) str.append(leftCursor).append(",").append(rightCursor);
            else str.append(leftCursor).append("-").append(rightCursor);

            leftCursor = rightCursor + 1;
        }


        str.append(",B");
        leftCursor = 0;
        first = true;

        while (leftCursor < bRules.length) {
            while (leftCursor < bRules.length && !bRules[leftCursor]) leftCursor++;
            if (leftCursor == bRules.length) break;

            rightCursor = leftCursor + 1;
            while(rightCursor < bRules.length && bRules[rightCursor]) rightCursor++;
            rightCursor--;

            if (first) {first = false;}
            else {str.append(",");}

            if (rightCursor == leftCursor) str.append(leftCursor);
            else if (rightCursor == leftCursor + 1) str.append(leftCursor).append(",").append(rightCursor);
            else str.append(leftCursor).append("-").append(rightCursor);

            leftCursor = rightCursor + 1;
        }

        this.ruleString = str.toString();
    }

    public LifeformLTL(String name, String rulestring, Color color) {
        super(name, rulestring, color);
    }


}
