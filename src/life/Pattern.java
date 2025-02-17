package life;

import display.MainFrame;

import java.util.regex.Matcher;

public class Pattern {
    private static final java.util.regex.Pattern RLE_Format = java.util.regex.Pattern.compile(
            "((?:#.*\n)*)x ?= ?([0-9]+) ?, ?y ?= ?([0-9]+)(?: ?, ?rule ?= ?(.*))?\n([0-9bo$ \n]*)!"
    );

    private final int lifeformId;
    private final int width;
    private final int height;

    private final int[][] cells;

    private Pattern(int width, int height, int lifeformId, int[][] cells) {
        this.width = width;
        this.height = height;
        this.lifeformId = lifeformId;
        this.cells = cells;
    }

    public static Pattern parse(String str) {
        Matcher m;
        if ((m = RLE_Format.matcher(str)).matches()) return RLE(m);

        return null;
    }

    private static Pattern RLE(Matcher m) {
        for (int i = 0; i <= m.groupCount(); i++)
            System.out.println("Group" + i + ": " + m.group(i) + "\n");

        //#TODO: Comment Part

        Lifeform lifeform = Lifeform.create("", m.group(4)!=null?m.group(4):"B3/S23");
        if (lifeform == null) return null;
        MainFrame.getInstance().setSelectedLifeform(lifeform);

        int lifeformId = lifeform.getId();
        int width = Integer.parseInt(m.group(2));
        int height = Integer.parseInt(m.group(3));

        int[][] cells = new int[width][height];
        int cellRowCursor;

        String[] dataStrings = m.group(5).toLowerCase().replaceAll("[ \\n]", "").split("\\$");
        try {
            for (int i = 0; i < dataStrings.length; i++) {
                if (dataStrings[i].isEmpty()) continue;
                cellRowCursor = 0;
                char[] line = dataStrings[i].toCharArray();
                int lineCursor = 0;
                while (lineCursor < line.length) {
                    int count = 0;
                    while(Character.isDigit(line[lineCursor])) count = (count * 10) + (line[lineCursor++] - '0');
                    if (count == 0) count = 1;

                    //#TODO: Skip when 0, check dimensions
                    int type = -1;
                    if (line[lineCursor] == 'b') type = 0;
                    else if (line[lineCursor] == 'o') type = 1;
                    else return null;
                    lineCursor++;

                    for (int j = 0; j < count; j++) cells[cellRowCursor++][i] = type;
                }


            }
        } catch (ArrayIndexOutOfBoundsException e) {e.printStackTrace(); return null;}


        return new Pattern(width, height, lifeformId, cells);
    }
}
