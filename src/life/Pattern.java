package life;

import display.MainFrame;
import display.SimulationCanvas;
import logic.Cell;
import logic.Console;
import logic.Simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.regex.Matcher;

public class Pattern {
    static class CellState {
        Lifeform life;
        int state;
        Color color;

        CellState(Lifeform life, int state) {
            this.life = life;
            this.state = state;
            if (life != null) {
                this.color = life.getColor(state - 1);
            } else this.color = Color.BLACK;

        }

        CellState() {this(null, 0);}
        CellState(Lifeform life) {this(life, 0);}
    };


    private static final java.util.regex.Pattern RLE_Format = java.util.regex.Pattern.compile(
            "((?:#.*\\n)*)x ?= ?([0-9]+) ?, ?y ?= ?([0-9]+)(?: ?, ?rule ?= ?(.*))?\\n([0-9bo$ \\n]*)!"
    );
    private static final java.util.regex.Pattern RLE_GENERATIONS_Format = java.util.regex.Pattern.compile(
      "((?:#.*\\n)*)x ?= ?([0-9]+) ?, ?y ?= ?([0-9]+)(?: ?, ?rule ?= ?(.*))?\\n([0-9.A-Z$ \\n]*)!"
    );
    private static final java.util.regex.Pattern PLAINTEXT_Format = java.util.regex.Pattern.compile(
            "((?:!.*\\n)*)((?:.*\\n?)+)"
    );

    private final int width;
    private final int height;

    private final CellState[][] cells;
    private final int typeCount;
    private boolean generations;

    private Pattern(int width, int height, CellState[][] cells, int typeCount, boolean generations) {
        this.width = width;
        this.height = height;
        this.cells = cells;
        this.typeCount = typeCount;
        this.generations = generations;
    }


    public static Pattern parse(String str) {
        Matcher m;
        str = str.trim();
        if ((m = RLE_Format.matcher(str)).matches()) return decodeRLE(m, false);
        else if ((m = RLE_GENERATIONS_Format.matcher(str)).matches()) return decodeRLE(m, true);
        else if ((m = PLAINTEXT_Format.matcher(str)).matches()) return decodePlaintext(m);

        MainFrame.log("Unsupported format");
        return null;
    }

    public static Pattern capture(Cell[][] data, int startX, int endX, int startY, int endY) {
        //System.out.println(startX + " - " + endX + ", " + startY + " - " + endY);

        int xLeft = Math.min(startX, endX);
        int xRight = Math.max(startX, endX);

        int yUp = Math.min(startY, endY);
        int yDown = Math.max(startY, endY);

        //Trim up
        boolean emptyLine = true;
        for (int y = yUp; y < yDown; y++) {
            for (int x = xLeft; x < xRight; x++) {
                if (data[y][x].getLife() != null) {
                    emptyLine = false;
                    break;
                }
            }
            if (emptyLine) yUp++;
            else break;
        }

        //Trim down
        emptyLine = true;
        for (int y = yDown-1; y >= yUp; y--) {
            for (int x = xLeft; x < xRight; x++) {
                if (data[y][x].getLife() != null) {
                    emptyLine = false;
                    break;
                }
            }
            if (emptyLine) yDown--;
            else break;
        }

        //Trim left
        emptyLine = true;
        for (int x = xLeft; x < xRight; x++) {
            for (int y = yUp; y < yDown; y++) {
                if (data[y][x].getLife() != null) {
                    emptyLine = false;
                    break;
                }
            }
            if (emptyLine) xLeft++;
            else break;
        }

        //Trim right
        emptyLine = true;
        for (int x = xRight - 1; x >= xLeft; x--) {
            for (int y = yUp; y < yDown; y++) {
                if (data[y][x].getLife() != null) {
                    emptyLine = false;
                    break;
                }
            }
            if (emptyLine) xRight--;
            else break;
        }

        Console c =  MainFrame.getInstance().getConsole();

        if (xLeft >= xRight || yUp >= yDown) {
            c.println("Area empty");
            return null;
        }

        int typeCount = 0;
        Set<Lifeform> types = new HashSet<>();

        int width = xRight - xLeft;
        int height = yDown - yUp;

        CellState[][] cells = new CellState[height][width];

        for (int i = yUp; i < yDown; i++) {
            for (int j = xLeft; j < xRight; j++) {
                Lifeform l = data[i][j].getLife();
                int state = 0;

                if (l != null) {
                    state = data[i][j].getState();
                    if (!types.contains(l)) {
                        typeCount++;
                        types.add(l);
                    }
                }

                cells[i - yUp][j - xLeft] = new CellState(l, state);
            }
        }

        if (typeCount == 0) {
            c.println("Area empty");
            return null;
        }

        c.println("Saved pattern [" + xLeft + " - " + xRight + ", " + yUp + " - " + yDown + "]");
        c.println("Types of lifeforms in pattern: " + typeCount + ((typeCount==1)?"":" (>1, export disabled)"));

        boolean generations = false;
        for (Lifeform l: types) {
            if (l.states > 1) {generations = true; break;}
        }

        return new Pattern(width, height, cells, typeCount, generations);
    }

