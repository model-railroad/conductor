package com.alflabs.conductor.v2.ui

import com.alflabs.conductor.v2.IActivableDisplayAdapter
import com.alflabs.conductor.v2.ISensorDisplayAdapter
import com.alflabs.conductor.v2.IThrottleDisplayAdapter
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.swing.JSVGCanvas
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter
import org.apache.batik.swing.gvt.GVTTreeRendererEvent
import org.apache.batik.swing.gvt.GVTTreeRendererListener
import org.apache.batik.util.XMLResourceDescriptor
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.css.CSSStyleDeclaration
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.EventTarget
import org.w3c.dom.svg.SVGDocument
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGStylable
import java.awt.Color
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.ByteArrayInputStream
import java.net.URI
import java.util.Collections
import java.util.Optional
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.JTextComponent
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import javax.swing.text.StyledDocument
import kotlin.math.min

/*
 * Some reading to understand this:
 * https://mrhaki.blogspot.com/2009/11/groovy-goodness-building-gui-with.html
 * https://uberconf.com/blog/andres_almiray/2009/11/building_rich_swing_applications_with_groovy__part_i
 *
 * All the default swing layouts:
 * https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
 */

class StatusWindow2k : IStatusWindow {
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
    private var onRenderCompleted: GVTTreeRendererListener? = null
    private val blockColorMap = mutableMapOf<Element, String>()
    private val modifSvgQueue: Queue<Runnable> = ConcurrentLinkedQueue()
    private val mUpdaters = mutableListOf<Runnable>()

    companion object {
        // Version number should match jmri/conductor/gradle.properties
        private const val VERSION = "2.9"
        private const val VERBOSE = true
        private const val midColumnW = 150
        private const val midThrottleH = 40
        private const val midColumnH = midThrottleH * 4
    }

    override fun open(windowCallback: IWindowCallback) {
        this.windowCallback = windowCallback

        frame = JFrame("Conductor v$VERSION").apply {
            preferredSize = Dimension(1000, 600)
            minimumSize = Dimension(300, 300)
            layout = GridBagLayout()
            defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
            addWindowStateListener(object : WindowAdapter() {
                override fun windowClosing(event: WindowEvent?) {
                    onQuit()
                }
            })
        }
        createContent()
        frame.apply {
            pack()
            isVisible = true
        }
    }

