/*
 * Project: Conductor
 * Copyright (C) 2019 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
//file:noinspection GrMethodMayBeStatic
package com.alflabs.conductor.v2.ui

import com.alflabs.conductor.v2.IActivableDisplayAdapter
import com.alflabs.conductor.v2.ISensorDisplayAdapter
import com.alflabs.conductor.v2.IThrottleDisplayAdapter
import groovy.swing.SwingBuilder
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.swing.JSVGCanvas
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter
import org.apache.batik.swing.gvt.GVTTreeRendererEvent
import org.apache.batik.util.XMLResourceDescriptor
import org.w3c.dom.Element
import org.w3c.dom.events.EventListener
import org.w3c.dom.svg.SVGDocument
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGStylable

import javax.swing.*
import javax.swing.text.*
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.List
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

/*
 * Some reading to understand this:
 * https://mrhaki.blogspot.com/2009/11/groovy-goodness-building-gui-with.html
 * https://uberconf.com/blog/andres_almiray/2009/11/building_rich_swing_applications_with_groovy__part_i
 *
 * All the default swing layouts:
 * https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
 */

import static java.awt.GridBagConstraints.*
import static javax.swing.ScrollPaneConstants.*
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE

class StatusWindow2 {
    private static final String TAG = "Ui2"
    private def VERBOSE = true
    def IWindowCallback mWindowCallback
    def SwingBuilder mSwingBuilder
    def JFrame mFrame
    def JTextField mScriptNameField
    def JTextArea mLogField
    def JScrollPane mLogScroller
    def JScrollPane mSimuScroller
    def JTextArea mLogSimul
    def JPanel mTopPanel
    def JPanel mThrottlePanel
    def JPanel mSensorPanel
    def JSVGCanvas mSvgCanvas
    def JButton mPauseButton
    def JButton mQuitButton
    def JCheckBox mKioskCheck
    def JCheckBox mFlakyCheck
    private def mIsSimulation = true
    private def mOnRenderCompleted
    private final Map mBlockColorMap = new HashMap()
    private final Queue<Runnable> mModifSvgQueue = new ConcurrentLinkedQueue<>()
    private final List<Runnable> mUpdaters = new ArrayList<>()
    private final def midColumnW = 125

