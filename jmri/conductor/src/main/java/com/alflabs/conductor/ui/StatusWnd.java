package com.alflabs.conductor.ui;

import com.alflabs.conductor.IConductorComponent;
import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriSensor;
import com.alflabs.conductor.script.ExecEngine;
import com.alflabs.conductor.script.Script;
import com.alflabs.conductor.script.Sensor;
import com.alflabs.conductor.script.Throttle;
import com.alflabs.conductor.script.Timer;
import com.alflabs.conductor.script.Turnout;
import com.alflabs.conductor.script.Var;
import com.alflabs.conductor.util.LogException;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.utils.RPair;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.*;

public class StatusWnd {
    private final JFrame mFrame;
    private JPanel mRootPanel;
    private JTextField mTextScriptName;
    private JButton mButtonReload;
    private JTextArea mAreaStatus;
    private JLabel mLabelSpeed1;
    private JLabel mLabelSpeed2;
    private JLabel mLabelDcc1;
    private JLabel mLabelDcc2;
    private JButton mButtonChangeDcc1;
    private JButton mButtonChangeDcc2;
    private JButton mButtonStop;
    private JScrollPane mScrollPanel;
    private JSeparator mSepThrottles;
    private JSeparator mSepConsole;
    private JPanel mSensorPanel;
    private String mLastError;

    private StatusWnd(JFrame frame) {
        mFrame = frame;

        // Fix the missing border on the JTextArea to match a JTextField border
        // (c.f. http://stackoverflow.com/questions/2654948)
        mAreaStatus.setBorder(mTextScriptName.getBorder());
    }

