package com.softdesign.devintensive.ui.activities;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.ui.fragments.DialogsFragment;
import com.softdesign.devintensive.utils.ConstantManager;

@SuppressWarnings("unused")
public class BaseActivity extends AppCompatActivity {
    private static final String TAG = ConstantManager.TAG_PREFIX + "BaseActivity";
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
            showDialogFragment(ConstantManager.DIALOG_SHOW_ERROR, getString(messageId));
            Log.d(TAG, getString(messageId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showError(String message) {
        showDialogFragment(ConstantManager.DIALOG_SHOW_ERROR, message);
        Log.d(TAG, String.valueOf(message));
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
