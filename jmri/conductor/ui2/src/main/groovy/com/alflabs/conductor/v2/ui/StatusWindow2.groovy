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
package com.alflabs.conductor.v2.ui

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

import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.text.JTextComponent
import java.awt.Color
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.ConcurrentLinkedQueue

/*
 * Some reading to understand this:
 * https://mrhaki.blogspot.com/2009/11/groovy-goodness-building-gui-with.html
 * https://uberconf.com/blog/andres_almiray/2009/11/building_rich_swing_applications_with_groovy__part_i
 *
 * All the default swing layouts:
 * https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
 */

import java.util.concurrent.atomic.AtomicReference

import static java.awt.GridBagConstraints.BOTH
import static java.awt.GridBagConstraints.HORIZONTAL
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE

class StatusWindow2 {

    def IWindowCallback mWindowCallback
    def SwingBuilder mSwingBuilder
    def JFrame mFrame
    def JTextField mScriptNameField
    def JTextArea mLogField
    def JScrollPane mLogScroller
    def JPanel mThrottlePanel
    def JPanel mSensorPanel
    def JSVGCanvas mSvgCanvas
    def JButton mPauseButton
    private def mOnRenderCompleted
    private final Map mBlockColorMap = new HashMap()
    private final Queue<Runnable> mModifSvgQueue = new ConcurrentLinkedQueue<>()

