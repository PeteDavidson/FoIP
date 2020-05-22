package com.lexmark.dev.FoIP.LexmarkFoIPSettingsMgr;


import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

class FoIPSetting {

    JTextField jtfSettingText;
    JTextField jtfSetting;
    JCheckBox  boxSetting;
    JCheckBox  boxSettingVal;
    JComboBox<String>  jcbSetting;
    int insetTop = 1;
    int insetLeft = 2;
    int insetBottom = 0;
    int insetRight = 2;
    int ipady = 2;

    public enum SettingType { TEXT, BOOLEAN, LIST, HEADING, NOSELECT };
    SettingType settingType;

    // Heading
    public FoIPSetting(JPanel Panel, int X, int Y, int W, String Name) {

        settingType = SettingType.HEADING;

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(insetTop,insetLeft+3,insetBottom,insetRight+3);
        c.gridwidth = W;

        jtfSettingText = new JTextField(Name, 18);
        jtfSettingText.setBorder(javax.swing.BorderFactory.createMatteBorder(0,0,2,0,Color.BLACK));
        jtfSettingText.setEditable(false);
        c.gridx = X; c.gridy = Y; c.ipadx = 150; c.ipady = ipady;
        Font font = jtfSettingText.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        //Font boldFont = new Font(font.getFontName(), Font.BOLD, 10);
        jtfSettingText.setFont(boldFont);
        Panel.add(jtfSettingText,c);
    }

    // Text Setting without selection box
    public FoIPSetting(JPanel Panel, int X, int Y, int W, String Name, String InitVal) {

        settingType = SettingType.NOSELECT;

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(insetTop,insetLeft+3,insetBottom,insetRight);
        //c.insets = new Insets(10,1,1,1);

        jtfSettingText = new JTextField(Name, 18);
        jtfSettingText.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        jtfSettingText.setEditable(false);
        c.gridx = X+1; c.gridy = Y;  c.ipadx = 150; c.ipady = ipady;
        jtfSettingText.setPreferredSize(new Dimension(150,5));
        Panel.add(jtfSettingText,c);

        c.insets = new Insets(insetTop,insetLeft,insetBottom,insetRight+3);
        //c.insets = new Insets(10,1,1,5);
        jtfSetting = new JTextField(InitVal,25);
        c.gridx = X+2; c.gridy = Y; c.ipadx = 250; c.ipady = ipady;
        c.weightx = 0.7;
        c.gridwidth = W;
        Panel.add(jtfSetting,c);
    }

    // Text Setting
    public FoIPSetting(JPanel Panel, int X, int Y, int W, String Name, String InitVal, boolean BoxChecked) {

        settingType = SettingType.TEXT;

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(insetTop,insetLeft+3,insetBottom,insetRight);
        //c.insets = new Insets(1,5,1,5);

        boxSetting = new JCheckBox();
        boxSetting.setSelected(BoxChecked);
        c.gridx = X; c.gridy = Y; c.ipadx = 0; c.ipady = ipady;
        Panel.add(boxSetting,c);

        c.insets = new Insets(insetTop,insetLeft,insetBottom,insetRight);
        //c.insets = new Insets(1,1,1,1);
        jtfSettingText = new JTextField(Name, 18);
        jtfSettingText.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        jtfSettingText.setEditable(false);
        c.gridx = X+1; c.gridy = Y; c.ipadx = 150; c.ipady = ipady;
        Panel.add(jtfSettingText,c);

        c.insets = new Insets(insetTop,insetLeft,insetBottom,insetRight+3);
        //c.insets = new Insets(1,1,1,5);
        jtfSetting = new JTextField(InitVal,25);
        c.gridx = X+2; c.gridy = Y; c.ipadx = 250; c.ipady = ipady;
        c.weightx = 0.7;
        c.gridwidth = W;
        Panel.add(jtfSetting,c);
    }

    // Boolean Setting
    public FoIPSetting(JPanel Panel, int X, int Y, int W, String Name, boolean InitVal, boolean BoxChecked) {

        settingType = SettingType.BOOLEAN;

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(insetTop,insetLeft+3,insetBottom,insetRight);
        //c.insets = new Insets(1,5,1,5);

        boxSetting = new JCheckBox();
        boxSetting.setSelected(BoxChecked);
        c.gridx = X; c.gridy = Y; c.ipadx = 0; c.ipady = ipady;
        Panel.add(boxSetting,c);

        c.insets = new Insets(insetTop,insetLeft,insetBottom,insetRight);
        //c.insets = new Insets(1,1,1,1);
        jtfSettingText = new JTextField(Name, 18);
        jtfSettingText.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        jtfSettingText.setEditable(false);
        c.gridx = X+1; c.gridy = Y; c.ipadx = 150; c.ipady = ipady;
        Panel.add(jtfSettingText,c);

        c.insets = new Insets(insetTop,insetLeft,insetBottom,insetRight+3);
        //c.insets = new Insets(1,1,1,5);
        boxSettingVal = new JCheckBox();
        boxSettingVal.setSelected(InitVal);
        c.gridx = X+2; c.gridy = Y; c.ipadx = 150; c.ipady = ipady;
        Panel.add(boxSettingVal,c);
    }

