package com.alflabs.conductor.v2.ui

import com.alflabs.conductor.v2.IActivableDisplayAdapter
import com.alflabs.conductor.v2.ISensorDisplayAdapter
import com.alflabs.conductor.v2.IThrottleDisplayAdapter
import org.apache.batik.swing.JSVGCanvas
import org.apache.batik.swing.gvt.GVTTreeRendererListener
import java.awt.Checkbox
import java.awt.Color
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.TextField
import java.net.URI
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField


/*
 * Some reading to understand this:
 * https://mrhaki.blogspot.com/2009/11/groovy-goodness-building-gui-with.html
 * https://uberconf.com/blog/andres_almiray/2009/11/building_rich_swing_applications_with_groovy__part_i
 *
 * All the default swing layouts:
 * https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
 */

class StatusWindow3 : IStatusWindow {
    private val TAG = javaClass.simpleName
    private var isSimulation = true
    private lateinit var windowCallback: IWindowCallback
    private lateinit var frame: JFrame
    private lateinit var scriptNameField: JTextField
    private lateinit var logField: JTextArea
    private lateinit var logScroller: JScrollPane
    private lateinit var simuScroller: JScrollPane
    private lateinit var logSimul: JTextArea
    private lateinit var topPanel: JPanel
    private lateinit var throttlePanel: JPanel
    private lateinit var sensorPanel: JPanel
    private lateinit var svgCanvas: JSVGCanvas
    private lateinit var pauseButton: JButton
    private lateinit var quitButton: JButton
    private lateinit var kioskCheck: JCheckBox
    private lateinit var flakyCheck: JCheckBox
    private lateinit var onRenderCompleted: GVTTreeRendererListener
    private val mBlockColorMap: Map<*, *> = HashMap<Any?, Any?>()
    private val mModifSvgQueue: Queue<Runnable> = ConcurrentLinkedQueue()
    private val mUpdaters = mutableListOf<Runnable>()
    private val midColumnW = 125


    override fun open(windowCallback: IWindowCallback) {
        this.windowCallback = windowCallback

        frame = JFrame("Conductor v2").apply {
            preferredSize = Dimension(1000, 500)
            minimumSize = Dimension(300, 300)
            layout = GridBagLayout()
            defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
            addWindowStateListener { onQuit() }
        }
        createWidgets()
        frame.apply {
            pack()
            show(true)
        }
    }