    public static StatusWnd open() {
        JFrame frame = new JFrame("Conductor Status");
        StatusWnd statusWnd = new StatusWnd(frame);
        frame.setContentPane(statusWnd.mRootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return statusWnd;
    }

    public void init(
            IConductorComponent component,
            Script script,
            ExecEngine engine,
            Supplier<RPair<Script, String>> onReloadAction,
            Runnable onStopAction) {
        mTextScriptName.setText(component.getScriptFile().getAbsolutePath());
        initScript(component, script, engine);

        mButtonReload.addActionListener(actionEvent -> {
            mLastError = null;
            RPair<Script, String> pair = onReloadAction.get();
            if (pair.second != null) {
                mLastError = pair.second;
                showError(mLastError);
            }
            if (pair.first != null) {
                initScript(component, pair.first, engine);
            }
        });

        mButtonStop.addActionListener(actionEvent -> {
            onStopAction.run();
            mFrame.dispose();
        });

        mFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                super.windowClosing(windowEvent);
                onStopAction.run();
                mFrame.dispose();
            }
        });

        List<String> sensorNames = script.getSensorNames();
        final int numCol = 6;
        final int numRow = (int) Math.ceil(sensorNames.size() / (double) numCol);
        int col = 0;
        int row = 0;
        mSensorPanel.setLayout(new GridLayoutManager(
                numRow,
                numCol,
                new Insets(0, 0, 0, 0), -1, -1));
        for (String name : sensorNames) {
            Sensor sensor = script.getSensor(name);
            JCheckBox box = new JCheckBox();
            box.setText(name.toUpperCase());
            box.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean selected = box.isSelected();
                    IJmriSensor jmriSensor = sensor.getJmriSensor();
                    if (jmriSensor != null && selected != sensor.isActive()) {
                        jmriSensor.setActive(selected);
                    }
                }
            });
            sensor.setOnChangedListener(() -> box.setSelected(sensor.isActive()));

            mSensorPanel.add(box, new GridConstraints(row, col, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
            col++;
            if (col == numCol) {
                col = 0;
                row++;
            }
        }
        mFrame.pack();
    }

    private void showError(String error) {
        mAreaStatus.setText(error);
        mLabelSpeed1.setText("");
        mLabelSpeed2.setText("");
        mLabelDcc1.setText("");
        mLabelDcc2.setText("");
    }

    private void initScript(
            IConductorComponent component,
            Script script,
            ExecEngine engine) {
        final int numThrottles = 2; // TODO hack for quick test
        JLabel[] labelSpeed = new JLabel[]{mLabelSpeed1, mLabelSpeed2};
        JLabel[] labelDcc = new JLabel[]{mLabelDcc1, mLabelDcc2};
        JButton[] buttonChangeDcc = new JButton[]{mButtonChangeDcc1, mButtonChangeDcc2};
        Throttle[] throttles = new Throttle[numThrottles];

        List<String> throttleNames = script.getThrottleNames();
        for (int i = 0; i < throttleNames.size() && i < numThrottles; i++) {
            Throttle throttle = script.getThrottle(throttleNames.get(i));
            throttles[i] = throttle;
            labelDcc[i].setText(throttle.getDccAddresses());

            JLabel label = labelSpeed[i];
            throttle.setSpeedListener(speed -> label.setText(Integer.toString(speed)));
        }

        for (int i = 0; i < numThrottles; i++) {
            JButton button = buttonChangeDcc[i];
            button.setEnabled(false);
            Arrays.stream(button.getActionListeners()).forEach(button::removeActionListener);
            if (i < throttleNames.size()) {
                Throttle throttle = throttles[i];
                JLabel label = labelDcc[i];
                button.setEnabled(true);
                button.addActionListener(actionEvent -> {
                    askNewDccAddress(throttle, component.getJmriProvider(), label);
                });
            }
        }

        engine.setHandleListener(() -> mAreaStatus.setText(
                generateVarStatus(script, engine, component.getKeyValueServer())));
    }

    private void askNewDccAddress(Throttle throttle, IJmriProvider jmriProvider, JLabel label) {
        String address = throttle.getDccAddresses();
        Object result = JOptionPane.showInputDialog(mFrame,
                "New DCC Address to replace " + address,
                "New DCC Address",
                JOptionPane.PLAIN_MESSAGE,
                null,  // icon
                null,  // possibilities
                address);
        if (result instanceof String) {
            try {
                int newAddress = Integer.parseInt((String) result);
                throttle.setDccAddress(newAddress);
                label.setText(Integer.toString(newAddress));
            } catch (Exception e) {
                LogException.logException(jmriProvider, e);
            }
        }
    }

    private StringBuilder mStatus = new StringBuilder();

    private String generateVarStatus(
            Script script,
            ExecEngine engine,
            KeyValueServer kvServer) {
        mStatus.setLength(0);

        mStatus.append("Freq: ");
        float freq = engine.getHandleFrequency();
        mStatus.append(String.format("%.1f Hz\n\n", freq));

        if (mLastError != null) {
            mStatus.append("--- [ LAST ERROR ] ---\n");
            mStatus.append(mLastError);
        }

        mStatus.append("--- [ TURNOUTS ] ---\n");
        int i = 0;
        for (String name : script.getTurnoutNames()) {
            Turnout turnout = script.getTurnout(name);
            mStatus.append(name.toUpperCase()).append(": ").append(turnout.isActive() ? 'N' : 'R');
            mStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        if (mStatus.charAt(mStatus.length() - 1) != '\n') {
            mStatus.append('\n');
        }

        mStatus.append("--- [ SENSORS ] ---\n");
        i = 0;
        for (String name : script.getSensorNames()) {
            Sensor sensor = script.getSensor(name);
            mStatus.append(name.toUpperCase()).append(": ").append(sensor.isActive() ? '1' : '0');
            mStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        if (mStatus.charAt(mStatus.length() - 1) != '\n') {
            mStatus.append('\n');
        }

        mStatus.append("--- [ TIMERS ] ---\n");
        i = 0;
        for (String name : script.getTimerNames()) {
            Timer timer = script.getTimer(name);
            mStatus.append(name).append(':').append(timer.isActive() ? '1' : '0');
            mStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        if (mStatus.charAt(mStatus.length() - 1) != '\n') {
            mStatus.append('\n');
        }

        mStatus.append("--- [ VARS ] ---\n");
        i = 0;
        for (String name : script.getVarNames()) {
            Var var = script.getVar(name);
            mStatus.append(name).append(':').append(var.getAsInt());
            mStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        if (mStatus.charAt(mStatus.length() - 1) != '\n') {
            mStatus.append('\n');
        }

        mStatus.append("--- [ KV Server ] ---\n");
        mStatus.append("Connections: ").append(kvServer.getNumConnections()).append('\n');
        for (Map.Entry<String, String> entry : kvServer.getAllValues().entrySet()) {
            mStatus.append('[').append(entry.getKey()).append("] = ").append(entry.getValue()).append('\n');
        }

        return mStatus.toString();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mRootPanel = new JPanel();
        mRootPanel.setLayout(new GridLayoutManager(9, 7, new Insets(10, 10, 10, 10), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Script");
        mRootPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mTextScriptName = new JTextField();
        mTextScriptName.setEditable(false);
        mRootPanel.add(mTextScriptName, new GridConstraints(0, 1, 1, 6, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Throttles");
        mRootPanel.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mButtonReload = new JButton();
        mButtonReload.setText("Reload Script");
        mRootPanel.add(mButtonReload, new GridConstraints(1, 5, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Status");
        mRootPanel.add(label3, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mLabelSpeed1 = new JLabel();
        mLabelSpeed1.setText("Label");
        mRootPanel.add(mLabelSpeed1, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mLabelSpeed2 = new JLabel();
        mLabelSpeed2.setText("Label");
        mRootPanel.add(mLabelSpeed2, new GridConstraints(4, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mLabelDcc1 = new JLabel();
        mLabelDcc1.setText("Label");
        mRootPanel.add(mLabelDcc1, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mLabelDcc2 = new JLabel();
        mLabelDcc2.setText("Label");
        mRootPanel.add(mLabelDcc2, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mRootPanel.add(spacer1, new GridConstraints(1, 3, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mRootPanel.add(spacer2, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
        mButtonChangeDcc1 = new JButton();
        mButtonChangeDcc1.setText("Change");
        mRootPanel.add(mButtonChangeDcc1, new GridConstraints(3, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mButtonChangeDcc2 = new JButton();
        mButtonChangeDcc2.setText("Change");
        mRootPanel.add(mButtonChangeDcc2, new GridConstraints(4, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mButtonStop = new JButton();
        mButtonStop.setText("Stop Script");
        mRootPanel.add(mButtonStop, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mSepThrottles = new JSeparator();
        mRootPanel.add(mSepThrottles, new GridConstraints(2, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mSepConsole = new JSeparator();
        mRootPanel.add(mSepConsole, new GridConstraints(5, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mScrollPanel = new JScrollPane();
        mScrollPanel.setVerticalScrollBarPolicy(22);
        mRootPanel.add(mScrollPanel, new GridConstraints(7, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(150, 150), null, null, 2, false));
        mAreaStatus = new JTextArea();
        mAreaStatus.setEditable(false);
        mScrollPanel.setViewportView(mAreaStatus);
        mSensorPanel = new JPanel();
        mSensorPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mRootPanel.add(mSensorPanel, new GridConstraints(8, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mRootPanel;
    }

    // ---------------------------------------------------------------------------------

}
