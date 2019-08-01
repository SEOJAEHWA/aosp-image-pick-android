package com.jhfactory.aospimagepick.helper;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import com.jhfactory.aospimagepick.request.CropRequest;

public abstract class PickImageHelper<T> {

    private T mHost;

    public static PickImageHelper<? extends Activity> newInstance(Activity host) {
        return new ActivityPickImageHelper(host);
    }

    public static PickImageHelper<? extends Fragment> newInstance(Fragment host) {
        return new SupportFragmentPickImageHelper(host);
    }

    public static PickImageHelper<? extends android.app.Fragment> newInstance(android.app.Fragment host) {
        return new FragmentPickImageHelper(host);
    }

    PickImageHelper(T host) {
        this.mHost = host;
    }

    T getHost() {
        return mHost;
    }

    /**
     * @return get gallery app intent
     */
    @Nullable
    Intent getGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_GET_CONTENT);

        intent.setType("image/*");
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            return intent;
        }
        return null;
    }

    /**
     * @return get camera app intent
     */
    @Nullable
    Intent getCameraIntent(@NonNull Uri targetImageUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, targetImageUri);
            return intent;
        }
        return null;
    }

    /**
     * @param pickedPhotoUri picked image uri from camera or gallery
     * @param targetImageUri uri that would be stored cropped image file
     * @return get crop app intent
     */
    @Nullable
    Intent getCropIntent(@NonNull Uri pickedPhotoUri, @NonNull Uri targetImageUri, @Nullable Bundle extras) {
        final int permissionFlag = Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION;
        getContext().grantUriPermission("com.android.camera", targetImageUri, permissionFlag);
        Intent intent = new Intent(CropRequest.ACTION_CROP);
        intent.setDataAndType(pickedPhotoUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (extras != null) {
            intent.putExtras(extras);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, targetImageUri);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            return intent;
        }
        return null;
    }


    public abstract Context getContext();

    @Nullable
    public abstract Uri requestOpenCamera(int requestCode);

    @Nullable
    public abstract Uri requestOpenCameraWithCrop(int requestCode);

    public abstract void requestOpenGallery(int requestCode);

    @Nullable
    public abstract Uri requestCropImage(int requestCode, Uri currentPhotoUri, Bundle extras);
}
