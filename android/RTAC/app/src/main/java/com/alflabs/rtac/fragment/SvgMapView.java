package com.alflabs.rtac.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.alflabs.rtac.BuildConfig;
import com.caverock.androidsvg.PreserveAspectRatio;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.util.Set;

public class SvgMapView extends View {

    private static final String TAG = SvgMapView.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

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

    public void loadSvg(String svgString) throws SVGParseException {
        mSvg = SVG.getFromString(svgString);
        mSvg.setDocumentPreserveAspectRatio(PreserveAspectRatio.UNSCALED);

        Set<String> viewList = mSvg.getViewList();
        if (DEBUG) Log.d(TAG, viewList.toString());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float ratio = mSvg == null ? 1.0f : mSvg.getDocumentAspectRatio();

        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);

        int w = wSize;
        int h = hSize;

        int hDesired = (int) (w * ratio);

        if (hMode == MeasureSpec.AT_MOST) {
            h = Math.min(hDesired, h);
        } else if (hMode != MeasureSpec.EXACTLY) {
            h = hDesired;
        }

        setMeasuredDimension(w, h);
        if (DEBUG) Log.d(TAG, String.format("onMeasure %f [%d %d] x [%d %d] ==> %d x %d", ratio, wMode, wSize, hMode, hSize, w, h));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSvg != null) {
            mSvg.renderToCanvas(canvas);
        }
    }
}
