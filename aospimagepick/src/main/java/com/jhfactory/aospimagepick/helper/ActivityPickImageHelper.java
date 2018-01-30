package com.jhfactory.aospimagepick.helper;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.jhfactory.aospimagepick.ImagePickUtils;
import com.jhfactory.aospimagepick.R;
import com.jhfactory.aospimagepick.request.CropRequest;

public final class ActivityPickImageHelper extends PickImageHelper<Activity> {

    private static final String TAG = ActivityPickImageHelper.class.getSimpleName();

    ActivityPickImageHelper(Activity host) {
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
        ActivityCompat.startActivityForResult(getHost(), intent, requestCode, null);
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
        ActivityCompat.startActivityForResult(getHost(), intent, requestCode, null);
    }

    @Override
    public Uri requestCropImage(int requestCode, @NonNull Uri currentPhotoUri, Bundle imageCropExtras) {
        final String fileExtension = "." + imageCropExtras.getString(CropRequest.KEY_OUTPUT_FORMAT);
        Uri targetUri = ImagePickUtils.getImageTargetUri2(getContext(), fileExtension);
        Log.d(TAG, "Current Uri: " + currentPhotoUri);
        Log.d(TAG, "Target Uri: " + targetUri);
        if (targetUri == null) {
            Log.e(TAG, "Target Uri is null. return selected Uri.");
            return currentPhotoUri;
        }
        Intent intent = getCropIntent(currentPhotoUri, targetUri);
        if (intent == null) {
            Log.e(TAG, "Cropper intent is null. Cannot launch Cropper app.");
            return null;
        }
        ActivityCompat.startActivityForResult(getHost(), intent, requestCode, null);
        return targetUri;
    }

    @Override
    public Context getContext() {
        return getHost();
    }
}
