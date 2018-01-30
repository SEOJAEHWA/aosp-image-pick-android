package com.jhfactory.aospimagepick;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jhfactory.aospimagepick.request.CameraRequest;
import com.jhfactory.aospimagepick.request.CropRequest;
import com.jhfactory.aospimagepick.request.GalleryRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class PickImage {

    private static final String TAG = "AospPickImage";
    public static final int REQ_CODE_PICK_IMAGE_FROM_CAMERA = 4111;
    public static final int REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP = 4112;
    public static final int REQ_CODE_PICK_IMAGE_FROM_GALLERY = 4121;
    public static final int REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP = 4122;
    public static final int REQ_CODE_CROP_IMAGE = 4131;

//    private static Uri mCurrentPhotoUri;

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
                                       @Nullable Uri currentPhotoUri, @NonNull Object callback) {
        if (resultCode != Activity.RESULT_OK) {
            Log.e(TAG, "OnActivityResult code is not OK >> " + resultCode);
            if (callback instanceof OnPickedImageUriCallback) {
                ((OnPickedImageUriCallback) callback).onReceiveImageUri(resultCode, null);
            }
            return;
        }
        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE_FROM_CAMERA:
                if (callback instanceof OnPickedImageUriCallback) {
                    ((OnPickedImageUriCallback) callback).onReceiveImageUri(resultCode, currentPhotoUri);
                }
                break;
            case REQ_CODE_PICK_IMAGE_FROM_GALLERY:
                if (callback instanceof OnPickedImageUriCallback) {
                    ((OnPickedImageUriCallback) callback).onReceiveImageUri(resultCode, GalleryRequest.pickSinglePhotoUri(data));
                }
                break;
            case REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP:
                runCropAfterImagePickedMethods(callback, requestCode);
                break;
            case REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP:
                runCropAfterImagePickedMethods(callback, requestCode);
                break;
            case REQ_CODE_CROP_IMAGE:
                if (callback instanceof OnPickedImageUriCallback) {
                    ((OnPickedImageUriCallback) callback).onReceiveImageUri(resultCode, currentPhotoUri);
                }
                break;
            default:
                break;
        }
    }

    private static void runCropAfterImagePickedMethods(@NonNull Object object, int requestCode) {
        Class clazz = object.getClass();
        if (isUsingAndroidAnnotations(object)) {
            clazz = clazz.getSuperclass();
        }
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                CropAfterImagePicked ann = method.getAnnotation(CropAfterImagePicked.class);
                if (ann != null) {
                    // Check for annotated methods with matching request code.
                    if (ann.requestCode() == requestCode) {
                        // Method must be void so that we can invoke it
                        if (method.getParameterTypes().length > 0) {
                            throw new RuntimeException("Cannot execute method " + method.getName()
                                    + " because it is non-void method and/or has input parameters.");
                        }
                        try {
                            // Make method accessible if private
                            if (!method.isAccessible()) {
                                method.setAccessible(true);
                            }
                            method.invoke(object);
                        } catch (IllegalAccessException e) {
                            Log.e(TAG, "runDefaultMethod:IllegalAccessException", e);
                        } catch (InvocationTargetException e) {
                            Log.e(TAG, "runDefaultMethod:InvocationTargetException", e);
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static boolean isUsingAndroidAnnotations(@NonNull Object object) {
        if (!object.getClass().getSimpleName().endsWith("_")) {
            return false;
        }
        try {
            Class clazz = Class.forName("org.androidannotations.api.view.HasViews");
            return clazz.isInstance(object);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}