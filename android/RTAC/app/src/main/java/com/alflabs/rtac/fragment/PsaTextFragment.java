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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.alflabs.annotations.Null;
import com.alflabs.kv.IKeyValue;
import com.alflabs.kv.KeyValueClient;
import com.alflabs.manifest.Constants;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
import com.alflabs.rtac.activity.MainActivity;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rx.AndroidSchedulers;
import com.alflabs.rx.IStream;
import com.alflabs.rx.ISubscriber;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fragment showing PSA (Public Service Announcement) text for the "Information" tab.
 */
public class PsaTextFragment extends Fragment {

    private static final String TAG = PsaTextFragment.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Inject DataClientMixin mDataClientMixin;

    private boolean mIsConnected;
    private View mRoot;
    private TextView mMainText;

    public static PsaTextFragment newInstance() {
        return new PsaTextFragment();
    }

    public PsaTextFragment() {
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
        mRoot = inflater.inflate(R.layout.psa_text_fragment, container, false);
        mMainText = (TextView) mRoot.findViewById(R.id.main_text);

        return mRoot;
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
        // from the KV client by sending a Constants.RtacPsaText. It is however necessary to defer this
        // using a view attach-state listener because the view is not attached yet.

        KeyValueClient kvClient = mDataClientMixin.getKeyValueClient();
        if (kvClient != null && kvClient.getValue(Constants.RtacPsaText) != null) {
            View.OnAttachStateChangeListener[] listener = new View.OnAttachStateChangeListener[1];
            listener[0] = new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    mRoot.removeOnAttachStateChangeListener(listener[0]);
                    mKeyChangedSubscriber.onReceive(mDataClientMixin.getKeyChangedStream(), Constants.RtacPsaText);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                }
            };
            mRoot.addOnAttachStateChangeListener(listener[0]);
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
        updateText(null);
    };

    private final ISubscriber<String> mKeyChangedSubscriber = new ISubscriber<String>() {
        @Override
        public void onReceive(IStream<? extends String> stream, String key) {
            if (!Constants.RtacPsaText.equals(key)) return;
            IKeyValue kvClient = mDataClientMixin.getKeyValueClient();
            if (kvClient == null) return;
            String value = kvClient.getValue(key);
            updateText(value);
        }
    };

    @SuppressWarnings("RegExpRedundantEscape")  // lint warning is wrong, the "redundant escape" of } is necessary.
    private static Pattern sAttribRe = Pattern.compile("^\\{([a-z]{1,2}):([^}]+)\\}(.*)");

    @SuppressLint("SetTextI18n")
    private void updateText(@Null String text) {
        if (!isVisible() || isDetached() || getView() == null) return;
        if (mRoot == null || mMainText == null) return;

        if (text == null || !mIsConnected) {
            text = "{bg:black}{b:red}{c:white}Automation Not Working";
        }

        String originalText = text;

        int txColor = Color.BLACK;
        int bgColor = Color.TRANSPARENT;
        int rootColor = Color.WHITE;

        while (!text.isEmpty()) {
            text = text.trim();
            Matcher m = sAttribRe.matcher(text);
            if (!m.matches()) {
                break;
            }
            String key = m.group(1);
            String val = m.group(2);
            text = m.group(3);

            try {
                int col = Color.parseColor(val); // Parses an HTM color name (e.g. "#RRGGBB" or "black")

                switch (key) {
                case "c":
                    // Text area font color -- defaults to black.
                    txColor = col;
                    break;
                case "b":
                    // Text area background color -- defaults to transparent.
                    bgColor = col;
                    break;
                case "bg":
                    // Root view background color -- defaults to white.
                    rootColor = col;
                default:
                    Log.d(TAG, "Ignoring invalid PSA text formatter {" + key + "} in " + originalText);
                }
            } catch (IllegalArgumentException invalidColor) {
                Log.e(TAG, "Invalid color name {..:" + val + "} in " + originalText);
            }
        }

        // Reminder: search pattern is a regex so "\" must be escaped twice.
        text = text.replaceAll("\\\\n", "\n");

        mRoot.setBackgroundColor(rootColor);
        mMainText.setBackgroundColor(bgColor);
        mMainText.setTextColor(txColor);
        mMainText.setText(text);
    }
}
