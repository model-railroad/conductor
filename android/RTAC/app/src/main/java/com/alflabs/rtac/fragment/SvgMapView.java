package com.alflabs.rtac.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.alflabs.manifest.Prefix;
import com.alflabs.rtac.BuildConfig;
import com.caverock.androidsvg.Colour;
import com.caverock.androidsvg.PreserveAspectRatio;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.caverock.androidsvg.Style;
import com.caverock.androidsvg.SvgElement;
import com.caverock.androidsvg.SvgObject;
import com.caverock.androidsvg.text.TSpan;
import com.caverock.androidsvg.text.Text;
import com.caverock.androidsvg.text.TextSequence;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class SvgMapView extends View {

    private static final String TAG = SvgMapView.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private final Map<String, BlockColor> mBlockColorMap = new TreeMap<>();
    private final Map<String, BlockText> mBlockTextMap = new TreeMap<>();
    private final Map<String, VisibleElement> mVisibilityMap = new TreeMap<>();

    private SVG mSvg;

    public SvgMapView(Context context) {
        super(context);
    }

    public SvgMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SvgMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void removeSvg() {
        mSvg = null;
        mBlockColorMap.clear();
        mBlockTextMap.clear();
        invalidate();
    }

    private static Pattern sTurnoutVisibilityId = Pattern.compile("^T-t.+[NR]$");

    public SVG loadSvg(String svgString) throws SVGParseException {
        removeSvg();

        mSvg = SVG.getFromString(svgString);
        mSvg.setDocumentPreserveAspectRatio(PreserveAspectRatio.UNSCALED);

        Set<String> viewList = mSvg.getViewList();
        if (DEBUG) Log.d(TAG, viewList.toString());

        for (String id : mSvg.getAllElementIds()) {
            if (id.startsWith("S-")) {
                // A block color for sensor S/xyz based on svg id S-xyz
                SvgElement e = mSvg.getElementById(id);
                mBlockColorMap.put(id.replace('-', '/'), new BlockColor(e, 0xFFFF0000));
            } else if (id.startsWith("LS-")) {
                // A block text for sensor S/xyz based on svg id LS-xyz
                SvgElement e = mSvg.getElementById(id);
                if (e instanceof Text) {
                    mBlockTextMap.put(id.replace('-', '/').substring(1), new BlockText((Text) e));
                }
            } else if (sTurnoutVisibilityId.matcher(id).matches()) {
                // An element that blocks a piece of track to represent a turnout in normal/diverging position.
                VisibleElement element = new VisibleElement(mSvg.getElementById(id));
                mVisibilityMap.put(id.replace('-', '/'), element);
                element.setVisible(false); // starts hidden
            }
        }

        forceLayout();
        invalidate();
        return mSvg;
    }

    /**
     * Changes the color of one of the sensor blocks.
     *
     * @param key An RTAC KV name, starting with a {@link Prefix#Sensor} prefix.
     * @param occupied True for block occupied. False for default block free.
     * @return true if the color has changed.
     */
    public boolean setBlockOccupancy(String key, boolean occupied) {
        boolean changed = false;
        BlockColor sensor = mBlockColorMap.get(key);
        if (sensor != null) {
            changed = sensor.set(occupied);
        }

        BlockText text = mBlockTextMap.get(key);
        if (text != null) {
            changed |= text.set(occupied);
        }

        return changed;
    }

    public boolean setTurnoutVisibility(String key, boolean normal) {
        boolean changed = false;

        VisibleElement n = mVisibilityMap.get(key + "N");
        if (n != null) {
            changed = n.setVisible(normal);
        }

        VisibleElement r = mVisibilityMap.get(key + "R");
        if (r != null) {
            changed |= r.setVisible(!normal);
        }

        return changed;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float ratio = mSvg == null ? 1.0f : mSvg.getDocumentAspectRatio();
        if (ratio == 0) { ratio = 1.0f; }
        if (DEBUG) if (mSvg != null) Log.d(TAG, String.format("SVG ratio: %f [%f x %f]", mSvg.getDocumentAspectRatio(), mSvg.getDocumentWidth(), mSvg.getDocumentHeight()));

        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);

        int w = wSize;
        int h = hSize;

        int hDesired = (int) (w / ratio);

        if (hMode == MeasureSpec.EXACTLY) {
            w = (int) (hSize * ratio);
        } else if (hMode == MeasureSpec.AT_MOST) {
            h = Math.min(hDesired, h);
            w = (int) (hSize * ratio);
        } else  {
            h = hDesired;
        }

        if (wMode == MeasureSpec.EXACTLY) {
            w = wSize;
        } else if (wMode == MeasureSpec.AT_MOST) {
            w = Math.min(w, wSize);
        }

        setMeasuredDimension(w, h);
        if (mSvg != null) {
            mSvg.setDocumentWidth(w);
            mSvg.setDocumentHeight(h);
            mSvg.setDocumentPreserveAspectRatio(PreserveAspectRatio.STRETCH);
        }
        if (DEBUG) Log.d(TAG, String.format("onMeasure %f [%08x %d] x [%08x %d] ==> %d x %d", ratio, wMode, wSize, hMode, hSize, w, h));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSvg != null) {
            mSvg.renderToCanvas(canvas);
        }
    }

    /**
     * Elements that can change color depending on a Block Sensor state. They rely on 2 principles:
     * - The SVG name is the same as the KV key name, except with - instead of /.
     *   For example the sensor "S/b330" is named "S-b330" in the SVG (can't use a / in the SVG id).
     * - The default stroke color indicates the "rest" position of the element: unoccupied for block sensors.
     * - Setting the block "on" means occupied for a block.
     */
    private static class BlockColor {
        private final SvgElement mElement;
        private final Colour mInitialStroke;
        private final int mOnColor;

        public BlockColor(SvgElement element, int occupiedColor) {
            mElement = element;
            mInitialStroke = (element.style.stroke instanceof Colour)
                    ? new Colour(((Colour) element.style.stroke).colour)
                    : null;
            mOnColor = occupiedColor;
        }

        /** Returns true if the color has changed. */
        public boolean set(boolean occupied) {
            if (mInitialStroke != null) {
                int newColor = occupied ? mOnColor : mInitialStroke.colour;
                int oldColor = ((Colour) mElement.style.stroke).colour;
                ((Colour) mElement.style.stroke).colour = newColor;
                return oldColor != newColor;
            }
            return false;
        }
    }

    /**
     * Text element that changes label depending on a Block Sensor state.
     * - The SVG name is the same as the KV key name, except with L and - instead of /.
     *   For example the sensor "S/b330" is named "LS-b330" in the SVG (can't use a / in the SVG id).
     * - The default text indicates the "rest" position of the element: unoccupied for block sensors.
     * - Setting the block to occupied changes the text to "[ text ]" or similar pattern.
     * - If a Text element has more than one TSpan, only the first TSpan is taken into consideration
     *   (or to be more precise, the first non-empty TextSequence of the first non-empty TSpan).
     *   If a text is empty, it is never affected.
     */
    private static class BlockText {
        private final TextSequence mTextSeq;
        private final String mInitialText;

        public BlockText(Text textElement) {
            TextSequence textSeq = null;
            String textString = null;
            loop: for (SvgObject child : textElement.getChildren()) {
                if (child instanceof TSpan) {
                    for (SvgObject grandchild : ((TSpan) child).getChildren()) {
                        if (grandchild instanceof TextSequence) {
                            String t = ((TextSequence) grandchild).text;
                            if (t != null && !t.isEmpty()) {
                                textSeq = (TextSequence) grandchild;
                                textString = t;
                                break loop;
                            }
                        }
                    }
                }
            }
            mTextSeq = textSeq;
            mInitialText = textString;
        }

        /** Returns true if the text has changed. */
        public boolean set(boolean occupied) {
            if (mTextSeq != null && mInitialText != null) {
                String newText = occupied ? ("< " + mInitialText + " >") : mInitialText;
                if (!newText.equals(mTextSeq.text)) {
                    mTextSeq.text = newText;
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * An SVG Element which style visibility can be either visible or hidden.
     * <p/>
     * The "display" SVG attribute is changed instead of "visible" because at least in the spec
     * only graphic and text elements have "visible". Groups, which we want to toggle too, do not
     * have "visible". However all elements have "display" thus we can use that.
     */
    private static class VisibleElement {

        private final SvgElement mElement;

        public VisibleElement(SvgElement element) {
            mElement = element;
            if (element != null) {
                if (element.style == null) {
                    element.style = new Style();
                }
                if (element.style.display == null) {
                    element.style.display = true;
                }
                element.style.specifiedFlags |= SVG.SPECIFIED_DISPLAY;
            }
        }

        public boolean setVisible(boolean visible) {
            boolean changed = false;
            if (mElement != null) {
                Boolean newState = visible;
                changed = !newState.equals(mElement.style.display);
                mElement.style.display = newState;
            }
            return changed;
        }
    }
}
