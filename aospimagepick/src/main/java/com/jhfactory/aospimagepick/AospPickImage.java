package com.jhfactory.aospimagepick;


import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AospPickImage {

    private static final String TAG = "AospPickImage";

    private static final String ACTION_CROP = "com.android.camera.action.CROP";

    //    public static final String KEY_CROPPED_RECT = "cropped-rect";
    static final String KEY_OUTPUT_X = "outputX";
    static final String KEY_OUTPUT_Y = "outputY";
    static final String KEY_SCALE = "scale";
    //    static final String KEY_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
    static final String KEY_ASPECT_X = "aspectX";
    static final String KEY_ASPECT_Y = "aspectY";
    //    public static final String KEY_SET_AS_WALLPAPER = "set-as-wallpaper";
    static final String KEY_RETURN_DATA = "return-data";
    //    static final String KEY_DATA = "data";
    //    static final String KEY_SPOTLIGHT_X = "spotlightX";
    //    static final String KEY_SPOTLIGHT_Y = "spotlightY";
    //    static final String KEY_SHOW_WHEN_LOCKED = "showWhenLocked";
    static final String KEY_OUTPUT_FORMAT = "outputFormat";

    public static final int REQ_CODE_PICK_IMAGE_FROM_CAMERA = 4111;
    public static final int REQ_CODE_PICK_IMAGE_FROM_GALLERY = 4121;
    public static final int REQ_CODE_CROP_IMAGE = 4131;

    private Activity activity;
    private Fragment fragment;
    private boolean runImageCrop;
    private Uri contentUri;
    private OnPickedImageUriCallback callback;
    private Bundle imageCropExtras;

    public interface OnPickedImageUriCallback {
        void onReceiveImageUri(int resultCode, @Nullable Uri contentUri);
    }

    @SuppressWarnings("WeakerAccess, unused")
    public AospPickImage(Activity activity) {
        this.activity = activity;
        if (activity instanceof OnPickedImageUriCallback) {
            callback = (OnPickedImageUriCallback) activity;
        } else {
            throw new RuntimeException("OnPickedImageUriCallback must be implemented in activity.");
        }
    }

    @SuppressWarnings("WeakerAccess, unused")
    public AospPickImage(Fragment fragment) {
        this.activity = fragment.getActivity();
        this.fragment = fragment;
        if (fragment instanceof OnPickedImageUriCallback) {
            callback = (OnPickedImageUriCallback) fragment;
        } else {
            throw new RuntimeException("OnPickedImageUriCallback must be implemented in fragment.");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            Log.e(TAG, "OnActivityResult code is not OK >> " + resultCode);
            callback.onReceiveImageUri(resultCode, null);
            return;
        }
        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE_FROM_CAMERA:
                if (runImageCrop) {
                    contentUri = cropImage(contentUri);
                } else {
                    callback.onReceiveImageUri(resultCode, contentUri);
                    contentUri = null;
                }
                break;
            case REQ_CODE_PICK_IMAGE_FROM_GALLERY:
                contentUri = pickSingleImageResult(data);
                if (runImageCrop) {
                    contentUri = cropImage(contentUri);
                } else {
                    callback.onReceiveImageUri(resultCode, contentUri);
                    contentUri = null;
                }
                break;
            case REQ_CODE_CROP_IMAGE:
                Uri croppedImageUri = data.getData();
                if (croppedImageUri != null) {
                    contentUri = croppedImageUri;
                }
                callback.onReceiveImageUri(resultCode, contentUri);
                contentUri = null;
                break;
        }
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public void openCamera() {
        openCamera(null);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public void openCamera(@Nullable Bundle imageCropExtras) {
        runImageCrop = imageCropExtras != null;
        if (runImageCrop) {
            this.imageCropExtras = imageCropExtras;
        }
        Intent intent = getCameraIntent(activity);
        if (intent == null) {
            Log.e(TAG, "Camera intent is null. Cannot launch camera app.");
            return;
        }
        startActivityForResult(intent, REQ_CODE_PICK_IMAGE_FROM_CAMERA);
    }

    /**
     * Open default gallery, choose single image only.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void openGallery() {
        openGallery(null);
    }

    /**
     * Open default gallery, choose single image only.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void openGallery(@Nullable Bundle imageCropExtras) {
        runImageCrop = imageCropExtras != null;
        if (runImageCrop) {
            this.imageCropExtras = imageCropExtras;
        }
        Intent intent = Intent.createChooser(getGalleryIntent(false), "Choose");
        startActivityForResult(intent, REQ_CODE_PICK_IMAGE_FROM_GALLERY);
    }

    /**
     * Open cropper(AOSP)
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public Uri cropImage(Uri imageUri) {
        Uri targetUri = getImageTargetUri(activity);
        Log.d(TAG, "Target Uri: " + targetUri);
        if (targetUri == null) {
            Log.e(TAG, "Target Uri is null. return selected Uri.");
            return imageUri;
        }
        Intent intent = getCropIntent(activity, imageUri, targetUri);
        if (intent == null) {
            Log.e(TAG, "Cropper intent is null. Cannot launch Cropper app.");
            return null;
        }
        startActivityForResult(intent, REQ_CODE_CROP_IMAGE);
        return targetUri;
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

    /**
     * @param runImageCrop If true, run default cropper intent.
     */
    @SuppressWarnings("unused")
    public void setRunImageCrop(boolean runImageCrop) {
        this.runImageCrop = runImageCrop;
    }

    /**
     * @return if this class instance is created on fragment, return true.
     */
    private boolean isOnFragment() {
        return fragment != null;
    }

    /**
     * @param chooseMultiple If true, Choose images multiple. If not, Choose single image. Now, Always be false.
     * @return created intent instance.
     */
    @SuppressWarnings("SameParameterValue")
    private Intent getGalleryIntent(boolean chooseMultiple) {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, chooseMultiple);
        }
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        return intent;
    }

    private Intent getCameraIntent(Context context) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFileOnExternal(context, ".jpg");
                String authority = context.getPackageName() + ".fileprovider";
                Log.d(TAG, "authority: " + authority);
                Uri photoUri = FileProvider.getUriForFile(context, authority, photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                contentUri = photoUri;
                return takePictureIntent;
            } catch (IOException ex) {
                Log.e(TAG, "Failed to create image file.", ex);
            }
        }
        return null;
    }

    /**
     * Get first(single) image uri in list
     *
     * @param data result data
     * @return first image uri
     */
    private Uri pickSingleImageResult(@NonNull Intent data) {
        List<Uri> imgList = pickImageResult(data);
        return imgList.get(0);
    }

    /**
     * Get image uri
     *
     * @param data result data
     * @return Image uri list
     */
    @NonNull
    private List<Uri> pickImageResult(@NonNull Intent data) {
        Log.d(TAG, "[onActivityResult] REQ_CODE_ACTIVITY_IMAGE_PICK");
        List<Uri> imgList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    Log.d(TAG, "### [getClipData] URI: " + uri);
                    imgList.add(uri);
                }
            } else {
                Uri uri = data.getData();
                Log.d(TAG, "### [getData] URI: " + uri);
                imgList.add(uri);
            }
        } else {
            final ArrayList<Uri> imageUris = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (imageUris != null) {
                for (Uri uri : imageUris) {
                    Log.d(TAG, "### URI: " + uri);
                    imgList.add(uri);
                }
            }
        }
        return imgList;
    }

    private Uri getImageTargetUri(Context context) {
        if (imageCropExtras == null) {
            throw new RuntimeException("ImageCropExtras is null.");
        }
        try {
            final String extension = "." + imageCropExtras.getString(KEY_OUTPUT_FORMAT);
            File photoFile = createImageFileOnExternal(context, extension);
            return Uri.fromFile(photoFile);
        } catch (IOException e) {
            return null;
        }
    }

    public static class ImageCropExtraBuilder {

        private int mOutputX = 0;
        private int mOutputY = 0;
        private int mAspectX = 0;
        private int mAspectY = 0;
        private boolean mScale = true;
        private boolean mReturnData = false;
        //        private Uri mExtraOutput = null;
        private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG;
        //        private float mSpotlightX = 0;
        //        private float mSpotlightY = 0;

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final ImageCropExtraBuilder aspectX(@IntRange(from = 1) int aspectX) {
            this.mAspectX = aspectX;
            return this;
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final ImageCropExtraBuilder aspectY(@IntRange(from = 1) int aspectY) {
            this.mAspectY = aspectY;
            return this;
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final ImageCropExtraBuilder aspectRatio(@NonNull String aspectRatio) {
            String[] ratioArray = aspectRatio.split(":");
            if (ratioArray.length != 2) {
                throw new IllegalArgumentException("Image aspect ratio String is not suitable. ex) \"16:9\" >>" + aspectRatio);
            }
            this.mAspectX = Integer.valueOf(ratioArray[0]);
            this.mAspectY = Integer.valueOf(ratioArray[1]);
            return this;
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final ImageCropExtraBuilder outputX(@IntRange(from = 1) int outputX) {
            this.mOutputX = outputX;
            return this;
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final ImageCropExtraBuilder outputY(@IntRange(from = 1) int outputY) {
            this.mOutputY = outputY;
            return this;
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final ImageCropExtraBuilder outputSize(@NonNull String outputSize) {
            String[] outputSizeArray = outputSize.split("\\*");
            if (outputSizeArray.length != 2) {
                throw new IllegalArgumentException("Output image size String is not suitable. ex) \"160*90\" >>" + outputSize);
            }
            this.mOutputX = Integer.valueOf(outputSizeArray[0]);
            this.mOutputY = Integer.valueOf(outputSizeArray[1]);
            return this;
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final ImageCropExtraBuilder scale(boolean scale) {
            this.mScale = scale;
            return this;
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final ImageCropExtraBuilder outputFormat(Bitmap.CompressFormat format) {
            this.mOutputFormat = format;
            return this;
        }

//        private double getAspectRatio() {
//            if (mAspectX < 1 || mAspectY < 1) {
//                return 0;
//            }
//            return (double) mAspectY / (double) mAspectX;
//        }

        public final Bundle build() {
            Bundle extras = new Bundle();
            if (mAspectX > 0 || mAspectY > 0) {
                extras.putInt(KEY_ASPECT_X, mAspectX);
                extras.putInt(KEY_ASPECT_Y, mAspectY);
            }
            if (mOutputX > 0 || mOutputY > 0) {
                extras.putInt(KEY_OUTPUT_X, mOutputX);
                extras.putInt(KEY_OUTPUT_Y, mOutputY);
            }
            extras.putBoolean(KEY_SCALE, mScale);
            extras.putBoolean(KEY_RETURN_DATA, mReturnData);
//            extras.putParcelable(MediaStore.EXTRA_OUTPUT, targetImageUri);
            extras.putString(KEY_OUTPUT_FORMAT, mOutputFormat.toString());
            return extras;
        }
    }

    private Intent getCropIntent(Context context, @NonNull Uri pickedImageUri, @NonNull Uri targetImageUri) {
        final int permissionFlag = Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION;
        context.grantUriPermission("com.android.camera", pickedImageUri, permissionFlag);
        Intent intent = new Intent(ACTION_CROP);
        intent.setDataAndType(pickedImageUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtras(imageCropExtras);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, targetImageUri);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            return intent;
        }
        return null;
    }

    private void startActivityForResult(Intent intent, int reqCode) {
        if (isOnFragment()) {
            fragment.startActivityForResult(intent, reqCode);
        } else {
            activity.startActivityForResult(intent, reqCode);
        }
    }

    /**
     * Create image file that would be saved temporary.
     *
     * @param context   context
     * @param extension file extension name
     * @return File that has been created.
     * @throws IOException Exception
     */
    @SuppressWarnings("SameParameterValue")
    private File createImageFileOnExternal(Context context, String extension) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, extension, storageDir);
    }

    /**
     * Delete image file that was saved temporary.
     *
     * @param context  context
     * @param filePath file path
     * @return If delete file success, return true. If not, return false.
     */
    private boolean deleteImageFileFromExternal(Context context, String filePath) {
        // TODO: Does file delete automatically? Not sure yet.
        return false;
    }
}