    private static Pattern decodeRLE(Matcher m, boolean generations) {
        //for (int i = 0; i <= m.groupCount(); i++) System.out.println("Group" + i + ": " + m.group(i) + "\n");

        Lifeform lifeform = Lifeform.create("", m.group(4)!=null?m.group(4):"B3/S23");
        if (lifeform == null) {
            MainFrame.log("Invalid rule in pattern");
            return null;
        }
        MainFrame.getInstance().setSelectedLifeform(lifeform);

        int width = Integer.parseInt(m.group(2));
        int height = Integer.parseInt(m.group(3));

        CellState[][] cells = new CellState[height][width];
        int cellXCursor;
        int cellYCursor = 0;

        String[] dataStrings = m.group(5).toLowerCase().replaceAll("[ \\n]", "").split("\\$");
        try {
            for (String ds : dataStrings) {
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

                    int type;
                    if (generations) {
                        if (line[lineCursor] == '.') type = 0;
                        else type = line[lineCursor] - 'a' + 1;
                    }
                    else {
                        if (line[lineCursor] == 'b') type = 0;
                        else if (line[lineCursor] == 'o') type = 1;
                        else {MainFrame.log("Pattern invalid"); return null;}
                    }


                    lineCursor++;

                    for (int j = 0; j < count; j++) {
                        if (type == 0) {
                            cells[cellYCursor][cellXCursor] = new CellState(null, 0);
                        } else {
                            cells[cellYCursor][cellXCursor] = new CellState(lifeform, type);
                        }
                        cellXCursor++;
                    }
                }
                while (cellXCursor < width) cells[cellYCursor][cellXCursor++] = new CellState(null, 0);
                cellYCursor++;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            MainFrame.log("Exception thrown: " + e.getMessage());
            return null;
        }


        return new Pattern(width, height, cells, 1, generations);
    }

    private static Pattern decodePlaintext(Matcher m) {
        //for (int i = 0; i <= m.groupCount(); i++) System.out.println("Group" + i + ": " + m.group(i) + "\n");

        //Can this even specify other rules?
        Lifeform lifeform = Lifeform.GOL;
        MainFrame.getInstance().setSelectedLifeform(lifeform);

        String[] data = m.group(2).split("\n");
        int height = data.length;
        int width = Arrays.stream(data).mapToInt(String::length).max().orElse(0);
        if (height == 0 || width == 0) {MainFrame.log("Empty pattern"); return null;}
        CellState[][] cells = new CellState[height][width];

        for (int i = 0; i < height; i++) {
            String line = data[i];
            int j = 0;
            for (; j < line.length(); j++) {
                if (line.charAt(j) != '.') cells[i][j] = new CellState(lifeform, 1);
                else cells[i][j] = new CellState(null, 0);
            }
            while (j < width) cells[i][j++] = new CellState(null, 0);
        }

        return new Pattern(width, height, cells, 1, false);
    }


