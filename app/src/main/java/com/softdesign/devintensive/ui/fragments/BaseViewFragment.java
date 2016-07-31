package com.softdesign.devintensive.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.redmadrobot.chronos.gui.fragment.ChronosSupportFragment;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.ui.callbacks.MainActivityCallback;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import de.greenrobot.event.EventBus;

@SuppressWarnings("deprecation")
public class BaseViewFragment extends ChronosSupportFragment {

    public final String TAG = Const.TAG_PREFIX + getClass().getSimpleName();
    public static final DataManager DATA_MANAGER = DataManager.getInstance();
    public static final EventBus BUS = EventBus.getDefault();
    public static final Context CONTEXT = DevIntensiveApplication.getContext();

    public MainActivityCallback mCallbacks;

    //region :::::::::::::::::::::::::::::::::::::::::: Life cycle

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach: ");
        if (activity instanceof MainActivityCallback) {
            mCallbacks = (MainActivityCallback) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement MainActivityCallback");
        }
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: ");
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: ");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: ");
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
