package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.CustomGlideModule;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.ui.adapters.RepositoriesAdapter;
import com.softdesign.devintensive.utils.Const;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

import static com.softdesign.devintensive.utils.UiHelper.setListViewHeightBasedOnChildren;

public class UserProfileActivity extends BaseActivity {

    private static final String TAG = Const.TAG_PREFIX + "UserProfActivity";

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

    @SuppressWarnings("SameParameterValue")
    private void logout(int mode) {
        Log.d(TAG, "logout: ");
        if (mode == 1)
            mDataManager.getPreferencesManager().totalLogout();
        else mDataManager.getPreferencesManager().softLogout();
        startActivity(new Intent(this, AuthActivity.class));
    }

    private void initProfileData() {
        UserDTO userDTO = getIntent().getParcelableExtra(Const.PARCELABLE_KEY);

        final List<String> repositories = userDTO.getRepositories();
        final RepositoriesAdapter repositoriesAdapter = new RepositoriesAdapter(this, repositories);

        mListView_repo.setAdapter(repositoriesAdapter);
        mListView_repo.setOnItemClickListener((parent, view, position, id) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + repositories.get(position)))));

        if (repositories.size() > 0) setListViewHeightBasedOnChildren(mListView_repo);

        mTextViews_userProfileValues.get(0).setText(userDTO.getRating());
        mTextViews_userProfileValues.get(1).setText(userDTO.getCodeLines());
        mTextViews_userProfileValues.get(2).setText(userDTO.getProjects());
        mEditText_about.setText(userDTO.getBio());

        mCollapsingToolbarLayout.setTitle(userDTO.getFullName());

        CustomGlideModule.loadImage(userDTO.getUserPhoto(), R.drawable.user_bg, R.drawable.user_bg, mImageView_profilePhoto);
    }
}
