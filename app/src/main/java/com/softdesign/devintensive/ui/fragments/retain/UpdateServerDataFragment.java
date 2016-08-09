package com.softdesign.devintensive.ui.fragments.retain;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.softdesign.devintensive.data.network.NetworkRequest;
import com.softdesign.devintensive.data.network.api.req.UserEditProfileReq;
import com.softdesign.devintensive.data.network.api.res.UserAvatarRes;
import com.softdesign.devintensive.data.network.api.res.UserEditProfileRes;
import com.softdesign.devintensive.data.network.api.res.UserPhotoRes;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.operations.FullUserDataOperation;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import static com.softdesign.devintensive.data.network.NetworkRequest.ID;
import static com.softdesign.devintensive.utils.AppUtils.filePathFromUri;

public class UpdateServerDataFragment extends BaseNetworkFragment {

    private Call<BaseModel<UserPhotoRes>> mUploadPhotoCall;
    private Call<BaseModel<UserPhotoRes>> mUploadAvatarCall;
    private Call<BaseModel<UserEditProfileRes>> mUploadDataCall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Requests
    public void uploadUserPhoto(final String imageUri) {

        final ID reqId = ID.UPLOAD_PHOTO;

        if (AppUtils.isEmptyOrNull(imageUri) || imageUri.startsWith("http") || !isExecutePossible(reqId, imageUri))
            return;

        Log.d(TAG, "uploadUserPhoto: ");

        if (mUploadPhotoCall != null) mUploadPhotoCall.cancel();

        final NetworkRequest request = onRequestStarted(reqId, imageUri);

        File file = new File(filePathFromUri(Uri.parse(imageUri)));

        final RequestBody requestFile =
                RequestBody.create(Const.MEDIATYPE_MULTIPART_FORM_DATA, file);

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("photo", file.getName(), requestFile);

        mUploadPhotoCall = DATA_MANAGER.uploadUserPhoto(DATA_MANAGER.getPreferencesManager().loadBuiltInAuthId(), body);
        mUploadPhotoCall.enqueue(new NetworkCallback<BaseModel<UserPhotoRes>>(request) {

            @Override
            public void onResponse(Call<BaseModel<UserPhotoRes>> call, Response<BaseModel<UserPhotoRes>> response) {
                if (response.isSuccessful()) {
                    if (AppUtils.isEmptyOrNull(response.body())) {
                        onRequestResponseEmpty(request);
                    } else {
                        UserPhotoRes res = new UserPhotoRes((String) request.getAdditionalInfo(),
                                response.body().getData().getUpdated());
                        request.setAdditionalInfo(res);
                        runOperation(new FullUserDataOperation(res));
                        onRequestComplete(request, response);
                    }
                } else {
                    onRequestHttpError(request, AppUtils.parseHttpError(response));
                }
            }
        });
    }

    public void uploadUserAvatar(final String imageUri) {

        final ID reqId = ID.UPLOAD_AVATAR;

        if (AppUtils.isEmptyOrNull(imageUri) || imageUri.startsWith("http") || !isExecutePossible(reqId, imageUri))
            return;

        Log.d(TAG, "uploadUserAvatar: ");

        if (mUploadAvatarCall != null) mUploadAvatarCall.cancel();

        final NetworkRequest request = onRequestStarted(reqId, imageUri);

        File file = new File(filePathFromUri(Uri.parse(imageUri)));

        final RequestBody requestFile =
                RequestBody.create(Const.MEDIATYPE_MULTIPART_FORM_DATA, file);

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);

        mUploadAvatarCall = DATA_MANAGER.uploadUserAvatar(DATA_MANAGER.getPreferencesManager().loadBuiltInAuthId(), body);
        mUploadAvatarCall.enqueue(new NetworkCallback<BaseModel<UserPhotoRes>>(request) {

            @Override
            public void onResponse(Call<BaseModel<UserPhotoRes>> call, Response<BaseModel<UserPhotoRes>> response) {
                if (response.isSuccessful()) {
                    if (AppUtils.isEmptyOrNull(response.body())) {
                        onRequestResponseEmpty(request);
                    } else {
                        UserAvatarRes res = new UserAvatarRes((String) request.getAdditionalInfo(),
                                response.body().getData().getUpdated());
                        request.setAdditionalInfo(res);
                        runOperation(new FullUserDataOperation(res));
                        onRequestComplete(request, response);
                    }
                } else {
                    onRequestHttpError(request, AppUtils.parseHttpError(response));
                }
            }
        });
    }

    public void uploadUserData(ProfileViewModel model) {

        final ID reqId = ID.UPLOAD_DATA;

        if (model == null || !isExecutePossible(reqId, model)) return;
        Log.d(TAG, "uploadUserData: ");

        if (mUploadDataCall != null) mUploadDataCall.cancel();
        final NetworkRequest request = onRequestStarted(reqId, model);
        mUploadDataCall = DATA_MANAGER.uploadUserInfo(new UserEditProfileReq(model).createReqBody());

        mUploadDataCall.enqueue(new NetworkCallback<BaseModel<UserEditProfileRes>>(request) {

            @Override
            public void onResponse(Call<BaseModel<UserEditProfileRes>> call, Response<BaseModel<UserEditProfileRes>> response) {
                if (response.isSuccessful()) {
                    if (AppUtils.isEmptyOrNull(response.body())) {
                        onRequestResponseEmpty(request);
                    } else {
                        User user = response.body().getData().getUser();
                        request.setAdditionalInfo(user);
                        runOperation(new FullUserDataOperation(user));
                        onRequestComplete(request, response);
                    }
                } else {
                    onRequestHttpError(request, AppUtils.parseHttpError(response));
                }
            }
        });
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
