package com.softdesign.devintensive.ui.fragments;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import com.softdesign.devintensive.data.network.api.req.EditProfileReq;
import com.softdesign.devintensive.data.network.api.res.EditProfileRes;
import com.softdesign.devintensive.data.network.api.res.UserPhotoRes;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.operations.FullUserDataOperation;
import com.softdesign.devintensive.ui.callbacks.BaseTaskCallbacks;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.NetworkUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

import static com.softdesign.devintensive.utils.UiHelper.filePathFromUri;

public class UpdateServerDataFragment extends BaseNetworkFragment {

    private static final String TAG = Const.TAG_PREFIX + "UploadInfoToServer";

    public UploadToServerCallbacks mCallbacks;

    public interface UploadToServerCallbacks extends BaseTaskCallbacks {
        void onRequestFinished(BaseModel<?> result);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof UploadToServerCallbacks) {
            mCallbacks = (UploadToServerCallbacks) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement BaseTaskCallbacks");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRequestComplete(Response response) {
        mStatus = Status.FINISHED;
        BaseModel bm = (BaseModel) response.body();
        if (mCallbacks != null) {
            if (bm.getData().getClass().isAssignableFrom(UserPhotoRes.class)) {
                BaseModel<UserPhotoRes> res = (BaseModel<UserPhotoRes>) response.body();
                mCallbacks.onRequestFinished(res);
            } else if (bm.getData().getClass().isAssignableFrom(EditProfileRes.class)) {
                BaseModel<EditProfileRes> res = (BaseModel<EditProfileRes>) response.body();
                runOperation(new FullUserDataOperation(res.getData().getUser()));
                mCallbacks.onRequestFinished(res);
            }
        }
    }

    public void uploadUserPhoto(Uri uri_SelectedImage) {

        if (uri_SelectedImage == null ||
                this.mStatus == Status.RUNNING ||
                !mDataManager.isUserAuthenticated() ||
                !NetworkUtils.isNetworkAvailable(sContext)) return;

        onRequestStarted();
        Log.d(TAG, "uploadUserPhoto: ");

        File file = new File(filePathFromUri(uri_SelectedImage));

        final RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("photo", file.getName(), requestFile);

        mDataManager.uploadUserPhoto(mDataManager.getPreferencesManager().loadBuiltInAuthId(), body)
                .enqueue(new NetworkCallback<>());
    }

    public void uploadUserAvatar(String uri_SelectedImage) {

        if (uri_SelectedImage == null ||
                this.mStatus == Status.RUNNING ||
                !mDataManager.isUserAuthenticated() ||
                !NetworkUtils.isNetworkAvailable(sContext)) return;

        File file = new File(filePathFromUri(Uri.parse(uri_SelectedImage)));

        final RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);

        mDataManager.uploadUserAvatar(mDataManager.getPreferencesManager().loadBuiltInAuthId(), body)
                .enqueue(new NetworkCallback<>());
    }

    public void uploadUserData(User user) {

        if (user == null ||
                this.mStatus == Status.RUNNING ||
                !mDataManager.isUserAuthenticated() ||
                !NetworkUtils.isNetworkAvailable(sContext)) return;

        Log.d(TAG, "uploadUserData: ");

        mDataManager.uploadUserInfo(new EditProfileReq(user).createReqBody()).enqueue(new NetworkCallback<>());
    }
}
