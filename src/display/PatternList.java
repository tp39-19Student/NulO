package display;

import life.Pattern;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class PatternList {

    private final JDialog savedPatternsDialog;

    private final MainFrame mainFrame;

    private static final Color backgroundColor = new Color(55, 55, 111);

    private static final Color importBackgroundColor = new Color(39, 39, 71);
    private static final Color confirmColor = new Color(149, 221, 147);
    private static final Color cancelColor = new Color(177, 55, 55);
    private static final Color exportColor = new Color(126, 126, 126);

    private static final int screenWidth = MainFrame.width;
    private static final int screenHeight = MainFrame.height;

    private static final float importFontSize = 10 + (screenWidth/100);
    private static final Font importFont = new Font(Font.MONOSPACED, Font.PLAIN, (int)importFontSize);

    private static final float listFontSize = 10 + (screenWidth/100);
    private static final Font listFont = new Font(Font.MONOSPACED, Font.PLAIN, (int)listFontSize);


    private final JScrollPane patternListScrollPane;
    private final JList<Pattern> patternList;
    private final DefaultListModel<Pattern> patternListModel;

    PatternList(MainFrame m) {
        this.mainFrame = m;

        patternListModel = new DefaultListModel<>();
        patternList = new JList<>(patternListModel);
        patternList.setBackground(Color.BLACK);
        patternList.setBorder(null);
        patternList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        patternList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        patternList.setVisibleRowCount(-1);
        patternList.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Icon thumbnail = ((Pattern)value).getThumbnail();
                label.setIcon(thumbnail);
                label.setText("");

                if (isSelected) label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10));
                else label.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                return label;
            }
        });

        patternListScrollPane = new JScrollPane(patternList);
        patternListScrollPane.setBackground(Color.BLACK);
        patternListScrollPane.setBorder(null);
        patternListScrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI(Color.BLACK, Color.WHITE));
        patternListScrollPane.getHorizontalScrollBar().setUI(new CustomScrollBarUI(Color.BLACK, Color.WHITE));

        this.savedPatternsDialog = createSavedPatternsDialog();
    }


    private void showImportPatternDialog() {
        JDialog dialog = new JDialog(mainFrame, "Import Pattern", true);
        dialog.setLayout(new BorderLayout(0, 20));

        JLabel label = new JLabel("Import Pattern (RLE / Plaintext)");
        label.setFont(importFont);
        label.setForeground(Color.LIGHT_GRAY);
        dialog.add(label, BorderLayout.NORTH);

        dialog.getContentPane().setBackground(backgroundColor);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialog.setSize(screenWidth/2, screenHeight/2);
        dialog.setLocationRelativeTo(null);
        dialog.setUndecorated(true);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(backgroundColor, 20));
        JTextArea patternText = new JTextArea();
        patternText.setBackground(importBackgroundColor);
        patternText.setForeground(Color.WHITE);
        patternText.setFont(importFont.deriveFont(importFontSize*2/3));
        patternText.setLineWrap(false);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBackground(backgroundColor);

        JButton button = MainFrame.makeButton("Import", importFont, confirmColor, a -> {
            Pattern p = Pattern.parse(patternText.getText());
            if (p != null) {
                mainFrame.setLoadedPattern(p);
                mainFrame.addPattern(p);
            }
            dialog.dispose();
        });
        buttonPanel.add(button);

        button = MainFrame.makeButton("Cancel", importFont, cancelColor, a -> {
            dialog.dispose();
        });
        buttonPanel.add(button);

        JScrollPane patternTextScroll = new JScrollPane(patternText);
        patternTextScroll.getHorizontalScrollBar().setUI(new CustomScrollBarUI(importBackgroundColor, Color.WHITE));
        patternTextScroll.getVerticalScrollBar().setUI(new CustomScrollBarUI(importBackgroundColor, Color.WHITE));
        dialog.add(patternTextScroll, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JDialog createSavedPatternsDialog() {
        JDialog dialog = new JDialog(mainFrame, true);
        dialog.setLayout(new BorderLayout(20, 20));

        dialog.getContentPane().setBackground(backgroundColor);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(screenWidth*3/4, screenHeight*3/4);
        dialog.setLocationRelativeTo(null);
        dialog.setUndecorated(true);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(backgroundColor, 20));

        dialog.add(patternListScrollPane, BorderLayout.CENTER);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 1));
        optionsPanel.setBackground(backgroundColor);
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 40, 40));
        buttonsPanel.setBackground(backgroundColor);

        JButton button = MainFrame.makeButton("Place", listFont, confirmColor, a -> {
            Pattern p = patternList.getSelectedValue();
            if (p != null) {
                mainFrame.setLoadedPattern(p);
                dialog.setVisible(false);
            }
        });
        buttonsPanel.add(button);

        button = MainFrame.makeButton("Export RLE", listFont, exportColor, a -> {
            Pattern p = patternList.getSelectedValue();
            if (p != null) {
                if (!p.isExportable()) {MainFrame.log("Patterns with multiple lifeforms can't be exported"); return;}
                showExportDialog(p.generateRLE());
            }
        });
        buttonsPanel.add(button);


        buttonsPanel.add(button);

        optionsPanel.add(buttonsPanel);
        dialog.add(optionsPanel, BorderLayout.EAST);

        button = MainFrame.makeButton("Close", listFont, cancelColor, a -> dialog.setVisible(false));
        dialog.add(button, BorderLayout.SOUTH);

        return dialog;
    }

    private void showExportDialog(String data) {
        JDialog dialog = new JDialog(mainFrame, "Export", true);
        dialog.setLayout(new BorderLayout(0, 20));

        dialog.getContentPane().setBackground(backgroundColor);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(screenWidth/2, screenHeight/2);
        dialog.setLocationRelativeTo(null);
        dialog.setUndecorated(true);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(backgroundColor, 20));
        JTextArea content = new JTextArea(data);
        content.setEditable(false);
        content.setBackground(importBackgroundColor);
        content.setForeground(Color.WHITE);
        content.setFont(importFont.deriveFont(importFontSize*2/3));
        content.setLineWrap(false);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBackground(backgroundColor);

        JButton button = MainFrame.makeButton("Copy", importFont, confirmColor, a -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content.getText()), null);
        });
        buttonPanel.add(button);

        button = MainFrame.makeButton("Close", importFont, cancelColor, a -> {
            dialog.dispose();
        });
        buttonPanel.add(button);

        JScrollPane contentScroll = new JScrollPane(content);
        contentScroll.getHorizontalScrollBar().setUI(new CustomScrollBarUI(importBackgroundColor, Color.WHITE));
        contentScroll.getVerticalScrollBar().setUI(new CustomScrollBarUI(importBackgroundColor, Color.WHITE));
        dialog.add(contentScroll, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void addPattern(Pattern p) {this.patternListModel.addElement(p); this.patternList.revalidate(); this.patternList.repaint();}
    public void showImport() {showImportPatternDialog();}
    public void showList() {this.savedPatternsDialog.setVisible(true);}
}