    def open(IWindowCallback windowCallback) {
        mWindowCallback = windowCallback
        mSwingBuilder = new SwingBuilder()
        mSwingBuilder.registerBeanFactory("svgCanvas", JSVGCanvas)
        mSwingBuilder.edt {
            mFrame = frame(title: "Conductor v2",
                    preferredSize: new Dimension(800, 500),
                    minimumSize: new Dimension(300, 300),
                    // locationRelativeTo: null,
                    pack: true,
                    show: true) {
                gridBagLayout()
                final def inset = [5, 5, 5, 5]
                final def gx = 4
                final def wx = 8
                def gy = 0
                // Top
                panel(constraints: gbc(gridx:0, gridy: gy++,
                        gridwidth: wx, gridheight: 1,
                        fill: HORIZONTAL, insets: inset)) {
                    gridBagLayout()
                    mScriptNameField = textField(text: "Script1 Name", editable: false,
                            constraints: gbc(gridx: 0, gridy: 0,
                                    gridwidth: wx-3, fill: HORIZONTAL, insets: inset, weightx: 1))
                    button(text: "Reload", constraints: gbc(gridx: wx-3, gridy: 0, insets: inset, weightx: 0),
                            actionPerformed: { evt -> windowCallback.onWindowReload() })
                    mPauseButton =
                        button(text: "Pause", constraints: gbc(gridx: wx-2, gridy: 0, insets: inset, weightx: 0),
                            actionPerformed: { evt -> windowCallback.onWindowPause() })
                    button(text: "Quit", constraints: gbc(gridx: wx-1, gridy: 0, insets: inset, weightx: 0),
                            actionPerformed: { evt -> onQuit() })
                }

                // Middle: SVG Map
                mSvgCanvas = svgCanvas(background: Color.DARK_GRAY,
                        constraints: gbc(gridx: 0, gridy: gy++,
                                gridwidth: gx, fill: BOTH,
                                minHeight: 200, insets: inset,
                                weightx: 1, weighty: 2))

                // Below Map
                mThrottlePanel = panel(constraints: gbc(gridx: 0, gridy: gy++,
                        gridwidth: gx, fill: HORIZONTAL,
                        insets: inset, weightx: 0)) {
                    boxLayout(axis: BoxLayout.X_AXIS)
                    textField(text: "No DCC Throttle", editable: false)
                }
                mSensorPanel = panel(constraints: gbc(gridx: 0, gridy: gy++,
                        gridwidth: gx, fill: HORIZONTAL,
                        insets: inset, weightx: 0)) {
                    flowLayout()
                    checkBox(text: "No JMRI Sensor", selected: false)
                }

                // Bottom Log
                /*
                scrollPane(verticalScrollBarPolicy: VERTICAL_SCROLLBAR_ALWAYS,
                        horizontalScrollBarPolicy: HORIZONTAL_SCROLLBAR_AS_NEEDED,
                        constraints: gbc(gridx: 0, gridy: gy++,
                                gridwidth: gx, fill: BOTH,
                                minHeight: 100, insets: inset,
                                weightx: 1, weighty: 1)) {
                    textArea(text: "Output Log (TBD)", editable: true,
                            lineWrap: true, wrapStyleWord: true)
                }
                */

                // Side Log
                mLogScroller = scrollPane(verticalScrollBarPolicy: VERTICAL_SCROLLBAR_ALWAYS,
                        horizontalScrollBarPolicy: HORIZONTAL_SCROLLBAR_AS_NEEDED,
                        constraints: gbc(gridx: gx, gridy: 1,
                                gridwidth: wx-gx, gridheight: gy-1, fill: BOTH,
                                minHeight: 200, insets: inset,
                                weightx: 1, weighty: 1)) {
                    mLogField = textArea(text: "Log Area", editable: false,
                            lineWrap: true, wrapStyleWord: true)
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

    def onQuit() {
        mFrame.dispose()
        mWindowCallback.onQuit();
    }

    def updatePause(boolean isPaused) {
        mPauseButton.text = isPaused ? "Continue" : "Pause"
    }

    def updateScriptName(String scriptName) {
        mScriptNameField.text = scriptName
    }

    def updateLog(String logText) {
        def p = mLogField.caretPosition
        mLogField.text = logText
        mLogField.caretPosition = p
    }

    def removeThrottles() {
        mSwingBuilder.edt {
            mThrottlePanel.removeAll()
            mThrottlePanel.add(Box.createRigidArea(new Dimension(5, 0)))
            mFrame.pack()
        }
    }

    def JTextComponent addThrottle(String name) {
        AtomicReference<JTextComponent> wx = new AtomicReference<JTextComponent>()
        mSwingBuilder.edt {
            wx.set(mSwingBuilder.textPane(
                    text: name,
                    opaque: false,
                    editable: false))
            mThrottlePanel.add(wx.get())
            mThrottlePanel.add(Box.createRigidArea(new Dimension(5, 0)))
            mFrame.pack()
        }
        return wx.get()
    }

    def removeSensors() {
        mSwingBuilder.edt {
            mSensorPanel.removeAll()
            mFrame.pack()
        }
    }

    def JCheckBox addSensor(String name) {
        AtomicReference<JCheckBox> wx = new AtomicReference<JCheckBox>()
        mSwingBuilder.edt {
            wx.set(mSwingBuilder.checkBox(text: name, selected: false))
            mSensorPanel.add(wx.get())
            mFrame.pack()
        }
        return wx.get()
    }

    // Fill SVG using svgDocument (as text).
    // If svgDocument is null or empty, rely only on mapUrl.
    void displaySvgMap(String svgDocument, URI mapUrl) {
        mSwingBuilder.edt {
            mModifSvgQueue.clear()
            mBlockColorMap.clear()
            // Per documentation in JSVGComponentListener, this is invoked from a background thread.
            mOnRenderCompleted = new GVTTreeRendererAdapter() {
                @Override
                void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                    mSvgCanvas.removeGVTTreeRendererListener(mOnRenderCompleted)
                    mWindowCallback.onWindowSvgLoaded()
                    mFrame.pack()

                    def onClick = new EventListener() {
                        @Override
                        void handleEvent(org.w3c.dom.events.Event event) {
                            // Note: This is called on the SVG UpdateManager thread.
                            def target = event.getTarget()
                            if (target instanceof SVGElement) {
                                mSwingBuilder.edt {
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
        }
    }

    void initAllSvgElements(element, onClick) {
        while (element != null) {
            if (element.firstChild != null) {
                initAllSvgElements(element.firstChild, onClick)
            }
            if (element instanceof SVGElement) {
                String id = element.getId()
                if (id != null && (id.startsWith("S-b") || id.startsWith("Toggle-T"))) {
                    // println "visit id = $id" // -- for debugging
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

    void setBlockColor(String id, boolean active) {
        modifySvg {
            SVGDocument doc = mSvgCanvas.getSVGDocument()
            Element elem = doc?.getElementById(id)
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
            println "@@ Deferring SVG update -------------- (DEBUG)"
            mModifSvgQueue.add(r)
            return;
        }

        while (!mModifSvgQueue.isEmpty()) {
            queue.invokeLater(mModifSvgQueue.remove())
        }

        queue.invokeLater(r)
    }
}
