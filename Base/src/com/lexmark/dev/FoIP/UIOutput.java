package com.lexmark.dev.FoIP;

import com.sun.xml.internal.bind.annotation.OverrideAnnotationOf;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Pete on 3/31/2018.
 */
public class UIOutput {
    private int textAreaLimit = 100000;
    private JFrame box;
    private JTextArea text;
    private JPanel buttonPane;
    private JButton endButton;
    private int autoDismiss = 120;
    private int RI;
    private String[] Rotator = {"|","/","-","\\"};
    private LogFile log;

    public UIOutput() {}

    public void initialize()
    {
        box = new JFrame();
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel,BoxLayout.PAGE_AXIS));
        box.add(boxPanel);

        text = new JTextArea();
        text.setEditable(false);
        text.setFont(new Font("monospaced", Font.PLAIN, 12));

        DefaultCaret caret = (DefaultCaret) text.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scroll = new JScrollPane(text);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);


        boxPanel.add(scroll);

        box.setTitle("FoIP Output");
        box.setSize(new Dimension(600, 400));
        box.setResizable(true);
        box.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        box.setLocationRelativeTo(null);
        box.setVisible(true);

        buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.setVisible(false);

        endButton = new JButton("Dismiss");
        endButton.setVisible(true);

        boxPanel.add(buttonPane);
        buttonPane.add(endButton);

    }

    public void initialize(JTextArea Text, JButton EndButton, int dismiss, int limit) {
        text = Text;
        box = (JFrame) SwingUtilities.getWindowAncestor(text);
        if (EndButton != null) {
            endButton = EndButton;
            buttonPane = (JPanel) endButton.getParent();
        }
        autoDismiss = dismiss;
        textAreaLimit = limit;
    }

    public void initLog(String LogDir, String LogFileName, int MaxFiles) {
        if (log == null)
            log = new LogFile(LogDir, LogFileName, MaxFiles);
        else
            log.initialize(LogDir, LogFileName, MaxFiles);
    }

    public void NewLogFile() {
        log.NewLogFile();
    }

    public int RemoveAllOldLogFiles() {
        return log.RemoveAllOldLogs();
    }

    public void end() {
        endButton.addActionListener(e -> {
            // display/center the jdialog when the button is pressed
            box.dispose();
        });

        buttonPane.setVisible(true);

        writeNoTime(" ");    // Move the caret to the end

        for (int i=0; i<autoDismiss; i++) {
            Delay(1);
        }

        box.dispose();
     }

    public void writeln(String S)
    {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        LimitResultBox();
        text.setText(text.getText()+dateFormat.format(date)+"  "+S+"\n");
        text.setCaretPosition(text.getText().length());
        if (log != null) {
            log.WriteLogData(dateFormat.format(date)+"  "+S+"\n");
        }
    }

    public void writelnNoTime(String S)
    {
        LimitResultBox();
        text.setText(text.getText()+S+"\n");
        text.setCaretPosition(text.getText().length());
        if (log != null) {
            log.WriteLogData(S+"\n");
        }
    }

    public void write(String S)
    {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        LimitResultBox();
        text.setText(text.getText()+dateFormat.format(date)+"  "+S);
        text.setCaretPosition(text.getText().length());
        if (log != null) {
            log.WriteLogData(dateFormat.format(date)+"  "+S);
        }
    }

    public void writeNoTime(String S)
    {
        LimitResultBox();
        text.setText(text.getText()+S);
        text.setCaretPosition(text.getText().length());
        if (log != null) {
            log.WriteLogData(S);
        }
    }

    public void overwrite(String S) {
        text.setText(text.getText().substring(0,text.getText().length()-S.length())+S);
        if (S.contains("\n")) {
            log.WriteLogData(" " + S);
        }
    }

    public void Delay(int seconds) {
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void DelayWithDots(int seconds, int interval) {
        for (int i=0; i<seconds/interval; i++) {
            try {
                Thread.sleep(interval*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            writeNoTime(".");
        }
    }

    public void DelayWithCountDown(int seconds) {
        writeNoTime(String.format("%1$3s", seconds));
        for (int i=0; i<seconds; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            overwrite(""+String.format("%1$3s", seconds-i-1));
        }
        overwrite("OK\n");

    }

    public void DelayWithCountDown(String S, int seconds) {
        write(S+"..."+ String.format("%1$3s", seconds));
        for (int i=0; i<seconds; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            overwrite(""+String.format("%1$3s", seconds-i-1));
        }
        overwrite("OK\n");

    }

    public void RotatorStart() {
        RI = 0;
        writeNoTime("["+Rotator[RI]+"]");
    }

    public void RotatorNext() {
        RI = (RI + 1) % 4;
        overwrite("["+Rotator[RI]+"]");
    }

    public void RotatorEnd() {
        text.setText(text.getText().substring(0,text.getText().length()-3));
    }

    public void setAutoDismiss(int Seconds) {
        autoDismiss = Seconds;
    }

    public void DelayRotator(int seconds) {
        for (int i=0; i<seconds; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            RotatorNext();
        }
    }

    protected void LimitResultBox() {
        Integer NumChars = text.getText().length();
        if (NumChars > textAreaLimit) {
            text.setText(text.getText().substring(NumChars-textAreaLimit));
        }
    }
}
