package com.jhfactory.aospimagepick.sample;


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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PickImage {

    private static final String TAG = "PickImage";

    public static final int REQ_CODE_PICK_IMAGE_FROM_CAMERA = 4100; //카메라 촬영
    public static final int REQ_CODE_PICK_IMAGE_FROM_GALLERY = 4101; //앨범 선택
    public static final int REQ_CODE_CROP_IMAGE = 4102;

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
    public PickImage(FragmentActivity activity, boolean doCrop) {
        this.activity = activity;
        runImageCrop = doCrop;
        if (activity instanceof OnPickedImageUriCallback) {
            callback = (OnPickedImageUriCallback) activity;
        }
        else {
            throw new RuntimeException("OnPickedImageUriCallback must be implemented in activity.");
        }
        // TODO: 2017. 8. 20. check permissions
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
        // TODO: 2017. 8. 20. check permissions
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    public void openCamera() {
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
        Intent intent = Intent.createChooser(getGalleryIntent(false), "Choose");
        startActivityForResult(intent, REQ_CODE_PICK_IMAGE_FROM_GALLERY);
    }

    /**
     * Open cropper(AOSP)
     */
    public Uri cropImage(Uri imageUri) {
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

    public void setRunImageCrop(boolean runImageCrop) {
        this.runImageCrop = runImageCrop;
    }

    private boolean isOnFragment() {
        return fragment != null;
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
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", PROFILE_IMAGE_ASPECT_X);
        intent.putExtra("aspectY", PROFILE_IMAGE_ASPECT_Y);
        intent.putExtra("outputX", PROFILE_IMAGE_OUTPUT_X);
        intent.putExtra("outputY", PROFILE_IMAGE_OUTPUT_Y);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
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
}
