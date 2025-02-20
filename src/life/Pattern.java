package life;

import display.MainFrame;
import logic.Simulation;

import java.awt.*;
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
        str = str.trim();
        if ((m = RLE_Format.matcher(str)).matches()) {
            return RLE(m);
        }

        return null;
    }

    private static Pattern RLE(Matcher m) {
        //for (int i = 0; i <= m.groupCount(); i++) System.out.println("Group" + i + ": " + m.group(i) + "\n");

        //#TODO: Comment Part

        Lifeform lifeform = Lifeform.create("", m.group(4)!=null?m.group(4):"B3/S23");
        if (lifeform == null) return null;
        MainFrame.getInstance().setSelectedLifeform(lifeform);

        int lifeformId = lifeform.getId();
        int width = Integer.parseInt(m.group(2));
        int height = Integer.parseInt(m.group(3));

        int[][] cells = new int[height][width];
        int cellXCursor;
        int cellYCursor = 0;

        String[] dataStrings = m.group(5).toLowerCase().replaceAll("[ \\n]", "").split("\\$");
        try {
            for (String ds : dataStrings) {
                if (ds.isEmpty()) return null;
                char[] line = ds.toCharArray();
                cellXCursor = 0;
                int lineCursor = 0;
                while (lineCursor < line.length) {
                    int count = 0;
                    while(lineCursor < line.length && Character.isDigit(line[lineCursor])) count = (count * 10) + (line[lineCursor++] - '0');
                    if (lineCursor == line.length) {
                        cellYCursor += (count - 1);
                        break;
                    }
                    if (count == 0) count = 1;

                    //#TODO: Skip when 0, check dimensions
                    int type;
                    if (line[lineCursor] == 'b') type = 0;
                    else if (line[lineCursor] == 'o') type = 1;
                    else return null;
                    lineCursor++;

                    for (int j = 0; j < count; j++) cells[cellYCursor][cellXCursor++] = type;
                }
                cellYCursor++;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }


        return new Pattern(width, height, lifeformId, cells);
    }


    public void drawPreview(Graphics g, int x, int y) {
        Color alive = (Lifeform.getById(this.lifeformId).getColor());
        Color dead = Color.BLACK;

        Simulation sim = MainFrame.getInstance().getSimulation();

        int pixelsize = MainFrame.getInstance().getCanvas().getPixelSize();
        int simWidth = sim.getWidth();
        int simHeight = sim.getHeight();

        for (int i = 0; i < this.height; i++)
            for (int j = 0; j < this.width; j++) {
                if (cells[i][j] == 1){
                    g.setColor(alive);
                }
                g.setColor((cells[i][j] == 1)?alive:dead);
                g.fillRect((x + j)*pixelsize, (y + i)*pixelsize, pixelsize, pixelsize);
        }

        if ((x + this.width) > simWidth || (y + this.height) > simHeight || x < 0 || y < 0)
            g.setColor(Color.RED);
        else
            g.setColor(Color.LIGHT_GRAY);
        g.drawRect(x*pixelsize, y*pixelsize, this.width * pixelsize - 1, this.height*pixelsize - 1);
    }

    public void place(int x, int y) {
        Simulation sim = MainFrame.getInstance().getSimulation();
        Lifeform life = Lifeform.getById(this.lifeformId);

        if (x + this.width > sim.getWidth() || y + this.height > sim.getHeight() || x < 0 || y < 0) return;

        for (int i = 0; i < this.height; i++)
            for (int j = 0; j < this.width; j++)
                sim.setCell(x+j, y+i, (cells[i][j] == 1)?life:null, true);
    }

    public int getWidth() {return this.width;}
    public int getHeight() {return this.height;}
}
