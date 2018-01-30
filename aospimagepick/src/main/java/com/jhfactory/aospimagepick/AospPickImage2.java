package com.jhfactory.aospimagepick;


import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jhfactory.aospimagepick.request.CameraRequest;
import com.jhfactory.aospimagepick.request.CropRequest;
import com.jhfactory.aospimagepick.request.GalleryRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class AospPickImage2 {

    private static final String TAG = "AospPickImage";
    public static final int REQ_CODE_PICK_IMAGE_FROM_CAMERA = 4111;
    public static final int REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP = 4112;
    public static final int REQ_CODE_PICK_IMAGE_FROM_GALLERY = 4121;
    public static final int REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP = 4122;
    public static final int REQ_CODE_CROP_IMAGE = 4131;

    private OnPickedImageUriCallback callback;
    private Bundle imageCropExtras;

    public interface OnPickedImageUriCallback {
        void onReceiveImageUri(int resultCode, @Nullable Uri contentUri);
    }

    public static Uri camera(@NonNull Activity host) {
        CameraRequest request = new CameraRequest.Builder(host).build();
        return request.getHelper().requestOpenCamera(REQ_CODE_PICK_IMAGE_FROM_CAMERA);
    }

    public static Uri cameraWithCrop(@NonNull Activity host) {
        CameraRequest request = new CameraRequest.Builder(host).build();
        return request.getHelper().requestOpenCamera(REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP);
    }

//    public static void camera(@NonNull Fragment host) {
//
//    }

    public static void gallery(@NonNull Activity host) {
        GalleryRequest request = new GalleryRequest.Builder(host).build();
        request.getHelper().requestOpenGallery(REQ_CODE_PICK_IMAGE_FROM_GALLERY);
    }

    public static void galleryWithCrop(@NonNull Activity host) {
        GalleryRequest request = new GalleryRequest.Builder(host).build();
        request.getHelper().requestOpenGallery(REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP);
    }

    public static Uri crop(@NonNull Activity host, @Nullable Uri currentPhotoUri, Bundle imageCropExtras) {
        if (currentPhotoUri == null) {
            Log.e(TAG, "CurrentPhotoUri is null.");
            return null;
        }
        CropRequest request = new CropRequest.Builder(host).build();
        return request.getHelper().requestCropImage(REQ_CODE_CROP_IMAGE, currentPhotoUri, imageCropExtras);
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data,
                                        @Nullable Uri currentPhotoUri,
                                        @NonNull OnPickedImageUriCallback callback) {
//        if (resultCode != Activity.RESULT_OK) {
//            Log.e(TAG, "OnActivityResult code is not OK >> " + resultCode);
//            callback.onReceiveImageUri(resultCode, null);
//            return;
//        }
        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE_FROM_CAMERA:
                callback.onReceiveImageUri(resultCode, currentPhotoUri);
                break;
            case REQ_CODE_PICK_IMAGE_FROM_GALLERY:
                callback.onReceiveImageUri(resultCode, GalleryRequest.pickSingleImageResult(data));
                break;
            case REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP:
            case REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP:
                // nothing to do.
                break;
            case REQ_CODE_CROP_IMAGE:
                callback.onReceiveImageUri(resultCode, currentPhotoUri);
                break;
        }
    }

    /**
     * Get File object from image url that stored
     *
     * @param imageUri Stored image uri
     * @return File object
     */
    @SuppressWarnings("unused")
    public File getFileFromUri(Uri imageUri) {
        return new File(imageUri.getPath());
    }

}