package com.softdesign.devintensive.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AuthorizationActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = ConstantManager.TAG_PREFIX + "Auth Activity";
    @BindView(R.id.auth_enter_button) Button mButton_authenticate;
    @BindView(R.id.forgot_pass_button) TextView mButton_forgot_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autorization_screen);
        ButterKnife.bind(this);

        mButton_authenticate.setOnClickListener(this);
        mButton_forgot_pass.setOnClickListener(this);
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