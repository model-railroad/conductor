package com.alflabs.rtac.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
import com.alflabs.rtac.activity.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AutomationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AutomationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AutomationFragment extends Fragment {

    private static final String TAG = AutomationFragment.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Deprecated
    private OnFragmentInteractionListener mListener;

    public AutomationFragment() {
        if (DEBUG) Log.d(TAG, "new AutomationFragment");
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    @Deprecated
    public static AutomationFragment newInstance() {
        if (DEBUG) Log.d(TAG, "newInstance");
        AutomationFragment fragment = new AutomationFragment();
        return fragment;
    }

    protected IAutomationFragmentComponent createComponent(Context context) {
        if (DEBUG) Log.d(TAG, "createComponent");
        return MainActivity.getMainActivityComponent(context).create();
    }

    // Version for API 11+, deprecated in API 23
    @Override
    public void onAttach(Activity activity) {
        if (DEBUG) Log.d(TAG, "onAttach Activity");
        super.onAttach(activity);
        IAutomationFragmentComponent component = createComponent(activity);
        component.inject(this);
    }

    // Version for API 23+
    @Override
    @TargetApi(23)
    public void onAttach(Context context) {
        if (DEBUG) Log.d(TAG, "onAttach Context");
        super.onAttach(context);
        IAutomationFragmentComponent component = createComponent(context);
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
        return inflater.inflate(R.layout.automation_fragment, container, false);
    }

    @Override
    public void onStart() {
        if (DEBUG) Log.d(TAG, "onStart activity=" + getActivity());
        super.onStart();
    }

    @Override
    public void onStop() {
        if (DEBUG) Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    @Deprecated
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
