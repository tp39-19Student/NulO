package logic;

import display.ConsoleDisplay;
import display.MainFrame;

public class Console {
    private static final String helpLine = "Commands:\n" +
            "play - Continue simulation\n" +
            "pause - Pause simulation\n" +
            "f - Next frame\n" +
            "setsize <Number> - Set size of single cell to NxN pixels\n" +
            "speed <Number> - Set simulation speed to N generations per second.\n" +
            "simdim - Display simulation size in cells\n" +
            "clear - Clear console\n" +
            "new - Clear simulation\n" +
            "count - Count living cells";

    private static final int historyLines = 20;

    private final ConsoleDisplay display;
    private Simulation sim;

    private final String[] history;
    private int historyIndex;
    private String currentInput;



    public Console(ConsoleDisplay display) {
        this.display = display;
        this.history = new String[historyLines];
        this.historyIndex = -1;
    }

    private void addToHistory(String str) {
        for (int i = historyLines-1; i > 0; i--) {
            history[i] = history[i-1];
        }
        history[0] = str;
    }

    public void command(String cmd) {
        //MainFrame.getInstance().getCanvas().repaint();
        cmd = cmd.trim();
        if (cmd.isEmpty()) {
            display.setInput("");
            return;
        }

        addToHistory(cmd);
        historyIndex = -1;
        String key = cmd;
        String args = null;
        int spaceIndex = cmd.indexOf(' ');
        if (spaceIndex != -1) {
            key = cmd.substring(0, spaceIndex);
            args = cmd.substring(spaceIndex + 1);
        }
        if (sim == null) sim = MainFrame.getInstance().getSimulation();
        key = key.toLowerCase();

        switch (key) {
            case "clear": display.clear(); break;
            case "echo": display.println(args); break;
            case "help": display.println(helpLine); break;
            case "simdim": case "dim": {
                display.println("Simulation fits " + sim.getWidth() + " x " + sim.getHeight() + " cells");
            } break;
            case "play": case "run": case "start": case "resume": sim.play(); break;
            case "pause": case "stop": sim.pause(); break;
            case "exit": case"quit": MainFrame.getInstance().dispose(); break;
            case "setsize": {
                if (args == null) {
                    display.println("Usage: setsize <Number>\nSet the size of a single cell to NxN pixels, will restart the simulation.");
                    return;
                }
                String s = null;
                try {
                    s = args.split(" ")[0];
                    int size = Integer.parseInt(s);
                    if (MainFrame.getInstance().getCanvas().getPixelSize() == size) {
                        display.println("Size is already " + size);
                        return;
                    }
                    if (size <= 0) {
                        display.println("Size must be greater than 0");
                        return;
                    }
                    else {
                        MainFrame.getInstance().getCanvas().setPixelSize(size);
                        this.sim = MainFrame.getInstance().getSimulation();
                        display.println("Size set to " + size);
                    }
                } catch (NumberFormatException e) {display.println(s + " is not an integer.");}
            } break;
            case "getsize": case "size": {
                int size = MainFrame.getInstance().getCanvas().getPixelSize();
                display.println("Current size of a single cell: " + size + "x" + size + " pixels");
            } break;
            case "new": {
                MainFrame.getInstance().getCanvas().newSimulation();
            } break;
            case "crazy": case "psycho": case "art": {
                display.println("Crazy brush = " + MainFrame.getInstance().getCanvas().toggleCrazy());
            } break;
            case "life": {
                display.println(Simulation.GOL.toString());
            } break;
            case "count": {
                //#TODO
                display.println("Reworking");
            } break;
            case "speed": case "setspeed": case "fps": case "gps": {
                if (args == null) {
                    display.println("Usage: speed <Number>\nSet the speed to N generations per second.");
                    return;
                }
                String s = null;
                try {
                    s = args.split(" ")[0];
                    int speed = Integer.parseInt(s);
                    if (sim.getFps() == speed) {
                        display.println("Speed is already " + speed);
                        return;
                    }
                    if (speed <= 0) {
                        display.println("Speed must be greater than 0");
                        return;
                    }
                    if (speed > 1000) {
                        display.println("Speed cannot be greater than 1000");
                        return;
                    }
                    else {
                        sim.setFps(speed);
                        display.println("Speed set to " + speed + " generations per second");
                    }
                } catch (NumberFormatException e) {display.println(s + " is not an integer.");}
            } break;
            case "next": case "f": case "frame": sim.pause(); sim.nextFrame(); break;
            default: display.println("Unknown command: " + (cmd.length()<20?cmd:"..."));
        }
    }

    public void historyUp(String current) {
        if (historyIndex == (historyLines - 1)) return;
        if (history[historyIndex + 1] == null) return;
        if (historyIndex == -1) currentInput = current;
        display.setInput(history[++historyIndex]);
    }
    public void historyDown() {
        if (historyIndex == -1) return;
        if (historyIndex == 0) {display.setInput(currentInput); historyIndex = -1;}
        else {
            display.setInput(history[--historyIndex]);
        }
    }
    public void setSimulation(Simulation sim) {
        this.sim = sim;
    }

    public void println(String s) {
        display.println(s);
    }
}