    private fun createContent() {
        val wsvg = 4
        val wlog = 4
        val wmid = 2
        val wbtn = 3
        val wx = wsvg + wlog + wmid
        val hthl = 4
        val hsen = 3
        val hbtn = 2
        val hsvg = hthl + hsen + hbtn
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
        ) {
            minimumSize = Dimension(200, 200)
        }

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
                weighty = 0.15
            }
        ) {
            minimumSize = Dimension(200, 50)
            logSimul = addWidget(JTextArea("Simulator output\nLine 2\nLine 3").apply {
                    rows = 4
                    isEditable = false
                    lineWrap = true
                    wrapStyleWord = true
                    caretPosition = 0
                })
        }

        // Mid column
        frame.addWidget(
            JPanel().apply { layout = GridBagLayout() },
            GridBagConstraints().apply {
                fill = GridBagConstraints.BOTH
                gridx = wsvg
                gridy = 1
                gridwidth = 1
                gridheight = hsvg
            }
        ) {
            minimumSize = Dimension(midColumnW, midColumnH)
            throttlePanel = addWidget(
                JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) },
                GridBagConstraints().apply {
                    fill = GridBagConstraints.BOTH
                    insets = insets5
                    gridx = 0
                    gridy = 0
                    gridwidth = 1
                    gridheight = hthl
                    weightx = 1.0
                    weighty = 0.0
                }
            ) {
                border = BorderFactory.createLineBorder(Color.LIGHT_GRAY)
                addWidget(JTextField("No DCC Throttle").apply { isEditable = false })
            }

            sensorPanel = addWidget(
                JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) },
                GridBagConstraints().apply {
                    fill = GridBagConstraints.BOTH
                    anchor = GridBagConstraints.LINE_START
                    insets = insets5
                    gridx = 0
                    gridy = hthl
                    gridwidth = 1
                    gridheight = hsen
                    weightx = 1.0
                    weighty = 1.0
                }
            ) {
                addWidget(JCheckBox("No Sensor").apply { isSelected = false })
            }

            addWidget(
                JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) },
                GridBagConstraints().apply {
                    fill = GridBagConstraints.HORIZONTAL
                    anchor = GridBagConstraints.LAST_LINE_START
                    insets = insets5
                    gridx = 0
                    gridy = hthl + hsen
                    gridwidth = 1
                    gridheight = hbtn
                    weightx = 1.0
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
            minimumSize = Dimension(200, 200)
            logField = addWidget(JTextArea("Log Area").apply {
                columns = 100
                isEditable = false
                lineWrap = true
                wrapStyleWord = true
                caretPosition = 0
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

    private fun <C : JComponent> JPanel.addWidget(
        instance: C,
        constraints: GridBagConstraints? = null) : C {
        this.add(instance, constraints)
        return instance
    }

    private fun <C : JPanel> JPanel.addWidget(
        instance: C,
        constraints: GridBagConstraints,
        block: C.() -> Unit) : C {
        this.add(instance, constraints)
        instance.block()
        return instance
    }

    private fun <C : JComponent> JScrollPane.addWidget(instance: C) : C {
        this.setViewportView(instance)
        return instance
    }

    override fun updateScriptName(scriptName: String) {
        SwingUtilities.invokeLater {
            scriptNameField.text = scriptName
        }
    }

    override fun setSimulationMode(isSimulation: Boolean) {
        this.isSimulation = isSimulation
        SwingUtilities.invokeLater {
            onSimulationModeChanged()
        }
    }

    override fun updateMainLog(logText: String) {
        SwingUtilities.invokeLater {
            val pos = logField.caretPosition
            logField.text = logText
            logField.caretPosition = min(pos, logText.length)
        }
    }

    override fun updateSimuLog(logText: String) {
        SwingUtilities.invokeLater {
            val pos = logSimul.caretPosition
            logSimul.text = logText
            logSimul.caretPosition = min(pos, logText.length)
        }
    }

    override fun updatePause(isPaused: Boolean) {
        SwingUtilities.invokeLater {
            pauseButton.text = if (isPaused) "Continue" else "Pause"
        }
    }

    override fun updateUI() {
        SwingUtilities.invokeLater {
            mUpdaters.forEach { it.run() }
        }
    }

    override fun clearUpdates() {
        SwingUtilities.invokeLater {
            mUpdaters.clear()
        }
    }

    private fun onReload() {
        clearUpdates()
        clearSvgMap()
        registerThrottles(Collections.emptyList())
        registerActivables(
            sensors  = Collections.emptyList(),
            blocks   = Collections.emptyList(),
            turnouts = Collections.emptyList())
        SwingUtilities.invokeLater {
            windowCallback.onWindowReload()
        }
        updatePause(false)
    }

    private fun onQuit() {
        frame.dispose()
        windowCallback.onQuit()
    }

    private fun onHideOrQuit() {
        if (isSimulation) {
            onQuit()
        } else {
            // Minimize the window
            changeFrameState(remove = JFrame.MAXIMIZED_BOTH, add = JFrame.ICONIFIED)
        }
    }

    private fun onKioskChanged() {
        if (kioskCheck.isSelected) {
            topPanel.isVisible = false
            simuScroller.isVisible = false
            logScroller.isVisible = false
            changeFrameState(remove = JFrame.ICONIFIED, add = JFrame.MAXIMIZED_BOTH)
            frame.setAlwaysOnTop(true)
        } else {
            topPanel.isVisible = true
            simuScroller.isVisible = true
            logScroller.isVisible = true
            onSimulationModeChanged()
            frame.pack()
            changeFrameState(remove = JFrame.MAXIMIZED_BOTH, add = JFrame.NORMAL)
            frame.setAlwaysOnTop(false)
        }
    }

    private fun onFlakyChanged() {
        windowCallback.onFlaky(flakyCheck.isSelected)
    }

    private fun onSimulationModeChanged() {
        simuScroller.isVisible = isSimulation
        flakyCheck.isVisible = isSimulation
        quitButton.text = if (isSimulation) "Quit" else "Hide"
    }

    private fun changeFrameState(remove: Int, add: Int) {
        // Java: state = (mFrame.getExtendedState() | addState) & ~removeState
        val state = (frame.extendedState or add) and remove.inv()
        frame.setExtendedState(state)
    }

    override fun enterKioskMode() {
        SwingUtilities.invokeLater {
            kioskCheck.isSelected = true
            onKioskChanged()
        }
    }

    override fun registerThrottles(throttles: MutableList<IThrottleDisplayAdapter>) {
        SwingUtilities.invokeLater {
            if (VERBOSE) println("$TAG registerThrottles # ${throttles.size}")
            throttlePanel.removeAll()
            var height = 0
            if (throttles.isEmpty()) {
                // Box Dimensions = MinSize / PreferredSize / MaxSize = given size.
                throttlePanel.add(JTextField("No DCC Throttle").apply { isEditable = false })
            } else {
                var bg = true
                throttles.forEach { th ->
                    val wx = addThrottle(th)
                    if (height == 0) {
                        wx.getFontMetrics(wx.font)?.let { fm -> height = fm.height }
                    }
                    if (bg) {
                        wx.isOpaque = true
                        wx.background = Color.LIGHT_GRAY
                    }
                    bg = !bg
                }
            }

            throttlePanel.minimumSize = Dimension(
                midColumnW,
                if (height <= 0) midColumnH else height * throttles.size)
            throttlePanel.topLevelAncestor.validate()
        }
    }

    private fun addThrottle(throttleAdapter: IThrottleDisplayAdapter): JTextPane {
        val context = StyleContext()
        val doc = DefaultStyledDocument(context)

        val d = context.getStyle(StyleContext.DEFAULT_STYLE)
        val c = context.addStyle("center", d)
        c.addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_CENTER)
        val b = context.addStyle("bold", c)
        b.addAttribute(StyleConstants.Bold, true)
        val f = context.addStyle("fwd", c)
        f.addAttribute(StyleConstants.Background, Color.GREEN)
        val r = context.addStyle("rev", c)
        r.addAttribute(StyleConstants.Background, Color.ORANGE)
        val l = context.addStyle("light", b)
        l.addAttribute(StyleConstants.Background, Color.YELLOW)
        val s = context.addStyle("sound", b)
        s.addAttribute(StyleConstants.Background, Color.CYAN)
        val e = context.addStyle("error", c)
        e.addAttribute(StyleConstants.Background, Color.RED)

        val wx = JTextPane(doc).apply {
            text = throttleAdapter.name
            isOpaque = false
            isEditable = false
            minimumSize = Dimension(midColumnW, midThrottleH)
        }
        throttlePanel.add(wx)

        val updater = Runnable { updateThrottlePane(wx, throttleAdapter) }
        updater.run()
        mUpdaters.add(updater)
        return wx
    }

    private fun updateThrottlePane(textPane: JTextComponent,
                                   throttleAdapter: IThrottleDisplayAdapter) {
        val speed = throttleAdapter.speed
        val fwd = speed > 0
        val rev = speed < 0
        val light = throttleAdapter.isLight
        val sound = throttleAdapter.isSound

        val spanSpeed = String.format(" %s [%d] %s %d ",
            throttleAdapter.getName(),
            throttleAdapter.getDccAddress(),
            when {
                speed < 0 -> " < "
                speed > 0 -> " > "
                else      -> " = "
            },
            speed)

        var spanA = ""
        if (throttleAdapter.activationsCount >= 0) {
            spanA = String.format(" #%d • ", throttleAdapter.activationsCount)
        }

        val spanL = String.format(" L%s ", if (light) "+" else "-")
        val spanS = String.format(" S%s ", if (sound) "+" else "-")

        val spanStatus = " ${throttleAdapter.getStatus()} "
        val error = spanStatus.contains("error", ignoreCase = true)

        try {
            val doc = textPane.document as StyledDocument
            doc.remove(0, doc.length)
            doc.insertString(0, spanSpeed, doc.getStyle(
                if (fwd) "fwd" else if (rev) "rev" else "center"))
            doc.insertString(doc.length, "\n", doc.getStyle("center"))
            if (spanA.isNotEmpty()) {
                doc.insertString(doc.length, spanA, doc.getStyle("center"))
            }
            doc.insertString(doc.length, spanL, doc.getStyle(
                if (light) "light" else "center"))
            doc.insertString(doc.length, spanS, doc.getStyle(
                if (sound) "sound" else "center"))
            doc.insertString(doc.length, "\n", doc.getStyle("center"))
            doc.insertString(doc.length, spanStatus, doc.getStyle(
                if (error) "error" else "center"))
            // everything is centered
            doc.setParagraphAttributes(0, doc.length, doc.getStyle("center"), false /*replace*/)
        } catch (e: BadLocationException) {
            throw RuntimeException(e)
        }
    }

    override fun registerActivables(sensors: MutableList<ISensorDisplayAdapter>,
                                    blocks: MutableList<IActivableDisplayAdapter>,
                                    turnouts: MutableList<IActivableDisplayAdapter>
    ) {
        SwingUtilities.invokeLater {
            if (VERBOSE) println("$TAG register UI Updates.")
            sensorPanel.removeAll()
            sensors.forEach  { addSensor(it) }
            blocks.forEach   { addBlock(it) }
            turnouts.forEach { addTurnout(it) }
            frame.pack()
        }
    }

    private fun addSensor(adapter: ISensorDisplayAdapter) {
        val wx = JCheckBox(adapter.name).apply { isSelected = false }
        sensorPanel.add(wx)

        val updater = Runnable { wx.isSelected = adapter.isActive }
        updater.run()
        mUpdaters.add(updater)

        wx.addActionListener { adapter.isActive = wx.isSelected }
    }

    private fun addBlock(adapter: IActivableDisplayAdapter) {
        val updater = Runnable {
            val name = "S-${adapter.name}"
            setBlockColor(name, adapter.isActive, adapter.blockState)
        }
        updater.run()
        mUpdaters.add(updater)
    }

    private fun addTurnout(adapter: IActivableDisplayAdapter) {
        val updater = Runnable {
            val normal = adapter.isActive
            val name = "T-${adapter.name}"
            val N = "${name}N"
            val R = "${name}R"

            setTurnoutVisible(N,  normal)
            setTurnoutVisible(R, !normal)
        }
        updater.run()
        mUpdaters.add(updater)
    }

    private fun clearSvgMap() {
        svgCanvas.stopProcessing()
        modifSvgQueue.clear()
        blockColorMap.clear()
    }

    /**
     * Fill SVG using svgDocument (as text).
     * If svgDocument is null or empty, rely only on mapUrl.
     * Note: the mapUrl is only used as a string below, however we use java.net.URI
     * to force callers to provide a valid URI.
     */
    override fun displaySvgMap(svgDocument: String?, mapUrl: URI) {
        svgCanvas.stopProcessing()
        blockColorMap.clear()
        // Per documentation in JSVGComponentListener, this is invoked from a background thread.
        onRenderCompleted = object : GVTTreeRendererAdapter() {
            override fun gvtRenderingCompleted(e: GVTTreeRendererEvent) {
                svgCanvas.removeGVTTreeRendererListener(onRenderCompleted)
                onRenderCompleted = null
                windowCallback.onWindowSvgLoaded()
                frame.pack()

                val onClick = EventListener { event ->
                    // Note: This is called on the SVG UpdateManager thread.
                    val target = event.target
                    if (target is SVGElement) {
                        SwingUtilities.invokeLater {
                            windowCallback.onWindowSvgClick(target.id)
                        }
                    }
                }

                modifySvg {
                    val doc = svgCanvas.svgDocument
                    val element = doc.rootElement
                    initAllSvgElements(element, onClick)
                }
                // updateSvg()
            }
        }
        svgCanvas.addGVTTreeRendererListener(onRenderCompleted)

        XMLResourceDescriptor.setCSSParserClassName(InkscapeCssParser::class.java.name)
        svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC)

        if (!svgDocument.isNullOrEmpty()) {
            // Parse given document and load it. The URL is only informational,
            // and it could be "" or "file:///" for path-less documents.
            val parser = XMLResourceDescriptor.getXMLParserClassName()
            val factory = SAXSVGDocumentFactory(parser)
            val document: SVGDocument = factory.createSVGDocument(
                mapUrl.toString(),
                ByteArrayInputStream(svgDocument.toByteArray(charset("UTF-8"))))
            svgCanvas.svgDocument = document
        } else {
            // Otherwise let the SVG load the given URL (which much be valid)
            svgCanvas.uri = mapUrl.toString()
        }

        if (VERBOSE) println("$TAG SVG Map loaded from $mapUrl")
    }

    fun initAllSvgElements(rootElement : Node?, onClick: EventListener) {
        var node = rootElement
        while (node != null) {
            if (node.firstChild != null) {
                initAllSvgElements(node.firstChild, onClick)
            }

            if (node is SVGElement) {
                val id: String = node.id
                if (id.startsWith("S-") || id.startsWith("T-")) {
                    //if (VERBOSE) println(TAG + "Add onClick listener to id = $id") // -- for debugging
                    node as EventTarget
                    node.addEventListener("click", onClick, false /* useCapture */)
                }

                if (id.startsWith("Toggle-T")) {
                    if (node is SVGStylable) {
                        node.style?.setProperty("opacity", "0", "")
                    }
                }
            }

            node = node.nextSibling
        }
    }

    private fun setBlockColor(id: String,
                              active: Boolean,
                              blockState: Optional<IActivableDisplayAdapter.BlockState>) {
        modifySvg {
            val doc: SVGDocument? = svgCanvas.svgDocument
            val elem: Element? = doc?.getElementById(id)
            // if (VERBOSE) println(TAG + "Set color for block id = $id") // -- for debugging
            if (elem is SVGStylable) {
                // Note: setProperty(String propertyName, String value, String priority)
                val priority = "" // or "important"
                val style: CSSStyleDeclaration = (elem as SVGStylable).style
                var original = blockColorMap[elem]
                if (original == null) {
                    val stroke = style.getPropertyValue("stroke")
                    if (stroke.isNotEmpty()) {
                        blockColorMap[elem] = stroke
                        original = stroke
                    }
                }

                //    println "_original_stroke $id = " + original.getClass().getSimpleName() + " " + original
                var rgb = if (active) "red" else original
                if (blockState.isPresent) {
                    when (blockState.get()) {
                        IActivableDisplayAdapter.BlockState.BLOCK_EMPTY -> {
                            /* no-op */
                        }
                        IActivableDisplayAdapter.BlockState.BLOCK_OCCUPIED,
                        IActivableDisplayAdapter.BlockState.BLOCK_TRAILING -> {
                            rgb = if (active) "red" else "orange"
                        }
                    }
                }

                if (rgb != null) {
                    style.setProperty("stroke", rgb, priority)
                }
            }
        }
    }

    private fun setTurnoutVisible(id: String, visible: Boolean) {
        modifySvg {
            val doc: SVGDocument? = svgCanvas.svgDocument
            val elem: Element? = doc?.getElementById(id)
            //if (VERBOSE) println(TAG + "Set turnout id = $id") // -- for debugging
            if (elem is SVGStylable) {
                val display = if (visible) "inline" else "none"
                elem.style?.setProperty("display", display, "")
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
     *
     * When the window opens and loads the SVG and the first script at the same time, this may
     * be called before the SVG Canvas's UpdateManager is available. In this case runnables are
     * queued and will be added as soon as the update manager exists. That pending list is cleared
     * when a new SVG is loaded.
     */
    private fun modifySvg(block: () -> Unit) {
        val r = Runnable { block() }
        val queue = svgCanvas.updateManager?.updateRunnableQueue

        if (queue == null) {
            modifSvgQueue.add(r)
            return
        }

        while (!modifSvgQueue.isEmpty()) {
            queue.invokeLater(modifSvgQueue.remove())
        }

        queue.invokeLater(r)
    }

}
