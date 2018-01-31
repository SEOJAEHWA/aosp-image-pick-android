package com.jhfactory.aospimagepick;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.jhfactory.aospimagepick.request.CameraRequest;
import com.jhfactory.aospimagepick.request.CropRequest;
import com.jhfactory.aospimagepick.request.GalleryRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class PickImage {

    private static final String TAG = PickImage.class.getSimpleName();
    public static final int REQ_CODE_PICK_IMAGE_FROM_CAMERA = 4111;
    public static final int REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP = 4112;
    public static final int REQ_CODE_PICK_IMAGE_FROM_GALLERY = 4121;
    public static final int REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP = 4122;
    public static final int REQ_CODE_CROP_IMAGE = 4131;

    private static Uri mCurrentPhotoUri;

    public interface OnPickedPhotoUriCallback {
        void onReceivePickedPhotoUri(int resultCode, @Nullable Uri contentUri);
    }

    public static void camera(@NonNull Activity host) {
        CameraRequest request = new CameraRequest.Builder(host).build();
        mCurrentPhotoUri = request.getHelper().requestOpenCamera(REQ_CODE_PICK_IMAGE_FROM_CAMERA);
    }

    public static void camera(@NonNull Fragment host) {
        CameraRequest request = new CameraRequest.Builder(host).build();
        mCurrentPhotoUri = request.getHelper().requestOpenCamera(REQ_CODE_PICK_IMAGE_FROM_CAMERA);
    }

    public static void cameraWithCrop(@NonNull Activity host) {
        CameraRequest request = new CameraRequest.Builder(host).build();
        mCurrentPhotoUri = request.getHelper().requestOpenCamera(REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP);
    }

    public static void cameraWithCrop(@NonNull Fragment host) {
        CameraRequest request = new CameraRequest.Builder(host).build();
        mCurrentPhotoUri = request.getHelper().requestOpenCamera(REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP);
    }

    public static void gallery(@NonNull Activity host) {
        GalleryRequest request = new GalleryRequest.Builder(host).build();
        request.getHelper().requestOpenGallery(REQ_CODE_PICK_IMAGE_FROM_GALLERY);
    }

    public static void gallery(@NonNull Fragment host) {
        GalleryRequest request = new GalleryRequest.Builder(host).build();
        request.getHelper().requestOpenGallery(REQ_CODE_PICK_IMAGE_FROM_GALLERY);
    }

    public static void galleryWithCrop(@NonNull Activity host) {
        GalleryRequest request = new GalleryRequest.Builder(host).build();
        request.getHelper().requestOpenGallery(REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP);
    }

    public static void galleryWithCrop(@NonNull Fragment host) {
        GalleryRequest request = new GalleryRequest.Builder(host).build();
        request.getHelper().requestOpenGallery(REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP);
    }

    public static void crop(@NonNull Activity host, CropRequest cropRequest) {
        crop(host, mCurrentPhotoUri, cropRequest);
    }

    public static void crop(@NonNull Fragment host, CropRequest cropRequest) {
        crop(host, mCurrentPhotoUri, cropRequest);
    }

    @SuppressWarnings("WeakerAccess")
    public static void crop(@NonNull Activity host, Uri currentPhotoUri, CropRequest cropRequest) {
        if (currentPhotoUri == null) {
            Log.e(TAG, "CurrentPhotoUri is null.");
            return;
        }
        mCurrentPhotoUri = cropRequest.getHelper().requestCropImage(REQ_CODE_CROP_IMAGE,
                currentPhotoUri, cropRequest.toBundle());
    }

    @SuppressWarnings("WeakerAccess")
    public static void crop(@NonNull Fragment host, Uri currentPhotoUri, CropRequest cropRequest) {
        if (currentPhotoUri == null) {
            Log.e(TAG, "CurrentPhotoUri is null.");
            return;
        }
        mCurrentPhotoUri = cropRequest.getHelper().requestCropImage(REQ_CODE_CROP_IMAGE,
                currentPhotoUri, cropRequest.toBundle());
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data, @NonNull Object callback) {
        if (resultCode != Activity.RESULT_OK) {
            if (callback instanceof OnPickedPhotoUriCallback) {
                ((OnPickedPhotoUriCallback) callback).onReceivePickedPhotoUri(resultCode, null);
            }
            return;
        }
        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE_FROM_CAMERA:
                if (callback instanceof OnPickedPhotoUriCallback) {
                    ((OnPickedPhotoUriCallback) callback).onReceivePickedPhotoUri(resultCode, mCurrentPhotoUri);
                }
                mCurrentPhotoUri = null;
                break;
            case REQ_CODE_PICK_IMAGE_FROM_GALLERY:
                if (callback instanceof OnPickedPhotoUriCallback) {
                    ((OnPickedPhotoUriCallback) callback).onReceivePickedPhotoUri(resultCode, GalleryRequest.pickSinglePhotoUri(data));
                }
                mCurrentPhotoUri = null;
                break;
            case REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP:
                runCropAfterImagePickedMethods(callback, requestCode);
                break;
            case REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP:
                mCurrentPhotoUri = GalleryRequest.pickSinglePhotoUri(data);
                runCropAfterImagePickedMethods(callback, requestCode);
                break;
            case REQ_CODE_CROP_IMAGE:
                if (callback instanceof OnPickedPhotoUriCallback) {
                    ((OnPickedPhotoUriCallback) callback).onReceivePickedPhotoUri(resultCode, mCurrentPhotoUri);
                }
                mCurrentPhotoUri = null;
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
                if (ann != null && getRequestCodeList(ann.requestCodes()).contains(requestCode)) {
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
            clazz = clazz.getSuperclass();
        }
    }

    private static List<Integer> getRequestCodeList(final int[] requestCodes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Arrays.stream(requestCodes).boxed().collect(Collectors.<Integer>toList());
        }
        return new ArrayList<Integer>() {{
            for (int i : requestCodes) {
                add(i);
            }
        }};
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