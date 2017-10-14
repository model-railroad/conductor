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
import android.widget.LinearLayout;
import android.widget.TableRow;
import com.alflabs.manifest.Constants;
import com.alflabs.manifest.RouteInfo;
import com.alflabs.manifest.RouteInfos;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
import com.alflabs.rtac.activity.MainActivity;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rx.AndroidSchedulers;
import com.alflabs.rx.ISubscriber;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment showing the automation routes overview.
 */
public class RoutesFragment extends Fragment {

    private static final String TAG = RoutesFragment.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Inject DataClientMixin mDataClientMixin;

    private final List<RouteCell> mRouteCells = new ArrayList<>();
    private LinearLayout mCellsRoot;

    public RoutesFragment() {
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
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreate activity=" + getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreateView activity=" + getActivity());
        // Inflate the layout for this fragment
        mCellsRoot = (LinearLayout) inflater.inflate(R.layout.routes_fragment, container, false);
        return mCellsRoot;
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

        if (Constants.RoutesKey.equals(key)) {
            initializeRoutes(value);

        } else {
            for (RouteCell routeCell : mRouteCells) {
                routeCell.onKVChanged(key, value);
            }
        }
    };

    private void initializeRoutes(final String jsonRoutes) {
        mCellsRoot.removeAllViews();
        mRouteCells.clear();

        try {
            RouteInfos infos = RouteInfos.parseJson(jsonRoutes);
            if (DEBUG) Log.d(TAG, "Adding " + infos.getRouteInfos().length + " routes");

            LayoutInflater inflater = LayoutInflater.from(mCellsRoot.getContext());

            boolean needsSeparator = false;
            for (RouteInfo info : infos.getRouteInfos()) {
                if (needsSeparator) {
                    View sep = inflater.inflate(R.layout.route_sep, mCellsRoot, false);
                    mCellsRoot.addView(sep);
                }

                RouteCell cell = RouteCell.inflate(inflater, info, mCellsRoot, mDataClientMixin.getKeyValueClient());
                mCellsRoot.addView(cell.getView());
                mRouteCells.add(cell);

                needsSeparator = true;
            }

        } catch (IOException e) {
            Log.e(TAG, "Parse RouteInfos JSON error", e);
        }
    }
}