    // List Setting
    public FoIPSetting(JPanel Panel, int X, int Y, int W, String Name, String[] Values, int InitVal, boolean BoxChecked) {

        settingType = SettingType.LIST;

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(insetTop,insetLeft+3,insetBottom,insetRight);
        //c.insets = new Insets(1,5,1,5);

        boxSetting = new JCheckBox();
        boxSetting.setSelected(BoxChecked);
        c.gridx = X; c.gridy = Y; c.ipadx = 0; c.ipady = ipady;
        Panel.add(boxSetting,c);

        c.insets = new Insets(insetTop,insetLeft,insetBottom,insetRight);
        //c.insets = new Insets(1,1,1,1);
        jtfSettingText = new JTextField(Name, 18);
        jtfSettingText.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        jtfSettingText.setEditable(false);
        c.gridx = X+1; c.gridy = Y; c.ipadx = 150; c.ipady = ipady;
        Panel.add(jtfSettingText,c);

        c.insets = new Insets(insetTop,insetLeft,insetBottom,insetRight+3);
        //c.insets = new Insets(1,1,1,5);
        jcbSetting = new JComboBox<>(Values);
        jcbSetting.setSelectedIndex(InitVal);
        c.gridx = X+2; c.gridy = Y; c.ipadx = 50; c.ipady = ipady;
        c.weightx = 0.7;
        Panel.add(jcbSetting,c);
    }

    public boolean getChecked() {
        if (boxSettingVal == null)
            return false;
        else
            return boxSettingVal.isSelected();
    }

    public void setChecked(boolean S) {
        if (boxSettingVal != null)
            boxSettingVal.setSelected(S);
    }

    public boolean isSelected() {
        if (boxSetting == null)
            return false;
        else
            return boxSetting.isSelected();
    }

    public void setSelected(boolean S) {
        if (boxSetting != null)
            boxSetting.setSelected(S);
    }

    public String getLabel() {
        return jtfSettingText.getText();
    }

    public String getText() {
        return jtfSetting.getText().trim();
    }

    public void setText(String S) {
        jtfSetting.setText(S);
    }

    public int getSelectedIndex() {
        return jcbSetting.getSelectedIndex();
    }

    public void setSelectedIndex(int I) {
        jcbSetting.setSelectedIndex(I);
    }

    public String getFileName() {
        Path p = Paths.get(jtfSetting.getText());
        String file = p.getFileName().toString();
        return file;
    }

    public void readSetting(BufferedReader R) throws IOException {
        String line = R.readLine();
        String delim = "[|]";
        String[] tokens = line.split(delim);
        this.setSelected(tokens[1].equals("X"));
        switch (this.settingType) {
            case TEXT:
                this.setText(tokens.length < 3 ? "" : tokens[2]);
                break;
            case NOSELECT:
                this.setText(tokens.length < 3 ? "" : tokens[2]);
                break;
            case LIST:
                this.setSelectedIndex(Integer.parseInt(tokens[2]));
                break;
            case BOOLEAN:
                this.setChecked((Integer.parseInt(tokens[2]) != 0));
                break;
        }
    }

    public void readSetting(String line) throws IOException {
        String delim = "[|]";
        String[] tokens = line.split(delim);
        this.setSelected(tokens[1].equals("X"));
        switch (this.settingType) {
            case TEXT:
                this.setText(tokens.length < 3 ? "" : tokens[2]);
                break;
            case NOSELECT:
                this.setText(tokens.length < 3 ? "" : tokens[2]);
                break;
            case LIST:
                this.setSelectedIndex(Integer.parseInt(tokens[2]));
                break;
            case BOOLEAN:
                this.setChecked((Integer.parseInt(tokens[2]) != 0));
                break;
        }
    }

    public void writeSetting(BufferedWriter W) throws IOException {
        switch (this.settingType) {
            case TEXT:
                W.write("" + this.getLabel() + "|" + (this.isSelected() ? 'X' : 'O') + "|" + this.getText() + "|" + "\n");
                break;
            case NOSELECT:
                W.write("" + this.getLabel() + "|" + (this.isSelected() ? 'X' : 'O') + "|" + this.getText() + "|" + "\n");
                break;
            case LIST:
                W.write("" + this.getLabel() + "|" + (this.isSelected() ? 'X' : 'O') + "|" + this.getSelectedIndex() + "|" + "\n");
                break;
            case BOOLEAN:
                W.write("" + this.getLabel() + "|" + (this.isSelected() ? 'X' : 'O') + "|" + (this.getChecked() ? 1 : 0) + "|" + "\n");
                break;
        }
    }

}
