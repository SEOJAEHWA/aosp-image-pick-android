package com.jhfactory.aospimagepick.helper;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;

import com.jhfactory.aospimagepick.R;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public final class SupportFragmentPickImageHelper extends PickImageHelper<Fragment> {

    private static final String TAG = SupportFragmentPickImageHelper.class.getSimpleName();

    SupportFragmentPickImageHelper(Fragment host) {
        super(host);
    }

    @Nullable
    @Override
    public Uri requestOpenCamera(int requestCode) {
        try {
            File photoFile = HelperUtils.createTempImageFileOnExternal(getContext());
            final Uri targetUri = HelperUtils.getUriForFile(getContext(), photoFile);
            startAospCamera(targetUri, requestCode);
            return Uri.fromFile(photoFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    @Override
    public Uri requestOpenCameraWithCrop(int requestCode) {
        final Uri targetUri = HelperUtils.getImageTargetUri(getContext());
        startAospCamera(targetUri, requestCode);
        return targetUri;
    }

    private void startAospCamera(@Nullable Uri targetUri, int requestCode) {
        Log.d(TAG, "Target Uri: " + targetUri);
        if (targetUri == null) {
            Log.e(TAG, "Target Uri is null. return selected Uri.");
            throw new RuntimeException("Target Uri is null. return selected Uri.");
        }
        Intent intent = getCameraIntent(targetUri);
        if (intent == null) {
            Log.e(TAG, "Camera intent is null. Cannot launch camera app.");
            throw new RuntimeException("Camera intent is null. Cannot launch camera app.");
        }
        getHost().startActivityForResult(intent, requestCode);
    }

    @Override
    public void requestOpenGallery(int requestCode) {
        Intent intent = getGalleryIntent();
        if (intent == null) {
            Log.e(TAG, "Gallery intent is null. Cannot launch camera app.");
            return;
        }
        intent = Intent.createChooser(intent, getContext().getString(R.string.pick_image_gallery_chooser));
        getHost().startActivityForResult(intent, requestCode);
    }

    @Nullable
    @Override
    public Uri requestCropImage(int requestCode, @NonNull Uri currentPhotoUri, @Nullable Bundle extras) {
        final String fileExtension = "." + Bitmap.CompressFormat.JPEG;
        Uri targetUri = HelperUtils.getImageTargetFileUri(getContext(), fileExtension);
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
        getHost().startActivityForResult(intent, requestCode);
        return targetUri;
    }

    @Override
    public Context getContext() {
        return getHost().getContext();
    }
}