    void open(IWindowCallback windowCallback) {
        mWindowCallback = windowCallback
        mSwingBuilder = new SwingBuilder()
        mSwingBuilder.registerBeanFactory("svgCanvas", JSVGCanvas)
        mSwingBuilder.edt {
            mFrame = frame(title: "Conductor v2",
                    preferredSize: new Dimension(1000, 500),
                    minimumSize: new Dimension(300, 300),
                    // locationRelativeTo: null,
                    pack: true,
                    show: true) {
                gridBagLayout()
                final def inset = [5, 5, 5, 5]
                final def wsvg = 4
                final def wlog = 4
                final def wmid = 2
                final def wbtn = 3
                final def wx = wsvg + wlog + wmid
                final def hthl = 1
                final def hsen = 1
                final def hbtn = 1
                final def hsvg = hthl + hsen + hbtn
                final def hy = 1 + hsvg + 1
                // Top
                mTopPanel = panel(constraints: gbc(gridx:0, gridy: 0,
                        gridwidth: wx, gridheight: 1,
                        fill: HORIZONTAL, insets: inset)) {
                    gridBagLayout()
                    mScriptNameField = textField(text: "Script1 Name", editable: false,
                            constraints: gbc(gridx: 0, gridy: 0,
                                    gridwidth: wx-wbtn, fill: HORIZONTAL, insets: inset, weightx: 1))
                    button(text: "Reload", constraints: gbc(gridx: wx-wbtn, gridy: 0, insets: inset, weightx: 0),
                            actionPerformed: { evt -> onReload() })
                    mPauseButton =
                        button(text: "Pause", constraints: gbc(gridx: wx-wbtn+1, gridy: 0, insets: inset, weightx: 0),
                            actionPerformed: { evt -> windowCallback.onWindowPause() })
                    mQuitButton = button(text: "Quit", constraints: gbc(gridx: wx-wbtn+2, gridy: 0, insets: inset, weightx: 0),
                            actionPerformed: { evt -> onHideOrQuit() })
                }

                // Middle: SVG Map
                mSvgCanvas = svgCanvas(background: Color.DARK_GRAY,
                        constraints: gbc(gridx: 0, gridy: 1,
                                gridwidth: wsvg, gridheight: hsvg,
                                fill: BOTH,
                                minHeight: 200, insets: inset,
                                weightx: 1, weighty: 1))

                // Below Map
                // Bottom Simulator Log
                mSimuScroller = scrollPane(verticalScrollBarPolicy: VERTICAL_SCROLLBAR_AS_NEEDED,
                        horizontalScrollBarPolicy: HORIZONTAL_SCROLLBAR_AS_NEEDED,
                        constraints: gbc(gridx: 0, gridy: hsvg + 1,
                                gridwidth: wsvg + 1, gridheight: 1,
                                fill: BOTH,
                                minHeight: 300,
                                insets: inset,
                                weightx: 1, weighty: 0.3)) {
                    mLogSimul = textArea(text: "Simulator output\nLine 2\nLine 3",
                            rows: 4,
                            editable: false,
                            lineWrap: true, wrapStyleWord: true)
                    mLogSimul.caretPosition = 0
                }

                // Mid column
                mThrottlePanel = panel(constraints: gbc(gridx: wsvg, gridy: 1,
                        gridwidth: 1, gridheight: hthl,
                        fill: BOTH,
                        insets: inset, weightx: 0, weighty: 1)) {
                    boxLayout(axis: BoxLayout.Y_AXIS)
                    textField(text: "No DCC Throttle", editable: false)
                }
                mSensorPanel = panel(constraints: gbc(gridx: wsvg, gridy: 1 + hthl,
                        gridwidth: 1, gridheight: hsen,
                        fill: VERTICAL,
                        insets: inset, weightx: 0, weighty: 1)) {
                    boxLayout(axis: BoxLayout.Y_AXIS)
                    checkBox(text: "No JMRI Sensor", selected: false)
                }
                panel(constraints: gbc(gridx: wsvg, gridy: 1 + hthl + hsen,
                        gridwidth: 1, gridheight: hbtn,
                        fill: VERTICAL, anchor: PAGE_END,
                        insets: inset, weightx: 0, weighty: 0)) {
                    boxLayout(axis: BoxLayout.Y_AXIS)
                    mFlakyCheck = checkBox(text: "Flaky", selected: false,
                            actionPerformed: { evt -> onFlakyChanged() })
                    mKioskCheck = checkBox(text: "Kiosk Mode", selected: false,
                            actionPerformed: { evt -> onKioskChanged() })
                }

                // Side Log
                mLogScroller = scrollPane(verticalScrollBarPolicy: VERTICAL_SCROLLBAR_ALWAYS,
                        horizontalScrollBarPolicy: HORIZONTAL_SCROLLBAR_AS_NEEDED,
                        constraints: gbc(gridx: wx-wlog, gridy: 1,
                                gridwidth: wlog, gridheight: hsvg + 1,
                                fill: BOTH,
                                minWidth: 200, minHeight: 200,
                                insets: inset,
                                weightx: 1, weighty: 1)) {
                    mLogField = textArea(text: "Log Area",
                            columns: 100,
                            editable: false, lineWrap: true, wrapStyleWord: true)
                    mLogField.caretPosition = 0
                }
            }

            mFrame.defaultCloseOperation = DO_NOTHING_ON_CLOSE
            mFrame.addWindowListener(new WindowAdapter() {
                @Override
                void windowClosing(WindowEvent windowEvent) {
                    onQuit()
                }
            })
        }

    }

    void onQuit() {
        mFrame.dispose()
        mWindowCallback.onQuit()
    }

    void onHideOrQuit() {
        if (mIsSimulation) {
            onQuit()
        } else {
            // Minimize the window
            changeFrameState(/*rm*/ JFrame.MAXIMIZED_BOTH, /*add*/ JFrame.ICONIFIED)
        }
    }

    void onReload() {
        clearUpdates()
        clearSvgMap()
        registerThrottles(Collections.emptyList())
        registerActivables(
                /* sensors= */ Collections.emptyList(),
                /* blocks= */ Collections.emptyList(),
                /* turnouts= */ Collections.emptyList())
        mSwingBuilder.doLater {
            mWindowCallback.onWindowReload()
        }
        updatePause(false)
    }

