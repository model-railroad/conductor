package com.alflabs.conductor.v2.ui

//import groovy.lang.Closure
//import org.w3c.dom.Element
//import org.w3c.dom.css.CSSStyleDeclaration
//import org.w3c.dom.events.Event
//import org.w3c.dom.events.EventListener
//import java.awt.Color
//import java.io.Serializable
//import java.net.URI
//import java.util.Optional
//import java.util.Queue
//import javax.swing.Box
//import javax.swing.text.Style

class StatusWindow2k {
//    fun open(windowCallback: IWindowCallback?) {
//        mWindowCallback = windowCallback
//        mSwingBuilder = SwingBuilder()
//        mSwingBuilder.registerBeanFactory("svgCanvas", JSVGCanvas::class.java)
//        mSwingBuilder.edt(object : Closure(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null) {
//                val map = LinkedHashMap<String, Any>(5)
//                map["title"] = "Conductor v2"
//                map["preferredSize"] = Dimension(1000, 500)
//                map["minimumSize"] = Dimension(300, 300)
//                map["pack"] = true
//                map["show"] = true
//                setmFrame(
//                    frame(
//                        map,
//                        object : Closure<JScrollPane?>(this@StatusWindow2k, this@StatusWindow2k) {
//                            @JvmOverloads
//                            fun doCall(it: Any? = null): JScrollPane {
//                                gridBagLayout()
//                                val inset = ArrayList(mutableListOf(5, 5, 5, 5))
//                                val wsvg = 4
//                                val wlog = 4
//                                val wmid = 2
//                                val wbtn = 3
//                                val wx = wsvg + wlog + wmid
//                                val hthl = 1
//                                val hsen = 1
//                                val hbtn = 1
//                                val hsvg = hthl + hsen + hbtn
//                                val hy = 1 + hsvg + 1
//                                // Top
//                                val map1 = LinkedHashMap<String, Any>(1)
//                                val map2 = LinkedHashMap<String, Int>(6)
//                                map2["gridx"] = 0
//                                map2["gridy"] = 0
//                                map2["gridwidth"] = getProperty("wx")
//                                map2["gridheight"] = 1
//                                map2["fill"] = getProperty("HORIZONTAL")
//                                map2["insets"] = getProperty("inset")
//                                map1["constraints"] = invokeMethod("gbc", arrayOf<Any>(map2))
//                                setmTopPanel(
//                                    panel(
//                                        map1,
//                                        object : Closure<JButton?>(
//                                            this@StatusWindow2k,
//                                            this@StatusWindow2k
//                                        ) {
//                                            @JvmOverloads
//                                            fun doCall(it: Any? = null): JButton {
//                                                gridBagLayout()
//                                                val map3 = LinkedHashMap<String, Serializable>(3)
//                                                map3["text"] = "Script1 Name"
//                                                map3["editable"] = false
//                                                val map4 = LinkedHashMap<String, Int>(6)
//                                                map4["gridx"] = 0
//                                                map4["gridy"] = 0
//                                                map4["gridwidth"] =
//                                                    getProperty("wx") - getProperty("wbtn")
//                                                map4["fill"] = getProperty("HORIZONTAL")
//                                                map4["insets"] = getProperty("inset")
//                                                map4["weightx"] = 1
//                                                map3["constraints"] =
//                                                    invokeMethod("gbc", arrayOf<Any>(map4))
//                                                setmScriptNameField(textField(map3))
//                                                val map5 = LinkedHashMap<String, Serializable>(3)
//                                                map5["text"] = "Reload"
//                                                val map6 = LinkedHashMap<String, Int>(4)
//                                                map6["gridx"] =
//                                                    getProperty("wx") - getProperty("wbtn")
//                                                map6["gridy"] = 0
//                                                map6["insets"] = getProperty("inset")
//                                                map6["weightx"] = 0
//                                                map5["constraints"] =
//                                                    invokeMethod("gbc", arrayOf<Any>(map6))
//                                                map5["actionPerformed"] =
//                                                    object : Closure(this, this) {
//                                                        fun doCall(evt: Any?): Any {
//                                                            return invokeMethod(
//                                                                "onReload",
//                                                                arrayOfNulls<Any>(0)
//                                                            )
//                                                        }
//                                                    }
//                                                button(map5)
//                                                val map7 = LinkedHashMap<String, Serializable>(3)
//                                                map7["text"] = "Pause"
//                                                val map8 = LinkedHashMap<String, Int>(4)
//                                                map8["gridx"] =
//                                                    getProperty("wx") - getProperty("wbtn") + 1
//                                                map8["gridy"] = 0
//                                                map8["insets"] = getProperty("inset")
//                                                map8["weightx"] = 0
//                                                map7["constraints"] =
//                                                    invokeMethod("gbc", arrayOf<Any>(map8))
//                                                map7["actionPerformed"] =
//                                                    object : Closure(this, this) {
//                                                        fun doCall(evt: Any?): Any {
//                                                            return getProperty("windowCallback").invokeMethod(
//                                                                "onWindowPause",
//                                                                arrayOfNulls<Any>(0)
//                                                            )
//                                                        }
//                                                    }
//                                                setmPauseButton(button(map7))
//                                                val map9 = LinkedHashMap<String, Serializable>(3)
//                                                map9["text"] = "Quit"
//                                                val map10 = LinkedHashMap<String, Int>(4)
//                                                map10["gridx"] =
//                                                    getProperty("wx") - getProperty("wbtn") + 2
//                                                map10["gridy"] = 0
//                                                map10["insets"] = getProperty("inset")
//                                                map10["weightx"] = 0
//                                                map9["constraints"] =
//                                                    invokeMethod("gbc", arrayOf<Any>(map10))
//                                                map9["actionPerformed"] =
//                                                    object : Closure(this, this) {
//                                                        fun doCall(evt: Any?): Any {
//                                                            return invokeMethod(
//                                                                "onHideOrQuit",
//                                                                arrayOfNulls<Any>(0)
//                                                            )
//                                                        }
//                                                    }
//                                                return setmQuitButton0(
//                                                    this@StatusWindow2k,
//                                                    button(map9)
//                                                )
//                                            }
//                                        })
//                                )
//
//                                // Middle: SVG Map
//                                val map3 = LinkedHashMap<String, Serializable>(2)
//                                map3["background"] = Color.DARK_GRAY
//                                val map4 = LinkedHashMap<String, Int>(9)
//                                map4["gridx"] = 0
//                                map4["gridy"] = 1
//                                map4["gridwidth"] = getProperty("wsvg")
//                                map4["gridheight"] = getProperty("hsvg")
//                                map4["fill"] = getProperty("BOTH")
//                                map4["minHeight"] = 200
//                                map4["insets"] = getProperty("inset")
//                                map4["weightx"] = 1
//                                map4["weighty"] = 1
//                                map3["constraints"] = gbc(map4)
//                                setmSvgCanvas(invokeMethod("svgCanvas", arrayOf<Any>(map3)))
//
//                                // Below Map
//                                // Bottom Simulator Log
//                                val map5 = LinkedHashMap<String, Any>(3)
//                                map5["verticalScrollBarPolicy"] =
//                                    getProperty("VERTICAL_SCROLLBAR_AS_NEEDED")
//                                map5["horizontalScrollBarPolicy"] =
//                                    getProperty("HORIZONTAL_SCROLLBAR_AS_NEEDED")
//                                val map6 = LinkedHashMap<String, Number>(9)
//                                map6["gridx"] = 0
//                                map6["gridy"] = getProperty("hsvg") + 1
//                                map6["gridwidth"] = getProperty("wsvg") + 1
//                                map6["gridheight"] = 1
//                                map6["fill"] = getProperty("BOTH")
//                                map6["minHeight"] = 300
//                                map6["insets"] = getProperty("inset")
//                                map6["weightx"] = 1
//                                map6["weighty"] = 0.3
//                                map5["constraints"] = invokeMethod("gbc", arrayOf<Any>(map6))
//                                setmSimuScroller(
//                                    scrollPane(
//                                        map5,
//                                        object : Closure<Int?>(
//                                            this@StatusWindow2k,
//                                            this@StatusWindow2k
//                                        ) {
//                                            @JvmOverloads
//                                            fun doCall(it: Any? = null): Int {
//                                                val map7 = LinkedHashMap<String, Serializable>(5)
//                                                map7["text"] = "Simulator output\nLine 2\nLine 3"
//                                                map7["rows"] = 4
//                                                map7["editable"] = false
//                                                map7["lineWrap"] = true
//                                                map7["wrapStyleWord"] = true
//                                                setmLogSimul(textArea(map7))
//                                                return setCaretPosition(getmLogSimul(), 0)
//                                            }
//                                        })
//                                )
//
//                                // Mid column
//                                val map7 = LinkedHashMap<String, Any>(1)
//                                val map8 = LinkedHashMap<String, Int>(8)
//                                map8["gridx"] = getProperty("wsvg")
//                                map8["gridy"] = 1
//                                map8["gridwidth"] = 1
//                                map8["gridheight"] = getProperty("hthl")
//                                map8["fill"] = getProperty("BOTH")
//                                map8["insets"] = getProperty("inset")
//                                map8["weightx"] = 0
//                                map8["weighty"] = 1
//                                map7["constraints"] = invokeMethod("gbc", arrayOf<Any>(map8))
//                                setmThrottlePanel(
//                                    panel(
//                                        map7,
//                                        object : Closure<JTextField?>(
//                                            this@StatusWindow2k,
//                                            this@StatusWindow2k
//                                        ) {
//                                            @JvmOverloads
//                                            fun doCall(it: Any? = null): JTextField {
//                                                val map9 = LinkedHashMap<String, Any>(1)
//                                                map9["axis"] = getProperty("BoxLayout").Y_AXIS
//                                                boxLayout(map9)
//                                                val map10 = LinkedHashMap<String, Serializable>(2)
//                                                map10["text"] = "No DCC Throttle"
//                                                map10["editable"] = false
//                                                return textField(map10)
//                                            }
//                                        })
//                                )
//                                val map9 = LinkedHashMap<String, Any>(1)
//                                val map10 = LinkedHashMap<String, Int>(8)
//                                map10["gridx"] = getProperty("wsvg")
//                                map10["gridy"] = 1 + getProperty("hthl")
//                                map10["gridwidth"] = 1
//                                map10["gridheight"] = getProperty("hsen")
//                                map10["fill"] = getProperty("VERTICAL")
//                                map10["insets"] = getProperty("inset")
//                                map10["weightx"] = 0
//                                map10["weighty"] = 1
//                                map9["constraints"] = invokeMethod("gbc", arrayOf<Any>(map10))
//                                setmSensorPanel(
//                                    panel(
//                                        map9,
//                                        object : Closure<JCheckBox?>(
//                                            this@StatusWindow2k,
//                                            this@StatusWindow2k
//                                        ) {
//                                            @JvmOverloads
//                                            fun doCall(it: Any? = null): JCheckBox {
//                                                val map11 = LinkedHashMap<String, Any>(1)
//                                                map11["axis"] = getProperty("BoxLayout").Y_AXIS
//                                                boxLayout(map11)
//                                                val map12 = LinkedHashMap<String, Serializable>(2)
//                                                map12["text"] = "No JMRI Sensor"
//                                                map12["selected"] = false
//                                                return checkBox(map12)
//                                            }
//                                        })
//                                )
//                                val map11 = LinkedHashMap<String, Any>(1)
//                                val map12 = LinkedHashMap<String, Int>(9)
//                                map12["gridx"] = getProperty("wsvg")
//                                map12["gridy"] = 1 + getProperty("hthl") + getProperty("hsen")
//                                map12["gridwidth"] = 1
//                                map12["gridheight"] = getProperty("hbtn")
//                                map12["fill"] = getProperty("VERTICAL")
//                                map12["anchor"] = getProperty("PAGE_END")
//                                map12["insets"] = getProperty("inset")
//                                map12["weightx"] = 0
//                                map12["weighty"] = 0
//                                map11["constraints"] = invokeMethod("gbc", arrayOf<Any>(map12))
//                                panel(
//                                    map11,
//                                    object : Closure<JCheckBox?>(
//                                        this@StatusWindow2k,
//                                        this@StatusWindow2k
//                                    ) {
//                                        @JvmOverloads
//                                        fun doCall(it: Any? = null): JCheckBox {
//                                            val map13 = LinkedHashMap<String, Any>(1)
//                                            map13["axis"] = getProperty("BoxLayout").Y_AXIS
//                                            boxLayout(map13)
//                                            val map14 = LinkedHashMap<String, Serializable>(3)
//                                            map14["text"] = "Flaky"
//                                            map14["selected"] = false
//                                            map14["actionPerformed"] =
//                                                object : Closure(this, this) {
//                                                    fun doCall(evt: Any?): Any {
//                                                        return invokeMethod(
//                                                            "onFlakyChanged",
//                                                            arrayOfNulls<Any>(0)
//                                                        )
//                                                    }
//                                                }
//                                            setmFlakyCheck(checkBox(map14))
//                                            val map15 = LinkedHashMap<String, Serializable>(3)
//                                            map15["text"] = "Kiosk Mode"
//                                            map15["selected"] = false
//                                            map15["actionPerformed"] =
//                                                object : Closure(this, this) {
//                                                    fun doCall(evt: Any?): Any {
//                                                        return invokeMethod(
//                                                            "onKioskChanged",
//                                                            arrayOfNulls<Any>(0)
//                                                        )
//                                                    }
//                                                }
//                                            return setmKioskCheck0(
//                                                this@StatusWindow2k,
//                                                checkBox(map15)
//                                            )
//                                        }
//                                    })
//
//                                // Side Log
//                                val map13 = LinkedHashMap<String, Any>(3)
//                                map13["verticalScrollBarPolicy"] =
//                                    getProperty("VERTICAL_SCROLLBAR_ALWAYS")
//                                map13["horizontalScrollBarPolicy"] =
//                                    getProperty("HORIZONTAL_SCROLLBAR_AS_NEEDED")
//                                val map14 = LinkedHashMap<String, Int>(10)
//                                map14["gridx"] = getProperty("wx") - getProperty("wlog")
//                                map14["gridy"] = 1
//                                map14["gridwidth"] = getProperty("wlog")
//                                map14["gridheight"] = getProperty("hsvg") + 1
//                                map14["fill"] = getProperty("BOTH")
//                                map14["minWidth"] = 200
//                                map14["minHeight"] = 200
//                                map14["insets"] = getProperty("inset")
//                                map14["weightx"] = 1
//                                map14["weighty"] = 1
//                                map13["constraints"] = invokeMethod("gbc", arrayOf<Any>(map14))
//                                return setmLogScroller0(
//                                    this@StatusWindow2k,
//                                    scrollPane(map13, object : Closure<Int?>(
//                                        this@StatusWindow2k, this@StatusWindow2k
//                                    ) {
//                                        @JvmOverloads
//                                        fun doCall(it: Any? = null): Int {
//                                            val map15 = LinkedHashMap<String, Serializable>(5)
//                                            map15["text"] = "Log Area"
//                                            map15["columns"] = 100
//                                            map15["editable"] = false
//                                            map15["lineWrap"] = true
//                                            map15["wrapStyleWord"] = true
//                                            setmLogField(textArea(map15))
//                                            return setCaretPosition(getmLogField(), 0)
//                                        }
//                                    })
//                                )
//                            }
//                        })
//                )
//
//                getmFrame().setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
//                getmFrame().addWindowListener(object : WindowAdapter() {
//                    override fun windowClosing(windowEvent: WindowEvent) {
//                        onQuit()
//                    }
//                })
//            }
//        })
//    }
//
//    fun onQuit() {
//        mFrame.dispose()
//        mWindowCallback!!.onQuit()
//    }
//
//    fun onHideOrQuit() {
//        if (mIsSimulation) {
//            onQuit()
//        } else {
//            // Minimize the window
//            changeFrameState(JFrame.MAXIMIZED_BOTH, JFrame.ICONIFIED)
//        }
//    }
//
//    fun onReload() {
//        clearUpdates()
//        clearSvgMap()
//        registerThrottles(emptyList<IThrottleDisplayAdapter>())
//        registerActivables(
//            emptyList<ISensorDisplayAdapter>(),
//            emptyList<IActivableDisplayAdapter>(),
//            emptyList<IActivableDisplayAdapter>()
//        )
//        mSwingBuilder.doLater(object : Closure(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null) {
//                getmWindowCallback()!!.onWindowReload()
//            }
//        })
//        updatePause(false)
//    }
//
//    fun onKioskChanged() {
//        val isKiosk: Boolean = mKioskCheck.isSelected()
//        if (isKiosk) {
//            mTopPanel.setVisible(false)
//            mSimuScroller.setVisible(false)
//            mLogScroller.setVisible(false)
//            changeFrameState(JFrame.ICONIFIED, JFrame.MAXIMIZED_BOTH)
//            mFrame.setAlwaysOnTop(true)
//        } else {
//            mTopPanel.setVisible(true)
//            mSimuScroller.setVisible(true)
//            mLogScroller.setVisible(true)
//            onSimulationModeChanged()
//            changeFrameState(JFrame.MAXIMIZED_BOTH, JFrame.NORMAL)
//            mFrame.setAlwaysOnTop(false)
//        }
//    }
//
//    fun onFlakyChanged() {
//        mWindowCallback!!.onFlaky(mFlakyCheck.isSelected())
//    }
//
//    fun onSimulationModeChanged() {
//        mSimuScroller.setVisible(mIsSimulation)
//        mFlakyCheck.setVisible(mIsSimulation)
//        mQuitButton.setText(if (mIsSimulation) "Quit" else "Hide")
//    }
//
//    fun changeFrameState(removeState: Int, addState: Int) {
//        val state: Int = (mFrame.getExtendedState() or addState) and removeState.inv()
//        mFrame.setExtendedState(state)
//    }
//
//    fun enterKioskMode() {
//        mSwingBuilder.doLater(object : Closure(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null) {
//                getmKioskCheck().setSelected(true)
//                onKioskChanged()
//            }
//        })
//    }
//
//    fun setSimulationMode(isSimulation: Boolean) {
//        mIsSimulation = isSimulation
//        mSwingBuilder.doLater(object : Closure(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null) {
//                onSimulationModeChanged()
//            }
//        })
//    }
//
//    fun updatePause(isPaused: Boolean) {
//        mSwingBuilder.doLater(object : Closure<String?>(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null): String {
//                return setText(getmPauseButton(), if (isPaused) "Continue" else "Pause")
//            }
//        })
//    }
//
//    fun updateScriptName(scriptName: String) {
//        mSwingBuilder.doLater(object : Closure<String?>(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null): String {
//                getmScriptNameField().setText(scriptName)
//                return scriptName
//            }
//        })
//    }
//
//    fun updateMainLog(logText: String?) {
//        mSwingBuilder.doLater(object : Closure<Int?>(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null): Int {
//                val p: Int = getmLogField().getCaretPosition()
//                getmLogField().setText(logText)
//                return setCaretPosition(
//                    getmLogField(),
//                    Math.min(p, StringGroovyMethods.size(logText))
//                )
//            }
//        })
//    }
//
//    fun updateSimuLog(logText: String?) {
//        mSwingBuilder.doLater(object : Closure<Int?>(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null): Int {
//                val p: Int = getmLogSimul().getCaretPosition()
//                getmLogSimul().setText(logText)
//                return setCaretPosition(
//                    getmLogSimul(),
//                    Math.min(p, StringGroovyMethods.size(logText))
//                )
//            }
//        })
//    }
//
//    fun clearUpdates() {
//        mSwingBuilder.doLater(object : Closure(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null) {
//                mUpdaters.clear()
//            }
//        })
//    }
//
//    fun registerThrottles(throttles: List<IThrottleDisplayAdapter?>) {
//        mSwingBuilder.doLater(object : Closure(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null) {
//                if (VERBOSE) {
//                    DefaultGroovyMethods.println(
//                        this@StatusWindow2k,
//                        TAG + "registerThrottles # " + throttles.size
//                    )
//                }
//                getmThrottlePanel().removeAll()
//                if (throttles.isEmpty()) {
//                    // Box Dimensions = MinSize / PreferredSize / MaxSize = given size.
//                    getmThrottlePanel().add(Box.createRigidArea(Dimension(midColumnW, 45)))
//                } else {
//                    for (throttle in throttles) {
//                        addThrottle(throttle)
//                    }
//                }
//
//                getmFrame().pack()
//            }
//        })
//    }
//
//    private fun addThrottle(throttleAdapter: IThrottleDisplayAdapter) {
//        val map = LinkedHashMap<String, Boolean>(3)
//        map["text"] = getProperty("throttleAdapter").name
//        map["opaque"] = false
//        map["editable"] = false
//        val wx: JTextPane = (mSwingBuilder as SwingBuilder?).textPane(map)
//        mThrottlePanel.add(wx)
//        mThrottlePanel.setMinimumSize(Dimension(midColumnW, 40))
//        mThrottlePanel.setBackground(Color.LIGHT_GRAY)
//
//        val context: StyleContext = StyleContext()
//        val doc: StyledDocument = DefaultStyledDocument(context)
//
//        val d: Style = context.getStyle(StyleContext.DEFAULT_STYLE)
//        val c: Style = context.addStyle("center", d)
//        c.addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_CENTER)
//        val b: Style = context.addStyle("bold", c)
//        b.addAttribute(StyleConstants.Bold, true)
//        val f: Style = context.addStyle("fwd", c)
//        f.addAttribute(StyleConstants.Background, Color.GREEN)
//        val r: Style = context.addStyle("rev", c)
//        r.addAttribute(StyleConstants.Background, Color.ORANGE)
//        val l: Style = context.addStyle("light", b)
//        l.addAttribute(StyleConstants.Background, Color.YELLOW)
//        val s: Style = context.addStyle("sound", b)
//        s.addAttribute(StyleConstants.Background, Color.CYAN)
//
//        wx.setDocument(doc)
//
//        val updater: Runnable = object : Closure(this, this) {
//            fun doCall() {
//                updateThrottlePane(wx, throttleAdapter)
//            }
//        }
//        updater.run()
//        mUpdaters.add(updater)
//    }
//
//    private fun updateThrottlePane(
//        textPane: JTextComponent,
//        throttleAdapter: IThrottleDisplayAdapter
//    ) {
//        val speed: Int = throttleAdapter.getSpeed()
//        val fwd = speed > 0
//        val rev = speed < 0
//        val light: Boolean = throttleAdapter.isLight()
//        val sound: Boolean = throttleAdapter.isSound()
//
//        val spanSpeed = String.format(
//            " %s [%d] %s %d \n", throttleAdapter.getName(), throttleAdapter.getDccAddress(),
//            if (speed < 0) " < " else (if (speed == 0) " = " else " > "), speed
//        )
//
//        var spanA = ""
//        if (throttleAdapter.getActivationsCount() >= 0) {
//            spanA = String.format(" #%d • ", throttleAdapter.getActivationsCount())
//        }
//
//
//        val spanL = String.format(" L%s ", if (light) "+" else "-")
//        val spanS = String.format(" S%s ", if (sound) "+" else "-")
//
//        try {
//            val doc: StyledDocument = textPane.getDocument() as StyledDocument
//            doc.remove(0, doc.getLength())
//            doc.insertString(
//                0,
//                spanSpeed,
//                doc.getStyle(if (fwd) "fwd" else (if (rev) "rev" else "center"))
//            )
//            if (!spanA.isEmpty()) {
//                doc.insertString(doc.getLength(), spanA, doc.getStyle("center"))
//            }
//
//            doc.insertString(doc.getLength(), spanL, doc.getStyle(if (light) "light" else "center"))
//            doc.insertString(doc.getLength(), spanS, doc.getStyle(if (sound) "sound" else "center"))
//            // everything is centered
//            doc.setParagraphAttributes(0, doc.getLength(), doc.getStyle("center"), false)
//        } catch (e: BadLocationException) {
//            throw RuntimeException(e)
//        }
//    }
//
//    fun registerActivables(
//        sensors: List<ISensorDisplayAdapter?>,
//        blocks: List<IActivableDisplayAdapter?>,
//        turnouts: List<IActivableDisplayAdapter?>
//    ) {
//        mSwingBuilder.doLater(object : Closure(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null) {
//                if (VERBOSE) {
//                    DefaultGroovyMethods.println(this@StatusWindow2k, TAG + "register UI Updates.")
//                }
//                getmSensorPanel().removeAll()
//
//                for (sensor in sensors) {
//                    addSensor(sensor)
//                }
//
//
//                for (block in blocks) {
//                    addBlock(block)
//                }
//
//
//                for (turnout in turnouts) {
//                    addTurnout(turnout)
//                }
//
//
//                getmFrame().pack()
//            }
//        })
//    }
//
//    fun addSensor(sensorAdapter: ISensorDisplayAdapter) {
//        val map = LinkedHashMap<String, Boolean>(2)
//        map["text"] = getProperty("sensorAdapter").name
//        map["selected"] = false
//        val wx: JCheckBox = (mSwingBuilder as SwingBuilder?).checkBox(map)
//        mSensorPanel.add(wx)
//
//        val updater: Runnable = object : Closure(this, this) {
//            fun doCall() {
//                wx.setSelected(sensorAdapter.isActive())
//            }
//        }
//        updater.run()
//        mUpdaters.add(updater)
//
//        wx.addActionListener()
//    }
//
//    fun addBlock(adapter: IActivableDisplayAdapter) {
//        val updater: Runnable = object : Closure(this, this) {
//            fun doCall() {
//                val name = "S-" + adapter.getName()
//                setBlockColor(name, adapter.isActive(), adapter.getBlockState())
//            }
//        }
//        updater.run()
//        mUpdaters.add(updater)
//    }
//
//    fun addTurnout(adapter: IActivableDisplayAdapter) {
//        val updater: Runnable = object : Closure(this, this) {
//            fun doCall() {
//                val normal: Boolean = adapter.isActive()
//                val name = "T-" + adapter.getName()
//                val N = name + "N"
//                val R = name + "R"
//
//                setTurnoutVisible(N, normal)
//                setTurnoutVisible(R, !normal)
//            }
//        }
//        updater.run()
//        mUpdaters.add(updater)
//    }
//
//    fun updateUI() {
//        mSwingBuilder.doLater(object : Closure<Void?>(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null) {
//                for (updater in mUpdaters) {
//                    updater.run()
//                }
//            }
//        })
//    }
//
//    fun clearSvgMap() {
//        mSvgCanvas.stopProcessing()
//        mModifSvgQueue.clear()
//        mBlockColorMap.clear()
//    }
//
//    fun displaySvgMap(svgDocument: String?, mapUrl: URI) {
//        mSvgCanvas.stopProcessing()
//        mBlockColorMap.clear()
//        // Per documentation in JSVGComponentListener, this is invoked from a background thread.
//        mOnRenderCompleted = object : GVTTreeRendererAdapter() {
//            override fun gvtRenderingCompleted(e: GVTTreeRendererEvent) {
//                getmSvgCanvas().removeGVTTreeRendererListener(mOnRenderCompleted)
//                mOnRenderCompleted = null
//                getmWindowCallback()!!.onWindowSvgLoaded()
//                getmFrame().pack()
//
//                val onClick: EventListener = object : EventListener {
//                    override fun handleEvent(event: Event) {
//                        // Note: This is called on the SVG UpdateManager thread.
//                        val target = event.target
//                        if (target is SVGElement) {
//                            getmSwingBuilder().doLater(object : Closure(this, this) {
//                                @JvmOverloads
//                                fun doCall(it: Any? = null) {
//                                    getmWindowCallback()!!.onWindowSvgClick((target as SVGElement).getId())
//                                }
//                            })
//                        }
//                    }
//                }
//
//                modifySvg(object : Closure(this, this) {
//                    @JvmOverloads
//                    fun doCall(it: Any? = null) {
//                        val doc: SVGDocument = getmSvgCanvas().getSVGDocument()
//                        val element: SVGSVGElement = doc.getRootElement()
//                        initAllSvgElements(element, onClick)
//                    }
//                })
//                // updateSvg()
//            }
//        }
//        mSvgCanvas.addGVTTreeRendererListener(mOnRenderCompleted)
//
//        XMLResourceDescriptor.setCSSParserClassName(InkscapeCssParser::class.java.name)
//        mSvgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC)
//
//        if (svgDocument != null && !svgDocument.isEmpty()) {
//            // Parse given document and load it. The URL is only informational
//            // and it could be "" or "file:///" for path-less documents.
//            val parser: String = XMLResourceDescriptor.getXMLParserClassName()
//            val factory: SAXSVGDocumentFactory = SAXSVGDocumentFactory(parser)
//            val document: SVGDocument = factory.createSVGDocument(
//                mapUrl.toString(), ByteArrayInputStream(
//                    svgDocument.toByteArray(
//                        charset("UTF-8")
//                    )
//                )
//            )
//            mSvgCanvas.setSVGDocument(document)
//        } else {
//            // Otherwise let the SVG load the given URL (which much be valid)
//            mSvgCanvas.setURI(mapUrl.toString())
//        }
//
//        if (VERBOSE) DefaultGroovyMethods.println(this, TAG + "SVG Map loaded from " + mapUrl)
//    }
//
//    fun initAllSvgElements(element: SVGSVGElement?, onClick: EventListener?) {
//        var element: SVGSVGElement? = element
//        while (element != null) {
//            if (element.firstChild != null) {
//                initAllSvgElements(element.firstChild, onClick)
//            }
//
//            if (element is SVGElement) {
//                val id: String = element.getId()
//                if (id != null && (id.startsWith("S-") || id.startsWith("T-"))) {
//                    //if (VERBOSE) println(TAG + "Add onClick listener to id = $id") // -- for debugging
//                    DefaultGroovyMethods.invokeMethod(
//                        element, "addEventListener", arrayOf<Any?>(
//                            "click",
//                            onClick,
//                            false
//                        )
//                    )
//                }
//
//                if (id.startsWith("Toggle-T")) {
//                    if (element is SVGStylable) {
//                        element.getStyle().setProperty("opacity", "0", "")
//                    }
//                }
//            }
//
//
//            element =
//                (element.invokeMethod("getNextSibling", arrayOfNulls<Any>(0)) as SVGSVGElement)
//        }
//    }
//
//    fun setBlockColor(id: String?, active: Boolean, blockState: Optional<BlockState?>) {
//        modifySvg(object : Closure(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null) {
//                val doc: SVGDocument = getmSvgCanvas().getSVGDocument()
//                val elem: Element = doc.getElementById(id)
//                // if (VERBOSE) println(TAG + "Set color for block id = $id") // -- for debugging
//                if (elem is SVGStylable) {
//                    // Note: setProperty(String propertyName, String value, String priority)
//                    val priority = "" // or "important"
//                    val style: CSSStyleDeclaration = (elem as SVGStylable).getStyle()
//                    if (style != null) {
//                        var original = mBlockColorMap[elem]
//                        if (original == null) {
//                            val stroke = style.getPropertyValue("stroke")
//                            if (!stroke.isEmpty()) {
//                                mBlockColorMap[elem] = stroke
//                                original = stroke
//                            }
//                        }
//
//                        //    println "_original_stroke $id = " + original.getClass().getSimpleName() + " " + original
//                        var rgb = if (active) "red" else original
//                        if (blockState.isPresent()) {
//                            when (blockState.get()) {
//                                PsiEnumConstant -> {
//                                    BLOCK_EMPTY@ break
//                                    BLOCK_OCCUPIED@ TODO(
//                                        """
//                                        |Cannot convert element
//                                        |With text:
//                                        |case PsiEnumConstant:
//                                        """.trimMargin()
//                                    )
//                                    BLOCK_TRAILING@ rgb = if (active) "red" else "orange"
//                                }
//
//                                PsiEnumConstant -> {
//                                    BLOCK_OCCUPIED@ TODO(
//                                        """
//                                        |Cannot convert element
//                                        |With text:
//                                        |case PsiEnumConstant:
//                                        """.trimMargin()
//                                    )
//                                    BLOCK_TRAILING@ rgb = if (active) "red" else "orange"
//                                }
//                            }
//                        }
//
//                        if (rgb != null) {
//                            style.setProperty("stroke", rgb, priority)
//                        }
//                    }
//                }
//            }
//        })
//    }
//
//    fun setTurnoutVisible(id: String?, visible: Boolean) {
//        modifySvg(object : Closure(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null) {
//                val doc: SVGDocument = getmSvgCanvas().getSVGDocument()
//                val elem: Element = doc.getElementById(id)
//                //if (VERBOSE) println(TAG + "Set turnout id = $id") // -- for debugging
//                if (elem is SVGStylable) {
//                    val display = if (visible) "inline" else "none"
//                    (elem as SVGStylable).getStyle().setProperty("display", display, "")
//                }
//            }
//        })
//    }
//
//    /**
//     * Wrap all modifications to the SVG using this call so that they happen on the UpdateManager.
//     *
//     *
//     * According to the javadoc in UpdateManager, all modifications to the SVG should be
//     * done in the UpdateManager's RunnableQueue (cf #getUpdateRunnableQueue()).
//     * The SVG will be automatically repainted as necessary.
//     *
//     *
//     * When the window opens and loads the SVG and the first script at the same time, this may
//     * be called before the SVG Canvas's UpdateManager is available. In this case runnables are
//     * queued and will be added as soon as the update manager exists. That pending list is cleared
//     * when a new SVG is loaded.
//     */
//    fun modifySvg(cl: Closure) {
//        val r: Runnable = object : Closure<Any?>(this, this) {
//            fun doCall(): Any {
//                return cl.call()
//            }
//        }
//        val queue: RunnableQueue = mSvgCanvas.getUpdateManager().getUpdateRunnableQueue()
//
//        if (queue == null) {
//            mModifSvgQueue.add(r)
//            return
//        }
//
//
//        while (!mModifSvgQueue.isEmpty()) {
//            queue.invokeLater(mModifSvgQueue.remove())
//        }
//
//
//        queue.invokeLater(r)
//    }
//
//    fun getmWindowCallback(): IWindowCallback? {
//        return mWindowCallback
//    }
//
//    fun setmWindowCallback(mWindowCallback: IWindowCallback?) {
//        this.mWindowCallback = mWindowCallback
//    }
//
//    fun getmSwingBuilder(): SwingBuilder? {
//        return mSwingBuilder
//    }
//
//    fun setmSwingBuilder(mSwingBuilder: SwingBuilder?) {
//        this.mSwingBuilder = mSwingBuilder
//    }
//
//    fun getmFrame(): JFrame? {
//        return mFrame
//    }
//
//    fun setmFrame(mFrame: JFrame?) {
//        this.mFrame = mFrame
//    }
//
//    fun getmScriptNameField(): JTextField? {
//        return mScriptNameField
//    }
//
//    fun setmScriptNameField(mScriptNameField: JTextField?) {
//        this.mScriptNameField = mScriptNameField
//    }
//
//    fun getmLogField(): JTextArea? {
//        return mLogField
//    }
//
//    fun setmLogField(mLogField: JTextArea?) {
//        this.mLogField = mLogField
//    }
//
//    fun getmLogScroller(): JScrollPane? {
//        return mLogScroller
//    }
//
//    fun setmLogScroller(mLogScroller: JScrollPane?) {
//        this.mLogScroller = mLogScroller
//    }
//
//    fun getmSimuScroller(): JScrollPane? {
//        return mSimuScroller
//    }
//
//    fun setmSimuScroller(mSimuScroller: JScrollPane?) {
//        this.mSimuScroller = mSimuScroller
//    }
//
//    fun getmLogSimul(): JTextArea? {
//        return mLogSimul
//    }
//
//    fun setmLogSimul(mLogSimul: JTextArea?) {
//        this.mLogSimul = mLogSimul
//    }
//
//    fun getmTopPanel(): JPanel? {
//        return mTopPanel
//    }
//
//    fun setmTopPanel(mTopPanel: JPanel?) {
//        this.mTopPanel = mTopPanel
//    }
//
//    fun getmThrottlePanel(): JPanel? {
//        return mThrottlePanel
//    }
//
//    fun setmThrottlePanel(mThrottlePanel: JPanel?) {
//        this.mThrottlePanel = mThrottlePanel
//    }
//
//    fun getmSensorPanel(): JPanel? {
//        return mSensorPanel
//    }
//
//    fun setmSensorPanel(mSensorPanel: JPanel?) {
//        this.mSensorPanel = mSensorPanel
//    }
//
//    fun getmSvgCanvas(): JSVGCanvas? {
//        return mSvgCanvas
//    }
//
//    fun setmSvgCanvas(mSvgCanvas: JSVGCanvas?) {
//        this.mSvgCanvas = mSvgCanvas
//    }
//
//    fun getmPauseButton(): JButton? {
//        return mPauseButton
//    }
//
//    fun setmPauseButton(mPauseButton: JButton?) {
//        this.mPauseButton = mPauseButton
//    }
//
//    fun getmQuitButton(): JButton? {
//        return mQuitButton
//    }
//
//    fun setmQuitButton(mQuitButton: JButton?) {
//        this.mQuitButton = mQuitButton
//    }
//
//    fun getmKioskCheck(): JCheckBox? {
//        return mKioskCheck
//    }
//
//    fun setmKioskCheck(mKioskCheck: JCheckBox?) {
//        this.mKioskCheck = mKioskCheck
//    }
//
//    fun getmFlakyCheck(): JCheckBox? {
//        return mFlakyCheck
//    }
//
//    fun setmFlakyCheck(mFlakyCheck: JCheckBox?) {
//        this.mFlakyCheck = mFlakyCheck
//    }
//
//    private val VERBOSE = true
//    private var mWindowCallback: IWindowCallback? = null
//    private var mSwingBuilder: SwingBuilder? = null
//    private var mFrame: JFrame? = null
//    private var mScriptNameField: JTextField? = null
//    private var mLogField: JTextArea? = null
//    private var mLogScroller: JScrollPane? = null
//    private var mSimuScroller: JScrollPane? = null
//    private var mLogSimul: JTextArea? = null
//    private var mTopPanel: JPanel? = null
//    private var mThrottlePanel: JPanel? = null
//    private var mSensorPanel: JPanel? = null
//    private var mSvgCanvas: JSVGCanvas? = null
//    private var mPauseButton: JButton? = null
//    private var mQuitButton: JButton? = null
//    private var mKioskCheck: JCheckBox? = null
//    private var mFlakyCheck: JCheckBox? = null
//    private var mIsSimulation = true
//    private var mOnRenderCompleted: GVTTreeRendererListener? = null
//    private val mBlockColorMap: MutableMap<*, *> = HashMap<Any?, Any?>()
//    private val mModifSvgQueue: Queue<Runnable> = ConcurrentLinkedQueue<Runnable>()
//    private val mUpdaters: MutableList<Runnable> = ArrayList()
//    private val midColumnW = 125
//
//    companion object {
//        private const val TAG = "Ui2"
//        private fun <Value : JCheckBox?> setmKioskCheck0(
//            propOwner: StatusWindow2k,
//            mKioskCheck: Value
//        ): Value {
//            propOwner.setmKioskCheck(mKioskCheck)
//            return mKioskCheck
//        }
//
//        private fun setCaretPosition(propOwner: JTextComponent?, position: Int): Int {
//            propOwner.setCaretPosition(position)
//            return position
//        }
//
//        private fun <Value : JScrollPane?> setmLogScroller0(
//            propOwner: StatusWindow2k,
//            mLogScroller: Value
//        ): Value {
//            propOwner.setmLogScroller(mLogScroller)
//            return mLogScroller
//        }
//
//        private fun <Value : String?> setText(propOwner: AbstractButton?, text: Value): Value {
//            propOwner.setText(text)
//            return text
//        }
//
//        private fun <Value : JButton?> setmQuitButton0(
//            propOwner: StatusWindow2k,
//            mQuitButton: Value
//        ): Value {
//            propOwner.setmQuitButton(mQuitButton)
//            return mQuitButton
//        }
//    }
}
