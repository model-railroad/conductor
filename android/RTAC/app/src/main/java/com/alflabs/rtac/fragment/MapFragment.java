package com.alflabs.rtac.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.alflabs.manifest.Constants;
import com.alflabs.manifest.MapInfos;
import com.alflabs.manifest.Prefix;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
import com.alflabs.rtac.activity.MainActivity;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rx.AndroidSchedulers;
import com.alflabs.rx.ISubscriber;
import com.caverock.androidsvg.SVGParseException;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Fragment showing debug data.
 */
public class MapFragment extends Fragment {

    private static final String TAG = MapFragment.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Inject DataClientMixin mDataClientMixin;

    private SvgMapView mSvgMapView;

    public MapFragment() {
        if (DEBUG) Log.d(TAG, "new fragment");
        // Required empty public constructor
    }

    protected IFragmentComponent createComponent(Context context) {
        if (DEBUG) Log.d(TAG, "createComponent");
        return MainActivity.getMainActivityComponent(context).create();
    }

    // Version for API 11+, deprecated in API 23
    @Override
    public void onAttach(Activity activity) {
        if (DEBUG) Log.d(TAG, "onAttach Activity");
        super.onAttach(activity);
        IFragmentComponent component = createComponent(activity);
        component.inject(this);
    }

    // Version for API 23+
    @Override
    @TargetApi(23)
    public void onAttach(Context context) {
        if (DEBUG) Log.d(TAG, "onAttach Context");
        super.onAttach(context);
        IFragmentComponent component = createComponent(context);
        component.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreateView activity=" + getActivity());
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.map_fragment, container, false);
        mSvgMapView = root.findViewById(R.id.svg_map);
        return root;
    }

    @Override
    public void onStart() {
        if (DEBUG) Log.d(TAG, "onStart activity=" + getActivity());
        super.onStart();
        mDataClientMixin.getKeyChangedStream().subscribe(mKeyChangedSubscriber, AndroidSchedulers.mainThread());
    }

    @Override
    public void onStop() {
        if (DEBUG) Log.d(TAG, "onStop");
        mDataClientMixin.getKeyChangedStream().remove(mKeyChangedSubscriber);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    // ----

    private final ISubscriber<String> mKeyChangedSubscriber = (stream, key) -> {
        if (!isVisible()) return;
        if (mDataClientMixin.getKeyValueClient() == null) return;
        assert key != null;
        String value = mDataClientMixin.getKeyValueClient().getValue(key);
        if (value == null) return;

        boolean changed = false;
        if (Constants.MapsKey.equals(key)) {
            initiazeMaps(value);

        } else if (mSvgMapView != null && key.startsWith(Prefix.Sensor)) {
            changed = mSvgMapView.setToggleColor(key, Constants.On.equals(value));

        } else if (mSvgMapView != null && key.startsWith(Prefix.Turnout)) {
            changed = mSvgMapView.setToggleColor(key, Constants.Reverse.equals(value));
        }

        if (changed) {
            mSvgMapView.invalidate();
        }
    };

    private void initiazeMaps(String jsonMaps) {
        // TODO right now only handle a single map
        try {
            MapInfos infos = MapInfos.parseJson(jsonMaps);
            if (DEBUG) Log.d(TAG, "Adding 1 out " + infos.getMapInfos().length + " maps");

            mSvgMapView.loadSvg(infos.getMapInfos()[0].getSvg());
            mSvgMapView.invalidate();
        } catch (SVGParseException | IOException e) {
            Log.e(TAG, "Parse MapInfos JSON error", e);
        }
    }
}