    void onKioskChanged() {
        def isKiosk = mKioskCheck.selected
        if (isKiosk) {
            mTopPanel.visible = false
            mSimuScroller.visible = false
            mLogScroller.visible = false
            changeFrameState(/*rm*/ JFrame.ICONIFIED, /*add*/ JFrame.MAXIMIZED_BOTH)
            mFrame.setAlwaysOnTop(true)
        } else {
            mTopPanel.visible = true
            mSimuScroller.visible = true
            mLogScroller.visible = true
            onSimulationModeChanged()
            changeFrameState(/*rm*/ JFrame.MAXIMIZED_BOTH, /*add*/ JFrame.NORMAL)
            mFrame.setAlwaysOnTop(false)
        }
    }

    void onFlakyChanged() {
        mWindowCallback.onFlaky(mFlakyCheck.selected)
    }

    void onSimulationModeChanged() {
        mSimuScroller.visible = mIsSimulation
        mFlakyCheck.visible = mIsSimulation
        mQuitButton.text = mIsSimulation ? "Quit" : "Hide"
    }

    void changeFrameState(int removeState, int addState) {
        def state = (mFrame.getExtendedState() | addState) & ~removeState
        mFrame.setExtendedState(state)
    }

    void enterKioskMode() {
        mSwingBuilder.doLater {
            mKioskCheck.selected = true
            onKioskChanged()
        }
    }

    void setSimulationMode(boolean isSimulation) {
        mIsSimulation = isSimulation
        mSwingBuilder.doLater {
            onSimulationModeChanged()
        }
    }

    void updatePause(boolean isPaused) {
        mSwingBuilder.doLater {
            mPauseButton.text = isPaused ? "Continue" : "Pause"
        }
    }

    void updateScriptName(String scriptName) {
        mSwingBuilder.doLater {
            mScriptNameField.text = scriptName
        }
    }

    void updateMainLog(String logText) {
        mSwingBuilder.doLater {
            def p = mLogField.caretPosition
            mLogField.text = logText
            mLogField.caretPosition = Math.min(p, logText.size())
        }
    }

    void updateSimuLog(String logText) {
        mSwingBuilder.doLater {
            def p = mLogSimul.caretPosition
            mLogSimul.text = logText
            mLogSimul.caretPosition = Math.min(p, logText.size())
        }
    }

    void clearUpdates() {
        mSwingBuilder.doLater {
            mUpdaters.clear()
        }
    }

    void registerThrottles(List<IThrottleDisplayAdapter> throttles) {
        mSwingBuilder.doLater {
            if (VERBOSE) println(TAG + "registerThrottles # " + throttles.size())
            mThrottlePanel.removeAll()
            if (throttles.empty) {
                // Box Dimensions = MinSize / PreferredSize / MaxSize = given size.
                mThrottlePanel.add(Box.createRigidArea(new Dimension(midColumnW, 45)))
            } else {
                for (final def throttle in throttles) {
                    addThrottle(throttle)
                }
            }
            mFrame.pack()
        }
    }

    private void addThrottle(IThrottleDisplayAdapter throttleAdapter) {
        def wx = mSwingBuilder.textPane(
                text: throttleAdapter.name,
                opaque: false,
                editable: false)
        mThrottlePanel.add(wx)
        mThrottlePanel.setMinimumSize(new Dimension(midColumnW, 40))
        mThrottlePanel.setBackground(Color.LIGHT_GRAY)

        StyleContext context = new StyleContext()
        StyledDocument doc = new DefaultStyledDocument(context)

        Style d = context.getStyle(StyleContext.DEFAULT_STYLE)
        Style c = context.addStyle("c", d)
        c.addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_CENTER)
        Style b = context.addStyle("b", c)
        b.addAttribute(StyleConstants.Bold, true)

        wx.setDocument(doc)

