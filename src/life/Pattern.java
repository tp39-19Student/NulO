package life;

import display.MainFrame;
import display.SimulationCanvas;
import logic.Simulation;

import java.awt.*;
import java.util.Arrays;
import java.util.regex.Matcher;

public class Pattern {
    private static final java.util.regex.Pattern RLE_Format = java.util.regex.Pattern.compile(
            "((?:#.*\\n)*)x ?= ?([0-9]+) ?, ?y ?= ?([0-9]+)(?: ?, ?rule ?= ?(.*))?\\n([0-9bo$ \\n]*)!"
    );
    private static final java.util.regex.Pattern PLAINTEXT_Format = java.util.regex.Pattern.compile(
            "((?:!.*\\n)*)((?:.*\\n?)+)"
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
        if ((m = RLE_Format.matcher(str)).matches()) return decodeRLE(m);
        else if ((m = PLAINTEXT_Format.matcher(str)).matches()) return decodePlaintext(m);

        return null;
    }

    private static Pattern decodeRLE(Matcher m) {
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

    private static Pattern decodePlaintext(Matcher m) {
        //for (int i = 0; i <= m.groupCount(); i++) System.out.println("Group" + i + ": " + m.group(i) + "\n");
        //#TODO: Comment Part

        //Can this even specify other rules?
        Lifeform lifeform = Lifeform.GOL;
        MainFrame.getInstance().setSelectedLifeform(lifeform);

        int lifeformId = lifeform.getId();

        String[] data = m.group(2).split("\n");
        int height = data.length;
        int width = Arrays.stream(data).mapToInt(String::length).max().orElse(0);
        if (height == 0 || width == 0) return null;
        int[][] cells = new int[height][width];

        for (int i = 0; i < height; i++) {
            String line = data[i];
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) != '.') cells[i][j] = 1;
            }
        }

        return new Pattern(width, height, lifeformId, cells);
    }


    public void drawPreview(Graphics g, int x, int y) {
        Color alive = (Lifeform.getById(this.lifeformId).getColor());
        Color dead = Color.BLACK;

        Simulation sim = MainFrame.getInstance().getSimulation();
        SimulationCanvas canvas = MainFrame.getInstance().getCanvas();

        int pixelsize = canvas.getPixelSize();
        int simWidth = sim.getWidth();
        int simHeight = sim.getHeight();

        int drawXStart = canvas.getDrawXStart();
        int drawYStart = canvas.getDrawYStart();

        for (int i = 0; i < this.height; i++)
            for (int j = 0; j < this.width; j++) {
                if (cells[i][j] == 1){
                    g.setColor(alive);
                }
                g.setColor((cells[i][j] == 1)?alive:dead);
                g.fillRect((x + j - drawXStart)*pixelsize, (y + i - drawYStart)*pixelsize, pixelsize, pixelsize);
        }

        if ((x + this.width) > simWidth || (y + this.height) > simHeight || x < 0 || y < 0)
            g.setColor(Color.RED);
        else
            g.setColor(Color.LIGHT_GRAY);
        g.drawRect((x - drawXStart)*pixelsize, (y - drawYStart)*pixelsize, this.width * pixelsize - 1, this.height*pixelsize - 1);
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
