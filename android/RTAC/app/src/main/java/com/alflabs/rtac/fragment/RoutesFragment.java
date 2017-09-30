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
import com.alflabs.manifest.Constants;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
import com.alflabs.rtac.activity.MainActivity;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rx.AndroidSchedulers;
import com.alflabs.rx.ISubscriber;
import com.alflabs.utils.RPair;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * Fragment showing the automation routes overview.
 */
public class RoutesFragment extends Fragment {

    private static final String TAG = RoutesFragment.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Inject DataClientMixin mDataClientMixin;

    private TextView mStatusText;
    private TextView mDebugKVView;

    public RoutesFragment() {
        if (DEBUG) Log.d(TAG, "new RoutesFragment");
        // Required empty public constructor
    }

    protected IRoutesFragmentComponent createComponent(Context context) {
        if (DEBUG) Log.d(TAG, "createComponent");
        return MainActivity.getMainActivityComponent(context).create();
    }

    // Version for API 11+, deprecated in API 23
    @Override
    public void onAttach(Activity activity) {
        if (DEBUG) Log.d(TAG, "onAttach Activity");
        super.onAttach(activity);
        IRoutesFragmentComponent component = createComponent(activity);
        component.inject(this);
    }

    // Version for API 23+
    @Override
    @TargetApi(23)
    public void onAttach(Context context) {
        if (DEBUG) Log.d(TAG, "onAttach Context");
        super.onAttach(context);
        IRoutesFragmentComponent component = createComponent(context);
        component.inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreate activity=" + getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreateView activity=" + getActivity());
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.routes_fragment, container, false);
        mStatusText = root.findViewById(R.id.data_client_status);
        mDebugKVView = root.findViewById(R.id.data_client_debug_kv);
        return root;
    }

    @Override
    public void onStart() {
        if (DEBUG) Log.d(TAG, "onStart activity=" + getActivity());
        super.onStart();
        mDataClientMixin.getStatusStream().subscribe(mDataClientStatusSubscriber, AndroidSchedulers.mainThread());
        mDataClientMixin.getKVChangedStream().subscribe(mKVChangedSubscriber, AndroidSchedulers.mainThread());
    }

    @Override
    public void onStop() {
        if (DEBUG) Log.d(TAG, "onStop");
        mDataClientMixin.getKVChangedStream().remove(mKVChangedSubscriber);
        mDataClientMixin.getStatusStream().remove(mDataClientStatusSubscriber);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    // ----


    private final ISubscriber<DataClientMixin.DataClientStatus> mDataClientStatusSubscriber = (stream, dataClientStatus) -> {
        String text = dataClientStatus.getText();
        mStatusText.setText(text == null ? "^_^" : text);
        mStatusText.setTextColor(dataClientStatus.isError() ? 0xFFFF0000 : 0xFFFFFFFF);
    };

    private final ISubscriber<RPair<String, String>> mKVChangedSubscriber = (stream, pair) -> {
        if (pair == null) return;
        String key = "[" + pair.first + "]";
        String value = pair.second;

        if (Constants.Routes.equals(key)) {
            initializeRoutes(value);
        }

        debugKVView(key, value);
    };

    private void debugKVView(final String key, final String value) {
        StringBuilder text = new StringBuilder(mDebugKVView.getText());

        if (text.length() == 0) {
            ArrayList<String> keys = new ArrayList<>(mDataClientMixin.getDataClient().getKeys());
            for (String k : keys) {
                text.append("[").append(k).append("] = ''\n");
            }
        }

        int pos = text.indexOf(key);
        if (pos < 0) {
            text.append(key).append(" = '" /* len=4 */).append(value).append("'\n");
        } else {
            pos += key.length() + 4;
            int pos2 = text.indexOf("'", pos);
            text.replace(pos, pos2, value);
        }

        mDebugKVView.setText(text.toString());
        mDebugKVView.setVisibility(View.VISIBLE);
    }

    private void initializeRoutes(final String jsonRoutes) {
        //--RouteIn
    }
}
