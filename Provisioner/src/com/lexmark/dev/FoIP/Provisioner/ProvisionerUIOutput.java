package com.lexmark.dev.FoIP.Provisioner;

import com.lexmark.dev.FoIP.UIOutput;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

/**
 * Created by Pete on 3/31/2018.
 */
public class ProvisionerUIOutput extends UIOutput {
    private JFrame box;
    private JTextArea text;
    private JPanel buttonPane;
    private JButton endButton;

    public ProvisionerUIOutput() {
        box = new JFrame();
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.PAGE_AXIS));
        box.add(boxPanel);

        text = new JTextArea();
        text.setEditable(false);
        text.setFont(new Font("monospaced", Font.PLAIN, 12));

        DefaultCaret caret = (DefaultCaret) text.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scroll = new JScrollPane(text);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);


        boxPanel.add(scroll);

        box.setTitle("Lexmark FoIP Provisioner");
        box.setSize(new Dimension(600, 400));
        box.setResizable(true);
        box.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        box.setLocationRelativeTo(null);
        box.setVisible(true);

        buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.setVisible(false);

        endButton = new JButton("Dismiss");
        endButton.setVisible(true);

        boxPanel.add(buttonPane);
        buttonPane.add(endButton);

        super.initialize(text, endButton,120,100000);
        super.initLog(System.getProperty("user.home"), "LexmarkProvisionerLog", 3);
    }
}

