package com.jhfactory.aospimagepick.helper;


import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Framework Fragment is not supported now.
 * If you want to use fragment, use support Fragment & helper. {@link SupportFragmentPickImageHelper}
 * This helper will be used later.
 */
public final class FragmentPickImageHelper extends PickImageHelper<Fragment> {

    FragmentPickImageHelper(Fragment host) {
        super(host);
    }

    @Override
    public Uri requestOpenCamera(int requestCode) {
        return null;
    }

    @Override
    public Uri requestOpenCameraWithCrop(int requestCode) {
        return null;
    }

    @Override
    public void requestOpenGallery(int requestCode) {

    }

    @Override
    public Uri requestCropImage(int requestCode, @NonNull Uri currentPhotoUri, @Nullable Bundle extras) {
        return null;
    }

    @Override
    public Context getContext() {
        return getHost().getActivity();
    }
}