    public void drawPreview(Graphics g, int x, int y) {
        Simulation sim = MainFrame.getInstance().getSimulation();
        SimulationCanvas canvas = MainFrame.getInstance().getCanvas();

        int pixelsize = canvas.getPixelSize();
        int simWidth = sim.getWidth();
        int simHeight = sim.getHeight();

        int drawXStart = canvas.getDrawXStart();
        int drawYStart = canvas.getDrawYStart();

        for (int i = 0; i < this.height; i++)
            for (int j = 0; j < this.width; j++) {
                g.setColor(cells[i][j].color);
                g.fillRect((x + j - drawXStart)*pixelsize, (y + i - drawYStart)*pixelsize, pixelsize, pixelsize);
        }

        if ((x + this.width) > simWidth || (y + this.height) > simHeight || x < 0 || y < 0)
            g.setColor(Color.RED);
        else
            g.setColor(Color.LIGHT_GRAY);
        g.drawRect((x - drawXStart)*pixelsize, (y - drawYStart)*pixelsize, this.width * pixelsize - 1, this.height*pixelsize - 1);
    }

    public Icon getThumbnail() {
        int pixelsize = (MainFrame.getInstance().getWidth()/4) / Math.max(this.width, this.height);
        if (pixelsize < 1) pixelsize = 1;
        if (pixelsize > 50) pixelsize = 50;

        BufferedImage img = new BufferedImage((this.width * pixelsize) + 2, (this.height * pixelsize) + 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                g.setColor(cells[i][j].color);
                g.fillRect((j*pixelsize) + 1, (i*pixelsize) + 1, pixelsize, pixelsize);
            }

        g.setColor(Color.LIGHT_GRAY);
        //g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{10f}, 0f));
        g.drawRect(0, 0, width*pixelsize + 1, height*pixelsize + 1);
        g.dispose();
        return new ImageIcon(img);
    }

    public void place(int x, int y) {
        Simulation sim = MainFrame.getInstance().getSimulation();
        if (x + this.width > sim.getWidth() || y + this.height > sim.getHeight() || x < 0 || y < 0) return;

        for (int i = 0; i < this.height; i++)
            for (int j = 0; j < this.width; j++)
                sim.setCell(x+j, y+i, cells[i][j].life, cells[i][j].state, true);
    }

    public String generateRLE() {
        if (typeCount != 1) return "UNSUPPORTED (multiple lifeforms)";

        StringBuilder data = new StringBuilder();

        Lifeform lifeform = null;

        int leftCursor = 0;
        int rightCursor;

        int line = 0;

        while (line < height) {
            while (leftCursor < width) {
                int current = cells[line][leftCursor].state;

                if (lifeform == null && current != 0) lifeform = cells[line][leftCursor].life;

                rightCursor = leftCursor + 1;
                while (rightCursor < width && cells[line][rightCursor].state == current) rightCursor++;

                if (rightCursor == width && current == 0) {
                    break;
                }


                int count = rightCursor - leftCursor;
                if ((data.length()%70 + ((int)Math.log10(count)) + 2) >= 70) data.append('\n');
                if (count > 1) data.append(count);

                if (generations) {
                    if (current == 0) data.append('.');
                    else data.append((char)('A' + current - 1));
                } else {
                    if (current == 0) data.append('b');
                    else data.append('o');
                }

                leftCursor = rightCursor;
            }
            line++;
            if (data.length()%70 == 69) data.append('\n');
            data.append('$');
            leftCursor = 0;
        }

        if (lifeform == null) return "ERROR: Empty Pattern";

        data.setCharAt(data.length() - 1, '!');
        return "x = " + width + ", y = " + height + ", rule = " + lifeform.ruleString + '\n' + data;
    }

    public boolean isExportable() {return this.typeCount == 1;}
    public int getWidth() {return this.width;}
    public int getHeight() {return this.height;}
}
