package com.softdesign.devintensive.ui.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.ui.callbacks.MainActivityCallback;
import com.softdesign.devintensive.utils.ConstantManager;

public class DialogsFragment extends DialogFragment {
    private static final String TAG = ConstantManager.TAG_PREFIX + "DialogsFragment";

    private MainActivityCallback mCallback;

    public static DialogsFragment newInstance(int type) {
        DialogsFragment dialogsFragment = new DialogsFragment();
        Bundle args = new Bundle();
        args.putInt(ConstantManager.DIALOG_FRAGMENT_KEY, type);
        dialogsFragment.setArguments(args);
        return dialogsFragment;
    }

    public static DialogsFragment newInstance(int type, String content) {
        DialogsFragment dialogsFragment = new DialogsFragment();
        Bundle args = new Bundle();
        args.putInt(ConstantManager.DIALOG_FRAGMENT_KEY, type);
        args.putString(ConstantManager.DIALOG_CONTENT_KEY, content);
        dialogsFragment.setArguments(args);
        return dialogsFragment;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof MainActivityCallback) {
            mCallback = (MainActivityCallback) activity;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int type = getArguments().getInt(ConstantManager.DIALOG_FRAGMENT_KEY);
        switch (type) {
            case ConstantManager.DIALOG_LOAD_PROFILE_PHOTO:
                if (mCallback != null) return loadPhotoDialog();
                else
                    throw new IllegalStateException("Parent activity must implement MainActivityCallback");
            case ConstantManager.DIALOG_SHOW_ERROR:
                return errorAlertDialog(getArguments().getString(ConstantManager.DIALOG_CONTENT_KEY));
            default:
                return errorAlertDialog(getString(R.string.error));
        }
    }

    //region Dialogs
    private Dialog loadPhotoDialog() {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.header_profile_placeHolder_loadPhotoDialog_title))
                .setItems(R.array.profile_placeHolder_loadPhotoDialog, (dialog, chosenItem) -> {
                    switch (chosenItem) {
                        case 0:
                            mCallback.loadPhotoFromCamera();
                            break;
                        case 1:
                            mCallback.loadPhotoFromGallery();
                            break;
                        case 2:
                            dialog.cancel();
                            break;
                    }
                }).create();
    }

    public Dialog errorAlertDialog(String error) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(error)
                .setCancelable(true)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    dialog.cancel();
                }).create();
    }
    //endregion
}
