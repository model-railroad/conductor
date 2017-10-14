package com.alflabs.rtac.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.alflabs.manifest.Constants;
import com.alflabs.manifest.Prefix;
import com.alflabs.rtac.BuildConfig;
import com.caverock.androidsvg.Colour;
import com.caverock.androidsvg.PreserveAspectRatio;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.caverock.androidsvg.SvgElement;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SvgMapView extends View {

    private static final String TAG = SvgMapView.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private final Map<String, ColorToggle> mTogglesMap = new TreeMap<>();
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

    public SVG loadSvg(String svgString) throws SVGParseException {
        mSvg = SVG.getFromString(svgString);
        mSvg.setDocumentPreserveAspectRatio(PreserveAspectRatio.UNSCALED);

        Set<String> viewList = mSvg.getViewList();
        if (DEBUG) Log.d(TAG, viewList.toString());

        for (String id : mSvg.getAllElementIds()) {
            if (id.startsWith("S-") || id.startsWith("T-")) {
                SvgElement e = mSvg.getElementById(id);
                mTogglesMap.put(id.replace('-', '/'), new ColorToggle(e, 0xFFFF0000));
            }
        }

        forceLayout();
        return mSvg;
    }

    /**
     * Changes the color of one of the toggles (either stroke or fill).
     *
     * @param key An RTAC KV name, starting with one of the {@link Prefix}es (e.g. "S/b330")
     * @param on True for "block on / turnout diverging". False for default "block off / turnout normal".
     * @return true if the color has changed.
     */
    public boolean setToggleColor(String key, boolean on) {
        ColorToggle toggle = mTogglesMap.get(key);
        if (toggle != null) {
            return toggle.set(on);
        }
        return false;
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
     * Color-toggable elements rely on 2 principles:
     * - The SVG name is the same as the KV key name, except with - instead of /.
     *   For example the sensor "S/b330" is named "S-b330" in the SVG (can't use a / in the SVG id).
     * - The default stroke or fill color indicates the "rest" position of the element:
     *   - default is unoccupied for block sensors.
     *   - default is normal for turnouts.
     * - Setting the block / turnout to "on" means occupied for a block, reverse/diverging for a turnout.
     */
    private static class ColorToggle {
        private final SvgElement mElement;
        private final Colour mInitialStroke;
        private final Colour mInitialFill;
        private final int mOnColor;

        public ColorToggle(SvgElement element, int onColor) {
            mElement = element;
            mInitialStroke = (element.style.stroke instanceof Colour)
                    ? new Colour(((Colour) element.style.stroke).colour)
                    : null;
            mInitialFill = (element.style.fill instanceof Colour)
                    ? new Colour(((Colour) element.style.fill).colour)
                    : null;
            mOnColor = onColor;
        }

        /** Returns true if the color has changed. */
        public boolean set(boolean on) {
            if (mInitialStroke != null) {
                int newColor = on ? mOnColor : mInitialStroke.colour;
                int oldColor = ((Colour) mElement.style.stroke).colour;
                ((Colour) mElement.style.stroke).colour = newColor;
                return oldColor != newColor;
            } else if (mInitialFill != null) {
                int newColor = on ? mOnColor : mInitialFill.colour;
                int oldColor = ((Colour) mElement.style.fill).colour;
                ((Colour) mElement.style.fill).colour = newColor;
                return oldColor != newColor;
            }
            return false;
        }
    }
}
