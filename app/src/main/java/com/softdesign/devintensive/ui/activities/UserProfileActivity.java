package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.ui.adapters.RepositoriesAdapter;
import com.softdesign.devintensive.utils.ConstantManager;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class UserProfileActivity extends BaseActivity {

    private static final String TAG = ConstantManager.TAG_PREFIX + "UserProfActivity";

    @BindViews({R.id.scoreBox_rating, R.id.scoreBox_codeLines, R.id.scoreBox_projects}) List<TextView> mTextViews_userProfileValues;

    @BindView(R.id.about_EditText) EditText mEditText_about;
    @BindView(R.id.main_coordinatorLayout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.user_photo_img) ImageView mImageView_profilePhoto;
    @BindView(R.id.repo_list) ListView mListView_repo;

    private DataManager mDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

        initProfileData();

        setupToolbar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_logout:
                logout(1);
                return true;
        }
        return false;
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            mToolbar.inflateMenu(R.menu.toolbar_menu_main);
        }
    }

    private void showSnackBar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void logout(int mode) {
        Log.d(TAG, "logout: ");
        if (mode == 1)
            mDataManager.getPreferencesManager().totalLogout();
        else mDataManager.getPreferencesManager().softLogout();
        startActivity(new Intent(this, AuthActivity.class));
    }

    private void initProfileData() {
        UserDTO userDTO = getIntent().getParcelableExtra(ConstantManager.PARCELABLE_KEY);

        final List<String> repositories = userDTO.getRepositories();
        final RepositoriesAdapter repositoriesAdapter = new RepositoriesAdapter(this, repositories);

        mListView_repo.setAdapter(repositoriesAdapter);
        mListView_repo.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + repositories.get(position))));
            }
        });

        mTextViews_userProfileValues.get(0).setText(userDTO.getRating());
        mTextViews_userProfileValues.get(1).setText(userDTO.getCodeLines());
        mTextViews_userProfileValues.get(2).setText(userDTO.getProjects());
        mEditText_about.setText(userDTO.getBio());

        mCollapsingToolbarLayout.setTitle(userDTO.getFullName());

        Picasso.with(this)
                .load(userDTO.getUserPhoto())
                .placeholder(R.drawable.user_bg)
                .error(R.drawable.user_bg)
                .fit()
                .centerInside()
                .into(mImageView_profilePhoto);
    }
}