        Runnable updater = { -> updateThrottlePane(wx, throttleAdapter) }
        updater.run()
        mUpdaters.add(updater)
    }

    private void updateThrottlePane(JTextComponent textPane, IThrottleDisplayAdapter throttleAdapter) {
        int speed = throttleAdapter.getSpeed()

        String line1 = String.format("%s [%d] %s %d\n",
                throttleAdapter.getName(),
                throttleAdapter.getDccAddress(),
                speed < 0 ? " << " : (speed == 0 ? " == " : " >> "),
                speed)

        String line2 = String.format("L%s S%s",
                throttleAdapter.isLight() ? "+" : "-",
                throttleAdapter.isSound() ? "+" : "-")

        try {
            StyledDocument doc = (StyledDocument) textPane.getDocument()
            doc.remove(0, doc.getLength())
            doc.insertString(0, line1, null)
            doc.insertString(doc.getLength(), line2, doc.getStyle("b"))
            // everything is centered
            doc.setParagraphAttributes(0, doc.getLength(), doc.getStyle("c"), false /*replace*/)
        } catch (BadLocationException e) {
            throw new RuntimeException(e)
        }
    }

    void registerActivables(
            List<ISensorDisplayAdapter> sensors,
            List<IActivableDisplayAdapter> blocks,
            List<IActivableDisplayAdapter> turnouts) {
        mSwingBuilder.doLater {
            if (VERBOSE) println(TAG + "register UI Updates.")
            mSensorPanel.removeAll()

            for (final def sensor in sensors) {
                addSensor(sensor)
            }

            for (final def block in blocks) {
                addBlock(block)
            }

            for (final def turnout in turnouts) {
                addTurnout(turnout)
            }

            mFrame.pack()
        }
    }

    void addSensor(ISensorDisplayAdapter sensorAdapter) {
        def wx = mSwingBuilder.checkBox(text: sensorAdapter.name, selected: false)
        mSensorPanel.add(wx)

        Runnable updater = { -> wx.setSelected(sensorAdapter.active) }
        updater.run()
        mUpdaters.add(updater)

        wx.addActionListener(actionEvent -> sensorAdapter.setActive(wx.isSelected()))
    }

    void addBlock(IActivableDisplayAdapter adapter) {
        Runnable updater = { ->
            String name = "S-" + adapter.name
            setBlockColor(name, adapter.active, adapter.blockState)
        }
        updater.run()
        mUpdaters.add(updater)
    }

    void addTurnout(IActivableDisplayAdapter adapter) {
        Runnable updater = { ->
            boolean normal = adapter.active
            String name = "T-" + adapter.name
            String N = name + "N"
            String R = name + "R"

            setTurnoutVisible(N,  normal)
            setTurnoutVisible(R, !normal)
        }
        updater.run()
        mUpdaters.add(updater)
    }


    void updateUI() {
        mSwingBuilder.doLater {
            for (final def updater in mUpdaters) {
                updater.run()
            }
        }
    }

    void clearSvgMap() {
        mSvgCanvas.stopThenRun {
            mModifSvgQueue.clear()
            mBlockColorMap.clear()

            def emptySvg = """<?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <svg xmlns="http://www.w3.org/2000/svg" version="1.1" viewBox="0 0 50 50" />"""

            String parser = XMLResourceDescriptor.getXMLParserClassName()
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser)
            SVGDocument document = factory.createSVGDocument(
                    "empty", // informational only
                    new ByteArrayInputStream(emptySvg.getBytes("UTF-8")))
            mSvgCanvas.setSVGDocument(document)
        }
    }

    // Fill SVG using svgDocument (as text).
    // If svgDocument is null or empty, rely only on mapUrl.
    // Note: the mapUrl is only used as a string below, however we use java.net.URI
    // to force callers to provide a valid URI.
    void displaySvgMap(String svgDocument, URI mapUrl) {
        mSvgCanvas.stopThenRun {
            mModifSvgQueue.clear()
            mBlockColorMap.clear()
            // Per documentation in JSVGComponentListener, this is invoked from a background thread.
            mOnRenderCompleted = new GVTTreeRendererAdapter() {
                @Override
                void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                    mSvgCanvas.removeGVTTreeRendererListener(mOnRenderCompleted)
                    mOnRenderCompleted = null
                    mWindowCallback.onWindowSvgLoaded()
                    mFrame.pack()

                    def onClick = new EventListener() {
                        @Override
                        void handleEvent(org.w3c.dom.events.Event event) {
                            // Note: This is called on the SVG UpdateManager thread.
                            def target = event.getTarget()
                            if (target instanceof SVGElement) {
                                mSwingBuilder.doLater {
                                    mWindowCallback.onWindowSvgClick(((SVGElement) target).getId())
                                }
                            }
                        }
                    }

                    modifySvg {
                        SVGDocument doc = mSvgCanvas.getSVGDocument()
                        def element = doc.getRootElement()
                        initAllSvgElements(element, onClick)
                    }
                    // updateSvg()
                }
            }
            mSvgCanvas.addGVTTreeRendererListener(mOnRenderCompleted)

            XMLResourceDescriptor.setCSSParserClassName(InkscapeCssParser.class.getName());
            mSvgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC)

            if (svgDocument != null && !svgDocument.isEmpty()) {
                // Parse given document and load it. The URL is only informational
                // and it could be "" or "file:///" for path-less documents.
                String parser = XMLResourceDescriptor.getXMLParserClassName()
                SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser)
                SVGDocument document = factory.createSVGDocument(
                        mapUrl.toString(), // informational only
                        new ByteArrayInputStream(svgDocument.getBytes("UTF-8")))
                mSvgCanvas.setSVGDocument(document)
            } else {
                // Otherwise let the SVG load the given URL (which much be valid)
                mSvgCanvas.setURI(mapUrl.toString())
            }
            if (VERBOSE) println(TAG + "SVG Map loaded from " + mapUrl)
        }
    }

    void initAllSvgElements(element, onClick) {
        while (element != null) {
            if (element.firstChild != null) {
                initAllSvgElements(element.firstChild, onClick)
            }
            if (element instanceof SVGElement) {
                String id = element.getId()
                if (id != null && (id.startsWith("S-NS") || id.startsWith("T-NT"))) {
                    //if (VERBOSE) println(TAG + "Add onClick listener to id = $id") // -- for debugging
                    element.addEventListener("click", onClick, false /* useCapture */)
                }
                if (id.startsWith("Toggle-T")) {
                    if (element instanceof SVGStylable) {
                        element.getStyle()?.setProperty("opacity", "0", "")
                    }
                }
            }

            element = element.getNextSibling()
        }
    }

    void setBlockColor(
            String id,
            boolean active,
            Optional<IActivableDisplayAdapter.BlockState> blockState) {
        modifySvg {
            SVGDocument doc = mSvgCanvas.getSVGDocument()
            Element elem = doc?.getElementById(id)
            // if (VERBOSE) println(TAG + "Set color for block id = $id") // -- for debugging
            if (elem instanceof SVGStylable) {
                // Note: setProperty(String propertyName, String value, String priority)
                def priority = "" // or "important"
                def style = ((SVGStylable) elem).getStyle()
                if (style != null) {
                    def original = mBlockColorMap.get(elem)
                    if (original == null) {
                        def stroke = style.getPropertyValue("stroke")
                        if (!stroke.isEmpty()) {
                            mBlockColorMap.put(elem, stroke)
                            original = stroke
                        }
                    }
                    //    println "_original_stroke $id = " + original.getClass().getSimpleName() + " " + original
                    def rgb = active ? "red" : original
                    if (blockState.present) {
                        switch (blockState.get()) {
                            case IActivableDisplayAdapter.BlockState.BLOCK_EMPTY:
                                break
                            case IActivableDisplayAdapter.BlockState.BLOCK_OCCUPIED: // fallthrough
                            case IActivableDisplayAdapter.BlockState.BLOCK_TRAILING:
                                rgb = active ? "red" : "orange"
                                break
                        }
                    }
                    if (rgb != null) {
                        style.setProperty("stroke", rgb, priority)
                    }
                }
            }
        }
    }

    void setTurnoutVisible(String id, boolean visible) {
        modifySvg {
            SVGDocument doc = mSvgCanvas.getSVGDocument()
            Element elem = doc?.getElementById(id)
            //if (VERBOSE) println(TAG + "Set turnout id = $id") // -- for debugging
            if (elem instanceof SVGStylable) {
                def display = visible ? "inline" : "none"
                ((SVGStylable) elem).getStyle()?.setProperty("display", display, "")
            }
        }
    }

    /**
     * Wrap all modifications to the SVG using this call so that they happen on the UpdateManager.
     *
     * According to the javadoc in UpdateManager, all modifications to the SVG should be
     * done in the UpdateManager's RunnableQueue (cf #getUpdateRunnableQueue()).
     * The SVG will be automatically repainted as necessary.
     *
     * When the window opens and loads the SVG and the first script at the same time, this may
     * be called before the SVG Canvas's UpdateManager is available. In this case runnables are
     * queued and will be added as soon as the update manager exists. That pending list is cleared
     * when a new SVG is loaded.
     */
    void modifySvg(Closure cl) {
        Runnable r = { -> cl.call() }
        def queue = mSvgCanvas?.getUpdateManager()?.getUpdateRunnableQueue()

        if (queue == null) {
            mModifSvgQueue.add(r)
            return
        }

        while (!mModifSvgQueue.isEmpty()) {
            queue.invokeLater(mModifSvgQueue.remove())
        }

        queue.invokeLater(r)
    }
}
