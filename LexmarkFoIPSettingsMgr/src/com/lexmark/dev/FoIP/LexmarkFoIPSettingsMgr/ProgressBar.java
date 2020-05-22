package com.lexmark.dev.FoIP.LexmarkFoIPSettingsMgr;


import javax.swing.*;
import java.awt.*;

class ProgressBar extends JPanel {
    private JProgressBar pbar;
    private int barMin = 0;
    private int barMax = 100;
    private int barWidth = 750;
    private int barHeight = 100;
    private JTextField jtfOK;
    private JTextField jtfFail;
    private JTextField jtfLabel;

    public ProgressBar() {
        setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        pbar = new JProgressBar();
        pbar.setMinimum(barMin);
        pbar.setMaximum(barMax);
        pbar.setValue(0);
        Dimension prefSize = pbar.getPreferredSize();
        prefSize.width = barWidth;
        //prefSize.height = barHeight;
        pbar.setPreferredSize(prefSize);
        pbar.setStringPainted(true);
        jtfOK = new JTextField("OK: 0",7);
        jtfOK.setEditable(false);
        jtfOK.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        jtfOK.setHorizontalAlignment(JTextField.CENTER);
        jtfOK.setForeground(new Color(20,140,20));
        Font font = jtfOK.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        jtfOK.setFont(boldFont);
        add(jtfOK);
        panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS));
        jtfLabel = new JTextField("Processing...");
        jtfLabel.setHorizontalAlignment(JTextField.CENTER);
        jtfLabel.setEditable(false);
        jtfLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        jtfLabel.setFont(boldFont);
        panel.add(jtfLabel);
        panel.add(pbar);
        add(panel);
        jtfFail = new JTextField("Fail: 0",7);
        jtfFail.setEditable(false);
        jtfFail.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        jtfFail.setHorizontalAlignment(JTextField.CENTER);
        jtfFail.setForeground(Color.red);
        jtfFail.setFont(boldFont);
        add(jtfFail);
    }

    public void updateBar(int newValue) {
        pbar.setValue(newValue);
    }

    public void setMax(int newValue) {
        pbar.setMaximum(newValue);
    }

    public void setOK(int newValue) {
        jtfOK.setText("OK: " + newValue);
    }

    public void setFail(int newValue) {
        jtfFail.setText("Fail: " + newValue);
    }

    public void setLabel(String newValue) {
        jtfLabel.setText(newValue);
    }


}
