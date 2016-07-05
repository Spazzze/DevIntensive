package com.softdesign.devintensive.ui.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AuthorizationActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = ConstantManager.TAG_PREFIX + "Auth Activity";
    @BindView(R.id.authorization_box) LinearLayout mLinearLayout_authBox;
    @BindView(R.id.auth_enter_button) Button mButton_authenticate;
    @BindView(R.id.forgot_pass_button) TextView mButton_forgot_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autorization_screen);
        ButterKnife.bind(this);

        mButton_authenticate.setOnClickListener(this);
        mButton_forgot_pass.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mLinearLayout_authBox.setElevation(getResources().getDimension(R.dimen.size_small_8));
        } else {
            //deprecated in API 22 но мы то это запустим в АПИ ниже 21, так что все как надо.
            mLinearLayout_authBox.setBackground(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.auth_enter_button:
                this.finish();
                break;
        }
    }
}