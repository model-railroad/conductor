package com.alfray.conductor.ui;

import com.alfray.conductor.script.Script;
import com.alfray.conductor.script.Sensor;
import com.alfray.conductor.script.Throttle;
import com.alfray.conductor.script.Turnout;
import com.alfray.conductor.script.Var;
import com.alfray.conductor.util.Pair;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import java.awt.Dimension;
import java.awt.Insets;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class StatusWnd {
    private final JFrame mFrame;
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
    private JButton mButtonStop;

    public StatusWnd(JFrame frame) {
        mFrame = frame;
    }

    public static StatusWnd open() {
        JFrame frame = new JFrame("Some Title");
        StatusWnd statusWnd = new StatusWnd(frame);
        frame.setContentPane(statusWnd.mPanel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return statusWnd;
    }

    public void init(
            File scriptName,
            Script script,
            Supplier<Pair<Script, String>> reloader,
            Runnable stopper) {
        mTextScriptName.setText(scriptName.getAbsolutePath());
        initScript(script);

        mButtonReload.addActionListener(actionEvent -> {
            Pair<Script, String> pair = reloader.get();
            if (pair.mFirst != null) {
                initScript(pair.mFirst);
            } else if (pair.mSecond != null) {
                showError(pair.mSecond);
            }
        });

        mButtonStop.addActionListener(actionEvent -> {
            stopper.run();
            mFrame.dispose();
        });

    }

    private void showError(String error) {
        mAreaStatus.setText(error);
        mLabelSpeed1.setText("");
        mLabelSpeed2.setText("");
        mLabelDcc1.setText("");
        mLabelDcc2.setText("");
    }

    private void initScript(Script script) {
        final int numThrottles = 2; // TODO hack for quick test
        JLabel[] labelSpeed = new JLabel[]{mLabelSpeed1, mLabelSpeed2};
        JLabel[] labelDcc = new JLabel[]{mLabelDcc1, mLabelDcc2};
        JButton[] buttonChangeDcc = new JButton[]{mButtonChangeDcc1, mButtonChangeDcc2};
        Throttle[] throttles = new Throttle[numThrottles];

        List<String> throttleNames = script.getThrottleNames();
        for (int i = 0; i < throttleNames.size() && i < numThrottles; i++) {
            Throttle throttle = script.getThrottle(throttleNames.get(i));
            throttles[i] = throttle;
            labelDcc[i].setText(Integer.toString(throttle.getDccAddress()));

            JLabel label = labelSpeed[i];
            throttle.setSpeedListener(speed -> label.setText(Integer.toString(speed)));
        }

        for (int i = 0; i < numThrottles; i++) {
            JButton button = buttonChangeDcc[i];
            button.setEnabled(false); // TODO implement changing address later, if relevant
            Arrays.stream(button.getActionListeners()).forEach(button::removeActionListener);
            if (i < throttleNames.size()) {
                button.addActionListener(actionEvent -> {
                    // throttles[i].setDccAddress(provider, newDccAddress);
                });
            }
        }

        script.setHandleListener(() -> mAreaStatus.setText(generateVarStatus(script)));
    }

    private StringBuilder mStatus = new StringBuilder();
    private String generateVarStatus(Script script) {
        mStatus.setLength(0);

        mStatus.append("Freq: ");
        long delayMs = script.getLastHandleDeltaMs();
        if (delayMs > 0) {
            mStatus.append(String.format("%.1f Hz\n", 1000.0f / delayMs));
        } else {
            mStatus.append("--.- Hz\n");
        }

        mStatus.append("\n");
        int i = 0;
        for (String name : script.getTurnoutNames()) {
            Turnout turnout = script.getTurnout(name);
            mStatus.append(name).append(':').append(turnout.isActive() ? 'N' : 'R');
            mStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }

        mStatus.append("\n\n");
        i = 0;
        for (String name : script.getSensorNames()) {
            Sensor sensor = script.getSensor(name);
            mStatus.append(name).append(':').append(sensor.isActive() ? '1' : '0');
            mStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }

        mStatus.append("\n\n");
        i = 0;
        for (String name : script.getVarNames()) {
            Var var = script.getVar(name);
            mStatus.append(name).append(':').append(var.getAsInt());
            mStatus.append((i++) % 4 == 3 ? "\n" : "   ");
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
        mPanel1 = new JPanel();
        mPanel1.setLayout(new GridLayoutManager(8, 7, new Insets(5, 5, 5, 5), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Script");
        mPanel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mTextScriptName = new JTextField();
        mPanel1.add(mTextScriptName, new GridConstraints(0, 1, 1, 6, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Throttles");
        mPanel1.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mButtonReload = new JButton();
        mButtonReload.setText("Reload Script");
        mPanel1.add(mButtonReload, new GridConstraints(1, 5, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Status");
        mPanel1.add(label3, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mAreaStatus = new JTextArea();
        mAreaStatus.setEditable(false);
        mPanel1.add(mAreaStatus, new GridConstraints(7, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        mLabelSpeed1 = new JLabel();
        mLabelSpeed1.setText("Label");
        mPanel1.add(mLabelSpeed1, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mLabelSpeed2 = new JLabel();
        mLabelSpeed2.setText("Label");
        mPanel1.add(mLabelSpeed2, new GridConstraints(4, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mLabelDcc1 = new JLabel();
        mLabelDcc1.setText("Label");
        mPanel1.add(mLabelDcc1, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mLabelDcc2 = new JLabel();
        mLabelDcc2.setText("Label");
        mPanel1.add(mLabelDcc2, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mPanel1.add(spacer1, new GridConstraints(1, 3, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mPanel1.add(spacer2, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
        mButtonChangeDcc1 = new JButton();
        mButtonChangeDcc1.setText("Change");
        mPanel1.add(mButtonChangeDcc1, new GridConstraints(3, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mButtonChangeDcc2 = new JButton();
        mButtonChangeDcc2.setText("Change");
        mPanel1.add(mButtonChangeDcc2, new GridConstraints(4, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mButtonStop = new JButton();
        mButtonStop.setText("Stop Script");
        mPanel1.add(mButtonStop, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        mPanel1.add(separator1, new GridConstraints(2, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        mPanel1.add(separator2, new GridConstraints(5, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mPanel1;
    }

    // ---------------------------------------------------------------------------------

}
