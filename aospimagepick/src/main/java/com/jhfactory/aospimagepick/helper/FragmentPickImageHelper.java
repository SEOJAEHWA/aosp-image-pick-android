package com.jhfactory.aospimagepick.helper;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;

import com.jhfactory.aospimagepick.ImagePickUtils;
import com.jhfactory.aospimagepick.R;

/**
 * Framework Fragment is not supported now.
 * If you want to use fragment, use support Fragment & helper. {@link SupportFragmentPickImageHelper}
 * This helper will be used later.
 */
public final class FragmentPickImageHelper extends PickImageHelper<Fragment> {

    private static final String TAG = FragmentPickImageHelper.class.getSimpleName();

    FragmentPickImageHelper(Fragment host) {
        super(host);
    }

    @Override
    public Uri requestOpenCamera(int requestCode) {
        Uri targetUri = ImagePickUtils.getImageTargetUri(getContext());
        Log.d(TAG, "Target Uri: " + targetUri);
        if (targetUri == null) {
            Log.e(TAG, "Target Uri is null. return selected Uri.");
            return null;
        }
        Intent intent = getCameraIntent(targetUri);
        if (intent == null) {
            Log.e(TAG, "Camera intent is null. Cannot launch camera app.");
            return null;
        }
//        startActivityForResult(intent, requestCode);
        return targetUri;
    }

    @Override
    public void requestOpenGallery(int requestCode) {
        Intent intent = getGalleryIntent();
        if (intent == null) {
            Log.e(TAG, "Gallery intent is null. Cannot launch camera app.");
            return;
        }
        intent = Intent.createChooser(intent, getContext().getString(R.string.pick_image_gallery_chooser));
//        startActivityForResult(intent, requestCode);
    }

    @Override
    public Uri requestCropImage(int requestCode, @NonNull Uri currentPhotoUri, @Nullable Bundle extras) {
        final String fileExtension = "." + Bitmap.CompressFormat.JPEG;
        Uri targetUri = ImagePickUtils.getImageTargetUri2(getContext(), fileExtension);
        Log.d(TAG, "Current Uri: " + currentPhotoUri);
        Log.d(TAG, "Target Uri: " + targetUri);
        if (targetUri == null) {
            Log.e(TAG, "Target Uri is null. return selected Uri.");
            return currentPhotoUri;
        }
        Intent intent = getCropIntent(currentPhotoUri, targetUri, extras);
        if (intent == null) {
            Log.e(TAG, "Cropper intent is null. Cannot launch Cropper app.");
            return null;
        }
//        startActivityForResult(intent, requestCode);
        return targetUri;
    }

    /*private void startActivityForResult(Intent intent, int requestCode) {
        if (getHost() instanceof AppCompatActivity) {
            ActivityCompat.startActivityForResult(getHost(), intent, requestCode, null);
        } else {
            getHost().startActivityForResult(intent, requestCode);
        }
    }*/

    @Override
    public Context getContext() {
        return getHost().getActivity();
    }
}
