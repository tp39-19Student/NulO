package display;

import logic.Console;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ConsoleDisplay extends JPanel {
    private static final Color consoleBackgroundColor = new Color(20, 20, 20);
    private static final Color consoleInputBackgroundColor = new Color(50, 50, 50);
    private static final Color consoleTextColor = new Color(115, 255, 135);

    private final JScrollBar textScroll;
    private final JTextArea text;
    private final JTextArea input;
    private final Console console;

    public ConsoleDisplay() {
        super();
        this.setLayout(new BorderLayout());

        this.text = new JTextArea();
        this.text.setBackground(consoleBackgroundColor);
        this.text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
        this.text.setForeground(consoleTextColor);
        this.text.setMargin(new Insets(10, 10, 10, 10));
        this.text.setLineWrap(true);
        this.text.setEditable(false);

        JScrollPane scroll = new JScrollPane(this.text);
        this.textScroll = scroll.getVerticalScrollBar();
        this.add(scroll, BorderLayout.CENTER);

        this.input = new JTextArea();
        this.input.setBackground(consoleBackgroundColor);
        this.input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
        this.input.setForeground(consoleTextColor);
        this.input.setCaretColor(consoleTextColor);
        this.input.setMargin(new Insets(10, 10, 10, 10));
        this.input.setLineWrap(true);

        this.console = new Console(this);
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    console.command(input.getText());
                    input.setText("");
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    console.historyUp(input.getText());
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    console.historyDown();
                }
            }
        });

        scroll = new JScrollPane(this.input);
        this.add(scroll, BorderLayout.SOUTH);

        this.setBackground(consoleInputBackgroundColor);
    }

    public void clear() { this.text.setText(""); }

    public void println(String line) {
        this.text.append(line + '\n');
        textScroll.setValue(textScroll.getMaximum());
    }

    public boolean inputFocused() { return this.input.hasFocus(); }
    public Console getConsole() { return this.console; }
    public void setInput(String text) { this.input.setText(text); }
}