    private fun createWidgets() {
        val wsvg = 4
        val wlog = 4
        val wmid = 2
        val wbtn = 3
        val wx = wsvg + wlog + wmid
        val hthl = 1
        val hsen = 1
        val hbtn = 1
        val hsvg = hthl + hsen + hbtn
        val hy = 1 + hsvg + 1
        val insets5 = Insets(5, 5, 5, 5)

        // Top
        topPanel = frame.addWidget(
            JPanel().apply { layout = GridBagLayout() },
            GridBagConstraints().apply {
                fill = GridBagConstraints.HORIZONTAL
                insets = insets5
                gridx = 0
                gridy = 0
                gridwidth = wx
                gridheight = 1
            }
        ) {
            scriptNameField = addWidget(
                JTextField("Script1 Name"). apply { isEditable = false },
                GridBagConstraints().apply {
                    fill = GridBagConstraints.HORIZONTAL
                    insets = insets5
                    gridx = 0
                    gridy = 0
                    gridwidth = wx - wbtn
                    weightx = 1.0
                }
            )

            addWidget(
                JButton("Reload"). apply { addActionListener { onReload() } },
                GridBagConstraints().apply {
                    insets = insets5
                    gridx = wx - wbtn
                    gridy = 0
                    weightx = 0.0
                }
            )

            pauseButton = addWidget(
                JButton("Pause"). apply { addActionListener { windowCallback.onWindowPause() } },
                GridBagConstraints().apply {
                    insets = insets5
                    gridx = wx - wbtn + 1
                    gridy = 0
                    weightx = 0.0
                }
            )

            quitButton = addWidget(
                JButton("Quit"). apply { addActionListener { onHideOrQuit() } },
                GridBagConstraints().apply {
                    fill = GridBagConstraints.HORIZONTAL
                    insets = insets5
                    gridx = wx - wbtn + 2
                    gridy = 0
                    weightx = 0.0
                }
            )
        }

        // Middle: SVG Map
        svgCanvas = frame.addWidget(
            JSVGCanvas().apply { background = Color.DARK_GRAY },
            GridBagConstraints().apply {
                fill = GridBagConstraints.BOTH
                insets = insets5
                gridx = 0
                gridy = 1
                gridwidth = wsvg
                gridheight = hsvg
                weightx = 1.0
                weighty = 1.0
            }
        ) {}

        // Below Map
        // Bottom Simulator Log
        simuScroller = frame.addWidget(
            JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
            GridBagConstraints().apply {
                fill = GridBagConstraints.BOTH
                insets = insets5
                gridx = 0
                gridy = hsvg + 1
                gridwidth = wsvg + 1
                gridheight = 1
                weightx = 1.0
                weighty = 0.3
            }
        ) {
            logSimul = addWidget(JTextArea("Simulator output\nLine 2\nLine 3").apply {
                    rows = 4
                    isEditable = false
                    lineWrap = true
                    wrapStyleWord = true
                    caretPosition = 0
                    minimumSize = Dimension(200, 300)
                })
        }

        // Mid column
        throttlePanel = frame.addWidget(
            JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) },
            GridBagConstraints().apply {
                fill = GridBagConstraints.BOTH
                insets = insets5
                gridx = wsvg
                gridy = 1
                weightx = 0.0
                weighty = 1.0
            }
        ) {
            addWidget(JTextField("No DCC Throttle").apply { isEditable = false })
        }

        sensorPanel = frame.addWidget(
            JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) },
            GridBagConstraints().apply {
                fill = GridBagConstraints.VERTICAL
                insets = insets5
                gridx = wsvg
                gridy = 1 + hthl
                gridwidth = 1
                gridheight = hsen
                weightx = 0.0
                weighty = 1.0
            }
        ) {
            addWidget(JCheckBox("No DCC Throttle").apply { isSelected = false })
        }

        frame.addWidget(
            JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) },
            GridBagConstraints().apply {
                fill = GridBagConstraints.VERTICAL
                anchor = GridBagConstraints.PAGE_END
                insets = insets5
                gridx = wsvg
                gridy = 1 + hthl + hsen
                gridwidth = 1
                gridheight = hbtn
                weightx = 0.0
                weighty = 0.0
            }
        ) {
            flakyCheck = addWidget(JCheckBox("Flaky").apply {
                isSelected = false
                addActionListener { onFlakyChanged() }
            })
            kioskCheck = addWidget(JCheckBox("Kiosk Mode").apply {
                isSelected = false
                addActionListener { onKioskChanged() }
            })
        }

        // Side Log
        logScroller = frame.addWidget(
            JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
            GridBagConstraints().apply {
                fill = GridBagConstraints.BOTH
                insets = insets5
                gridx = wx - wlog
                gridy = 1
                gridwidth = wlog
                gridheight = hsvg + 1
                weightx = 1.0
                weighty = 1.0
            }
        ) {
            logField = addWidget(JTextArea("Log Area").apply {
                columns = 100
                isEditable = false
                lineWrap = true
                wrapStyleWord = true
                caretPosition = 0
                minimumSize = Dimension(200, 200)
            })
        }
    }

    private fun <C : JComponent> JFrame.addWidget(
            instance: C,
            constraints: GridBagConstraints,
            block: C.() -> Unit) : C {
        this.add(instance, constraints)
        instance.block()
        return instance
    }

    private fun <C : JComponent> JComponent.addWidget(
        instance: C,
        constraints: GridBagConstraints? = null) : C {
        this.add(instance, constraints)
        return instance
    }


    override fun updateScriptName(scriptName: String) {
        println("StatusWindow3 - Not yet implemented - updateScriptName")
    }

    override fun setSimulationMode(isSimulation: Boolean) {
        println("StatusWindow3 - Not yet implemented - setSimulationMode")
    }

    override fun displaySvgMap(svgDocument: String?, mapUrl: URI) {
        println("StatusWindow3 - Not yet implemented - displaySvgMap")
        windowCallback.onWindowSvgLoaded() // TEMP
    }

    override fun updateUI() {
        println("StatusWindow3 - Not yet implemented - updateUI")
    }

    override fun updateMainLog(logText: String) {
        println("StatusWindow3 - Not yet implemented - updateMainLog")
    }

    override fun updateSimuLog(logText: String) {
        println("StatusWindow3 - Not yet implemented - updateSimuLog")
    }

    override fun updatePause(isPaused: Boolean) {
        println("StatusWindow3 - Not yet implemented - updatePause")
    }

    override fun clearUpdates() {
        println("StatusWindow3 - Not yet implemented - clearUpdates")
    }

    override fun registerThrottles(throttles: MutableList<IThrottleDisplayAdapter>) {
        println("StatusWindow3 - Not yet implemented - registerThrottles")
    }

    override fun registerActivables(
        sensors: MutableList<ISensorDisplayAdapter>,
        blocks: MutableList<IActivableDisplayAdapter>,
        turnouts: MutableList<IActivableDisplayAdapter>
    ) {
        println("StatusWindow3 - Not yet implemented - registerActivables")
    }

    private fun onReload() {
        println("StatusWindow3 - Not yet implemented - onReload")
//        clearUpdates()
//        clearSvgMap()
//        registerThrottles(Collections.emptyList())
//        registerActivables(
//            /* sensors= */ Collections.emptyList(),
//            /* blocks= */ Collections.emptyList(),
//            /* turnouts= */ Collections.emptyList())
//        mSwingBuilder.doLater {
//            mWindowCallback.onWindowReload()
//        }
//        updatePause(false)
    }

    private fun onQuit() {
        frame.dispose()
        windowCallback.onQuit()
    }

    private fun onHideOrQuit() {
        println("StatusWindow3 - Not yet implemented - onHideOrQuit")

        if (isSimulation) {
            onQuit()
        } else {
            // Minimize the window
            changeFrameState(/*rm*/ JFrame.MAXIMIZED_BOTH, /*add*/ JFrame.ICONIFIED)
        }
    }

    private fun onKioskChanged() {
        println("StatusWindow3 - Not yet implemented - onKioskChanged")
//        def isKiosk = mKioskCheck.selected
//                if (isKiosk) {
//                    mTopPanel.visible = false
//                    mSimuScroller.visible = false
//                    mLogScroller.visible = false
//                    changeFrameState(/*rm*/ JFrame.ICONIFIED, /*add*/ JFrame.MAXIMIZED_BOTH)
//                    mFrame.setAlwaysOnTop(true)
//                } else {
//                    mTopPanel.visible = true
//                    mSimuScroller.visible = true
//                    mLogScroller.visible = true
//                    onSimulationModeChanged()
//                    changeFrameState(/*rm*/ JFrame.MAXIMIZED_BOTH, /*add*/ JFrame.NORMAL)
//                    mFrame.setAlwaysOnTop(false)
//                }
    }

    private fun onFlakyChanged() {
        println("StatusWindow3 - Not yet implemented - onFlakyChanged")
//        mWindowCallback.onFlaky(mFlakyCheck.selected)
    }

    private fun onSimulationModeChanged() {
        println("StatusWindow3 - Not yet implemented - onSimulationModeChanged")
//        mSimuScroller.visible = mIsSimulation
//        mFlakyCheck.visible = mIsSimulation
//        mQuitButton.text = mIsSimulation ? "Quit" : "Hide"
    }

    private fun changeFrameState(removeState: Int, addState: Int) {
        println("StatusWindow3 - Not yet implemented - changeFrameState")
        // Java: state = (mFrame.getExtendedState() | addState) & ~removeState
        val state = (frame.extendedState or addState) and removeState.inv()
        frame.setExtendedState(state)
    }

    override fun enterKioskMode() {
        println("StatusWindow3 - Not yet implemented - enterKioskMode")

//        mSwingBuilder.doLater {
//            mKioskCheck.selected = true
//            onKioskChanged()
//        }
    }

}
