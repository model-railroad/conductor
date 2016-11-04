package com.alfray.conductor.ui;

import javax.swing.*;

public class StatusWnd {
    private JPanel mPanel1;
    private JTextField mTextScriptName;
    private JButton mButtonReload;
    private JTextArea mAreaStatus;
    private JLabel mLabelSpeed1;
    private JLabel mLabelSpeed2;
    private JLabel mLabelDcc1;
    private JLabel mLabelDcc2;
    private JButton mButtonChangeDcc1;
    private JButton mButtonChangeDcc2;

    public StatusWnd() {
        mButtonReload.addActionListener(actionEvent -> {

        });
        mButtonChangeDcc1.addActionListener(actionEvent -> {

        });
        mButtonChangeDcc2.addActionListener(actionEvent -> {

        });
    }

    public static StatusWnd open() {
        JFrame frame =new JFrame("Some Title");
        StatusWnd statusWnd = new StatusWnd();
        frame.setContentPane(statusWnd.mPanel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return statusWnd;
    }
}
