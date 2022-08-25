/*
 * Project: RTAC
 * Copyright (C) 2017 alf.labs gmail com,
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

package com.alflabs.rtac.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.alflabs.kv.KeyValueClient;
import com.alflabs.manifest.Constants;
import com.alflabs.manifest.MapInfos;
import com.alflabs.manifest.Prefix;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
import com.alflabs.rtac.activity.MainActivity;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rx.AndroidSchedulers;
import com.alflabs.rx.IStream;
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

    private boolean mIsConnected;

    private SvgMapView mSvgMapView;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    public MapFragment() {
        if (DEBUG) Log.d(TAG, "new fragment");
        // Required empty public constructor
    }

    protected IFragmentComponent createComponent(Context context) {
        if (DEBUG) Log.d(TAG, "createComponent");
        return MainActivity
                .getMainActivityComponent(context)
                .getFragmentComponentFactory()
                .create();
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
        mDataClientMixin.getKeyChangedStream().subscribe(AndroidSchedulers.mainThread(), mKeyChangedSubscriber);
        mDataClientMixin.getConnectedStream().subscribe(AndroidSchedulers.mainThread(), mConnectedSubscriber);
    }

    @Override
    public void onResume() {
        if (DEBUG) Log.d(TAG, "onResume");
        super.onResume();

        // When resuming after a screen orientation change, restore the state using the information
        // from the KV client by sending a Constants.MapsKey. It is however necessary to defer this
        // using a view attach-state listener because the view is not attached yet.

        KeyValueClient kvClient = mDataClientMixin.getKeyValueClient();
        if (kvClient != null && kvClient.getValue(Constants.MapsKey) != null) {
            View.OnAttachStateChangeListener[] listener = new View.OnAttachStateChangeListener[1];
            listener[0] = new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    mSvgMapView.removeOnAttachStateChangeListener(listener[0]);
                    mKeyChangedSubscriber.onReceive(mDataClientMixin.getKeyChangedStream(), Constants.MapsKey);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                }
            };
            mSvgMapView.addOnAttachStateChangeListener(listener[0]);
        }
    }

    @Override
    public void onStop() {
        if (DEBUG) Log.d(TAG, "onStop");
        mDataClientMixin.getKeyChangedStream().remove(mKeyChangedSubscriber);
        mDataClientMixin.getConnectedStream().remove(mConnectedSubscriber);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    // ----

    private final ISubscriber<Boolean> mConnectedSubscriber = (stream, key) -> {
        mIsConnected = key;
        if (isVisible() && !isDetached() && getView() != null) {
            getView().setAlpha(mIsConnected ? 1.0f : 0.25f);
        }
    };

    private final ISubscriber<String> mKeyChangedSubscriber = new ISubscriber<String>() {
        @Override
        public void onReceive(IStream<? extends String> stream, String key) {
            if (!isVisible() || isDetached() || getView() == null) return;
            if (mSvgMapView == null) return;
            if (mDataClientMixin.getKeyValueClient() == null) return;
            assert key != null;
            String value = mDataClientMixin.getKeyValueClient().getValue(key);
            if (value == null) return;

            boolean changed = false;

            if (Constants.MapsKey.equals(key)) {
                initiazeMaps(value);

            } else if (key.startsWith(Prefix.Sensor)) {
                changed = mSvgMapView.setBlockOccupancy(key, Constants.On.equals(value));

            } else if (key.startsWith(Prefix.Turnout)) {
                changed = mSvgMapView.setTurnoutVisibility(key, Constants.Normal.equals(value));
            }

            if (changed) {
                mSvgMapView.invalidate();
            }
        }
    };

    private void initiazeMaps(String jsonMaps) {
        mSvgMapView.removeSvg();

        try {
            MapInfos infos = MapInfos.parseJson(jsonMaps);
            if (DEBUG) Log.d(TAG, "Adding 1 out " + infos.getMapInfos().length + " maps");

            mSvgMapView.loadSvg(infos.getMapInfos()[0].getSvg());

            // Update initial state of all known sensors and turnouts
            KeyValueClient kvClient = mDataClientMixin.getKeyValueClient();

            for (String key : kvClient.getKeys()) {
                if (key.startsWith(Prefix.Sensor)) {
                    String value = kvClient.getValue(key);
                    if (value != null) {
                        mSvgMapView.setBlockOccupancy(key, Constants.On.equals(value));
                    }
                } else if (key.startsWith(Prefix.Turnout)) {
                    String value = kvClient.getValue(key);
                    if (value != null) {
                        mSvgMapView.setTurnoutVisibility(key, Constants.Normal.equals(value));
                    }
                }
            }

            mSvgMapView.invalidate();

        } catch (SVGParseException | IOException e) {
            Log.e(TAG, "Parse MapInfos JSON error", e);
        }
    }
}
