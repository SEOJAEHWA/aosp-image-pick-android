package com.jhfactory.aospimagepick.sample;


import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PickImage {

    private static final String TAG = "PickImage";

    private static final String[] PERMISSIONS_CAMERA = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private static final String[] PERMISSIONS_CROP = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final int REQ_CODE_PICK_IMAGE_FROM_CAMERA = 4111; //카메라 촬영
    public static final int REQ_CODE_PICK_IMAGE_FROM_GALLERY = 4121; //앨범 선택
    public static final int REQ_CODE_CROP_IMAGE = 4131;

    public static final int REQ_CODE_PERMISSION_IMAGE_PICK_CAMERA = 4110;
    public static final int REQ_CODE_PERMISSION_IMAGE_PICK_CROP = 4130;
    @Deprecated
    public static final int REQ_CODE_PERMISSION_IMAGE_PICK = 4100;

    public static final int PROFILE_IMAGE_ASPECT_X = 1;
    public static final int PROFILE_IMAGE_ASPECT_Y = 1;
    public static final int PROFILE_IMAGE_OUTPUT_X = 600;
    public static final int PROFILE_IMAGE_OUTPUT_Y = 600;

    public interface OnPickedImageUriCallback {
        void onReceiveImageUri(int resultCode, @Nullable Uri contentUri);
    }

    private FragmentActivity activity;
    private Fragment fragment;
    private boolean runImageCrop;
    private Uri contentUri;
    private OnPickedImageUriCallback callback;

    // TODO: 2017-08-22 여러가지 옵션을 빌드해서 파라미터로 넘기는 방식으로 생성자 생성
    public PickImage(FragmentActivity activity) {
        this(activity, false);
    }

    public PickImage(FragmentActivity activity, boolean doCrop) {
        this.activity = activity;
        runImageCrop = doCrop;
        if (activity instanceof OnPickedImageUriCallback) {
            callback = (OnPickedImageUriCallback) activity;
        }
        else {
            throw new RuntimeException("OnPickedImageUriCallback must be implemented in activity.");
        }
    }

    public PickImage(Fragment fragment) {
        this(fragment, false);
    }

    public PickImage(Fragment fragment, boolean doCrop) {
        this.activity = fragment.getActivity();
        this.fragment = fragment;
        runImageCrop = doCrop;
        if (fragment instanceof OnPickedImageUriCallback) {
            callback = (OnPickedImageUriCallback) fragment;
        }
        else {
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
                }
                else {
                    callback.onReceiveImageUri(resultCode, contentUri);
                }
                break;
            case REQ_CODE_PICK_IMAGE_FROM_GALLERY:
                contentUri = pickSingleImageResult(data);
                if (runImageCrop) {
                    contentUri = cropImage(contentUri);
                }
                else {
                    callback.onReceiveImageUri(resultCode, contentUri);
                }
                break;
            case REQ_CODE_CROP_IMAGE:
                callback.onReceiveImageUri(resultCode, contentUri);
                break;
        }
    }

    /**
     * @param permissions The requested permissions. Never null.
     * @param reqCode     The request code passed in.
     * @return
     */
    private boolean requestPermissions(final String[] permissions, int reqCode) {
        if (shouldShowRequestPermissionRationale(permissions)) {
            Log.d(TAG, "shouldShowRequestPermissionRationale has been called.");
        }
        if (checkPermissions(activity, permissions)) {
            Log.i(TAG, "All permissions are granted. Thanks");
            return true;
        }
        if (isOnFragment()) {
            fragment.requestPermissions(permissions, reqCode);
        }
        else {
            ActivityCompat.requestPermissions(activity, permissions, reqCode);
        }
        return false;
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *                     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_CODE_PERMISSION_IMAGE_PICK:
                if (verifyPermission(permissions, grantResults)) {
                    Log.d(TAG, "Permissions are granted.");
                    return;
                }
                break;
            case REQ_CODE_PERMISSION_IMAGE_PICK_CAMERA:
                if (!verifyPermission(permissions, grantResults)) {
                    return;
                }
                openCamera();
                break;
            case REQ_CODE_PERMISSION_IMAGE_PICK_CROP:
                if (!verifyPermission(permissions, grantResults)) {
                    return;
                }
                cropImage(contentUri);
                break;
        }
    }

    public void openCamera() {
        if (!requestPermissions(PERMISSIONS_CAMERA, REQ_CODE_PERMISSION_IMAGE_PICK_CAMERA)) {
            Log.e(TAG, "Camera permissions are not granted.");
            return;
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
    public void openGallery() {
        // Do not require permissions.
        Intent intent = Intent.createChooser(getGalleryIntent(false), "Choose");
        startActivityForResult(intent, REQ_CODE_PICK_IMAGE_FROM_GALLERY);
    }

    /**
     * Open cropper(AOSP)
     */
    public Uri cropImage(Uri imageUri) {
        if (requestPermissions(PERMISSIONS_CROP, REQ_CODE_PERMISSION_IMAGE_PICK_CROP)) {
            Log.e(TAG, "Crop permissions are not granted.");
            return null;
        }
        Intent intent = getCropIntent(activity, imageUri);
        if (intent == null) {
            Log.e(TAG, "Cropper intent is null. Cannot launch Cropper app.");
            return null;
        }
        startActivityForResult(intent, REQ_CODE_CROP_IMAGE);
        return imageUri;
    }

    /**
     * Get File object from image url that stored
     *
     * @param imageUri Stored image uri
     * @return File object
     */
    public File getFileFromUri(Uri imageUri) {
        return new File(imageUri.getPath());
    }

    /**
     * @param runImageCrop If true, run default cropper intent.
     */
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
     * @param context     context
     * @param permissions The requested permissions. Never null.
     * @return Result checkPermissions result.
     */
    private boolean checkPermissions(Context context, final String[] permissions) {
        for (String permission : permissions) {
            if (PermissionChecker.checkSelfPermission(context, permission) != PermissionChecker.PERMISSION_GRANTED) {
                Log.e(TAG, "[checkPermissions] " + permission + " is not granted.");
                return false;
            }
        }
        return true;
    }

    /**
     * @param permissions The requested permissions. Never null.
     * @return Return shouldShowRequestPermissionRationale result.
     */
    private boolean shouldShowRequestPermissionRationale(final String[] permissions) {
        boolean result = false;
        if (isOnFragment()) {
            for (String permission : permissions) {
                result = result || fragment.shouldShowRequestPermissionRationale(permission);
            }
        }
        else {
            for (String permission : permissions) {
                result = result || ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
            }
        }
        return result;
    }

    @Deprecated
    private boolean verifyPermission(String targetPermission, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int index = Arrays.asList(permissions).indexOf(targetPermission);
        return index != -1 && grantResults[index] == PermissionChecker.PERMISSION_GRANTED;
    }

    private boolean verifyPermission(@NonNull String[] permissions, @NonNull int[] grantResults) {
        for (String permission : permissions) {
            int index = Arrays.asList(permissions).indexOf(permission);
            if (index == -1 || grantResults[index] != PermissionChecker.PERMISSION_GRANTED) {
                Log.e(TAG, "[verifyPermission] " + permission + " is not granted.");
                return false;
            }
        }
        return true;
    }

    /**
     * @param chooseMultiple If true, Choose images multiple. If not, Choose single image. Now, Always be false.
     * @return created intent instance.
     */
    private Intent getGalleryIntent(boolean chooseMultiple) {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }
        else {
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
            }
            catch (IOException ex) {
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
        if (imgList == null) {
            return null;
        }
        return imgList.get(0);
    }

    /**
     * Get image uri
     *
     * @param data result data
     * @return Image uri list
     */
    @Nullable
    private List<Uri> pickImageResult(@NonNull Intent data) {
        Log.d(TAG, "[onActivityResult] REQ_CODE_ACTIVITY_IMAGE_PICK");
        List<Uri> imgList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    Log.d(TAG, "### [getClipData] URI: " + uri.toString());
                    imgList.add(uri);
                }
            }
            else {
                Uri uri = data.getData();
                Log.d(TAG, "### [getData] URI: " + uri.toString());
                imgList.add(uri);
            }
        }
        else {
            final ArrayList<Uri> imageUris = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (imageUris != null) {
                for (Uri uri : imageUris) {
                    Log.d(TAG, "### URI: " + uri.toString());
                    imgList.add(uri);
                }
            }
        }
        return imgList;
    }

    private Intent getCropIntent(Context context, Uri imageUri) {
        Uri targetUri;
        try {
            File photoFile = createImageFileOnExternal(context, ".jpg");
            targetUri = Uri.fromFile(photoFile);
        }
        catch (IOException e) {
            return null;
        }
        if (targetUri == null) {
            return null;
        }
        Log.d(TAG, "Target Uri: " + targetUri);
        context.grantUriPermission("com.android.camera", imageUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", PROFILE_IMAGE_ASPECT_X);
        intent.putExtra("aspectY", PROFILE_IMAGE_ASPECT_Y);
        intent.putExtra("outputX", PROFILE_IMAGE_OUTPUT_X);
        intent.putExtra("outputY", PROFILE_IMAGE_OUTPUT_Y);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        /* REF.
        public static final String KEY_CROPPED_RECT = "cropped-rect";
        public static final String KEY_OUTPUT_X = "outputX";
        public static final String KEY_OUTPUT_Y = "outputY";
        public static final String KEY_SCALE = "scale";
        public static final String KEY_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
        public static final String KEY_ASPECT_X = "aspectX";
        public static final String KEY_ASPECT_Y = "aspectY";
        public static final String KEY_SET_AS_WALLPAPER = "set-as-wallpaper";
        public static final String KEY_RETURN_DATA = "return-data";
        public static final String KEY_DATA = "data";
        public static final String KEY_SPOTLIGHT_X = "spotlightX";
        public static final String KEY_SPOTLIGHT_Y = "spotlightY";
        public static final String KEY_SHOW_WHEN_LOCKED = "showWhenLocked";
        public static final String KEY_OUTPUT_FORMAT = "outputFormat";

        protected static CropExtras getExtrasFromIntent(Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                return new CropExtras(extras.getInt(CropExtras.KEY_OUTPUT_X, 0),
                        extras.getInt(CropExtras.KEY_OUTPUT_Y, 0),
                        extras.getBoolean(CropExtras.KEY_SCALE, true) &&
                                extras.getBoolean(CropExtras.KEY_SCALE_UP_IF_NEEDED, false),
                        extras.getInt(CropExtras.KEY_ASPECT_X, 0),
                        extras.getInt(CropExtras.KEY_ASPECT_Y, 0),
                        extras.getBoolean(CropExtras.KEY_SET_AS_WALLPAPER, false),
                        extras.getBoolean(CropExtras.KEY_RETURN_DATA, false),
                        (Uri) extras.getParcelable(MediaStore.EXTRA_OUTPUT),
                        extras.getString(CropExtras.KEY_OUTPUT_FORMAT),
                        extras.getBoolean(CropExtras.KEY_SHOW_WHEN_LOCKED, false),
                        extras.getFloat(CropExtras.KEY_SPOTLIGHT_X),
                        extras.getFloat(CropExtras.KEY_SPOTLIGHT_Y));
            }
            return null;
        }
        */
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            return intent;
        }
        return null;
    }

    private void startActivityForResult(Intent intent, int reqCode) {
        if (isOnFragment()) {
            fragment.startActivityForResult(intent, reqCode);
        }
        else {
            activity.startActivityForResult(intent, reqCode);
        }
    }

    /**
     * Create temp file
     *
     * @param context   context
     * @param extension file extension name
     * @return File that has been created.
     * @throws IOException Exception
     */
    private File createImageFileOnExternal(Context context, String extension)
            throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, extension, storageDir);
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public void setContentUri(Uri contentUri) {
        this.contentUri = contentUri;
    }
}