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
import android.widget.TextView;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
import com.alflabs.rtac.activity.MainActivity;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rx.AndroidSchedulers;
import com.alflabs.rx.ISubscriber;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * Fragment showing debug data.
 */
public class DebugFragment extends Fragment {

    private static final String TAG = DebugFragment.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Inject DataClientMixin mDataClientMixin;

    private TextView mDebugView;

    public DebugFragment() {
        if (DEBUG) Log.d(TAG, "new DebugFragment");
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
        View root = inflater.inflate(R.layout.debug_fragment, container, false);
        mDebugView = root.findViewById(R.id.debug_text);
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

        debugKVView(key, value);
    };

    private void debugKVView(String key, String value) {
        StringBuilder text = new StringBuilder(mDebugView.getText());

        if (text.length() == 0) {
            ArrayList<String> keys = new ArrayList<>(mDataClientMixin.getKeyValueClient().getKeys());
            for (String k : keys) {
                text.append("[").append(k).append("] = ''\n");
            }
        }

        key = "[" + key + "]";
        int pos = text.indexOf(key);
        if (pos < 0) {
            text.append(key).append(" = '" /* len=4 */).append(value).append("'\n");
        } else {
            pos += key.length() + 4;
            int pos2 = text.indexOf("'", pos);
            text.replace(pos, pos2, value);
        }

        mDebugView.setText(text.toString());
        mDebugView.setVisibility(View.VISIBLE);
    }
}
