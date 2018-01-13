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
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.alflabs.kv.KeyValueClient;
import com.alflabs.manifest.Constants;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
import com.alflabs.rtac.activity.MainActivity;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rx.AndroidSchedulers;
import com.alflabs.rx.ISubscriber;

import javax.inject.Inject;

/**
 * Fragment showing the E-Stop in Normal / Active / Reset states.
 */
public class EStopFragment extends Fragment {

    private static final String TAG = EStopFragment.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Inject DataClientMixin mDataClientMixin;

    private TextView mInstructions;
    private Button mEStopButton;
    private Button mResetButton;
    private View mResetGroup;

    public EStopFragment() {
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
        View root = inflater.inflate(R.layout.estop_fragment, container, false);
        mEStopButton = root.findViewById(R.id.estop_button);
        mResetGroup = root.findViewById(R.id.reset_group);
        mInstructions = root.findViewById(R.id.reset_instructions_text);
        mResetButton = root.findViewById(R.id.reset_button);

        Context context = new ContextThemeWrapper(mEStopButton.getContext(), R.style.InvertedTheme);

        mEStopButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.estop_alert_title)
                    .setMessage(R.string.estop_alert_text)
                    .setPositiveButton(R.string.estop_button_title,
                            (dialogInterface, i) -> changeState(Constants.EStopState.ACTIVE))
                    .setNegativeButton(android.R.string.cancel, null);

            builder.show();
        });

        mResetButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.reset_alert_title)
                    .setMessage(R.string.reset_alert_text)
                    .setPositiveButton(R.string.reset_button_title,
                            (dialogInterface, i) -> changeState(Constants.EStopState.RESET))
                    .setNegativeButton(android.R.string.cancel, null);
            builder.show();
        });

        return root;
    }

    private void changeState(Constants.EStopState state) {
        if (DEBUG) Log.d(TAG, "changeState: " + state);
        if (mDataClientMixin != null && mDataClientMixin.getKeyValueClient() != null) {
            mDataClientMixin.getKeyValueClient().broadcastValue(Constants.EStopKey, state.toString());
        }
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


    @Override
    public void onResume() {
        if (DEBUG) Log.d(TAG, "onResume");
        super.onResume();

        // When resuming after a screen orientation change, restore the state using the information
        // from the KV client by sending a Constants.EStopKey. It is however necessary to defer this
        // using a view attach-state listener because the view is not attached yet.

        final View view = getView();
        KeyValueClient kvClient = mDataClientMixin.getKeyValueClient();
        if (view != null && kvClient != null && kvClient.getValue(Constants.EStopKey) != null) {
            View.OnAttachStateChangeListener[] listener = new View.OnAttachStateChangeListener[1];
            listener[0] = new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    view.removeOnAttachStateChangeListener(listener[0]);
                    mKeyChangedSubscriber.onReceive(mDataClientMixin.getKeyChangedStream(), Constants.EStopKey);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                }
            };
            view.addOnAttachStateChangeListener(listener[0]);
        }
    }

    // ----

    private final ISubscriber<String> mKeyChangedSubscriber = (stream, key) -> {
        if (!Constants.EStopKey.equals(key)) {
            return;
        }

        if (!isVisible()) {
            if (DEBUG) Log.d(TAG, "mKeyChangedSubscriber IGNORED: isVisible is " + isVisible());
            return;
        }
        if (mDataClientMixin.getKeyValueClient() == null) {
            if (DEBUG) Log.d(TAG, "mKeyChangedSubscriber IGNORED: mDataClientMixin is " + mDataClientMixin);
            return;
        }

        String value = mDataClientMixin.getKeyValueClient().getValue(key);
        try {
            Constants.EStopState state = Constants.EStopState.valueOf(value);
            if (DEBUG) Log.d(TAG, "E-STOP state changed to " + state);

            switch (state) {
            case NORMAL:
                mEStopButton.setVisibility(View.VISIBLE);
                mResetGroup.setVisibility(View.GONE);
                mResetButton.setVisibility(View.GONE);
                break;
            case ACTIVE:
                mEStopButton.setVisibility(View.GONE);
                mResetGroup.setVisibility(View.VISIBLE);
                mInstructions.setText(R.string.reset_instructions_text);
                mResetButton.setVisibility(View.VISIBLE);
                break;
            case RESET:
                mEStopButton.setVisibility(View.GONE);
                mResetGroup.setVisibility(View.VISIBLE);
                mInstructions.setText(R.string.reset_pending_text);
                mResetButton.setVisibility(View.GONE);
                break;
            }

        } catch (NullPointerException | IllegalArgumentException e) {
            if (DEBUG) Log.d(TAG, "E-STOP state changed to INVALID value " + value);
        }
    };
}
