package com.jhfactory.aospimagepick.helper;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.jhfactory.aospimagepick.R;

import java.io.File;
import java.io.IOException;

public final class ActivityPickImageHelper extends PickImageHelper<Activity> {

    private static final String TAG = ActivityPickImageHelper.class.getSimpleName();

    ActivityPickImageHelper(Activity host) {
        super(host);
    }

    @Override
    public Uri requestOpenCamera(int requestCode) {
        try {
            File photoFile = HelperUtils.createTempImageFileOnExternal(getContext());
            Uri targetUri = HelperUtils.getUriForFile(getContext(), photoFile);
            startAospCamera(targetUri, requestCode);
            return Uri.fromFile(photoFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Uri requestOpenCameraWithCrop(int requestCode) {
        Uri targetUri = HelperUtils.getImageTargetUri(getContext());
        startAospCamera(targetUri, requestCode);
        return targetUri;
    }

    private void startAospCamera(@Nullable Uri targetUri, int requestCode) {
        Log.d(TAG, "Target Uri: " + targetUri);
        if (targetUri == null) {
            Log.e(TAG, "Target Uri is null. return selected Uri.");
            return;
        }
        Intent intent = getCameraIntent(targetUri);
        if (intent == null) {
            Log.e(TAG, "Camera intent is null. Cannot launch camera app.");
            return;
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void requestOpenGallery(int requestCode) {
        Intent intent = getGalleryIntent();
        if (intent == null) {
            Log.e(TAG, "Gallery intent is null. Cannot launch gallery app.");
            return;
        }
        intent = Intent.createChooser(intent, getContext().getString(R.string.pick_image_gallery_chooser));
        startActivityForResult(intent, requestCode);
    }

    @Nullable
    @Override
    public Uri requestCropImage(int requestCode, @NonNull Uri currentPhotoUri, @Nullable Bundle extras) {
        final String fileExtension = Bitmap.CompressFormat.JPEG.name();
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
        startActivityForResult(intent, requestCode);
        return targetUri;
    }

    private void startActivityForResult(Intent intent, int requestCode) {
        if (getHost() instanceof AppCompatActivity) {
            ActivityCompat.startActivityForResult(getHost(), intent, requestCode, null);
        } else {
            getHost().startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public Context getContext() {
        return getHost();
    }
}
