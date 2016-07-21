package com.softdesign.devintensive.ui.activities;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.Toast;

import com.redmadrobot.chronos.gui.activity.ChronosAppCompatActivity;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.ui.fragments.DialogsFragment;
import com.softdesign.devintensive.utils.Const;

import de.greenrobot.event.EventBus;

@SuppressWarnings("unused")
public class BaseActivity extends ChronosAppCompatActivity {

    private static final String TAG = Const.TAG_PREFIX + "BaseActivity";

    public static final EventBus BUS = EventBus.getDefault();
    private ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.style.custom_dialog);
            mProgressDialog.setCancelable(false);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        mProgressDialog.show();
        mProgressDialog.setContentView(R.layout.progress_splash);
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void showError(int messageId) {
        try {
            showDialogFragment(Const.DIALOG_SHOW_ERROR, getString(messageId));
            Log.e(TAG, getString(messageId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showError(int dialogId, int messageId) {
        try {
            showDialogFragment(dialogId, getString(messageId));
            Log.e(TAG, getString(messageId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showError(String message) {
        showDialogFragment(Const.DIALOG_SHOW_ERROR, message);
        Log.e(TAG, String.valueOf(message));
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void showDialogFragment(int dialogId) {
        DialogFragment newFragment = DialogsFragment.newInstance(dialogId);
        newFragment.show(getFragmentManager(), newFragment.getClass().toString() + dialogId);
    }

    public void showDialogFragment(int dialogId, String message) {
        DialogFragment newFragment = DialogsFragment.newInstance(dialogId, message);
        newFragment.show(getFragmentManager(), newFragment.getClass().toString() + dialogId);
    }
}
