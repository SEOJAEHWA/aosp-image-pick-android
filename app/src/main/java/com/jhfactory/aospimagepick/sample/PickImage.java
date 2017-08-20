package com.jhfactory.aospimagepick.sample;


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
        void onReceiveImageUri(Uri contentUri);
    }

    private boolean doImageCrop;
    private Uri contentUri;
    private OnPickedImageUriCallback callback;

    public PickImage(FragmentActivity activity, boolean doCrop) {
        // TODO: 2017. 8. 20. check permissions
        doImageCrop = doCrop;
        if (activity instanceof OnPickedImageUriCallback) {
            callback = (OnPickedImageUriCallback) activity;
        }
        else {
            throw new RuntimeException("OnPickedImageUriCallback must be implemented in activity.");
        }
    }

    public PickImage(Fragment fragment, boolean doCrop) {
        // TODO: 2017. 8. 20. check permissions
        doImageCrop = doCrop;
        if (fragment instanceof OnPickedImageUriCallback) {
            callback = (OnPickedImageUriCallback) fragment;
        }
        else {
            throw new RuntimeException("OnPickedImageUriCallback must be implemented in fragment.");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE_FROM_CAMERA:
                break;
            case REQ_CODE_PICK_IMAGE_FROM_GALLERY:
                break;
            case REQ_CODE_CROP_IMAGE:
                break;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    /**
     *
     *
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

    /**
     * Open default gallery
     *
     * @param activity caller activity
     * @param reqCode Request code for OnActivityResult.
     * @param chooseMultiple If true, Choose images multiple. If not, Choose single image. Now, Always be false.
     */
    public void openGallery(FragmentActivity activity, int reqCode, boolean chooseMultiple) {
        Intent intent = getGalleryIntent(chooseMultiple);
        activity.startActivityForResult(Intent.createChooser(intent, "Choose"), reqCode);
    }

    /**
     * Open default gallery
     *
     * @param fragment caller fragment
     * @param reqCode Request code for OnActivityResult.
     * @param chooseMultiple If true, Choose images multiple. If not, Choose single image. Now, Always be false.
     */
    public void openGallery(Fragment fragment, int reqCode, boolean chooseMultiple) {
        Intent intent = getGalleryIntent(chooseMultiple);
        fragment.startActivityForResult(Intent.createChooser(intent, "Choose"), reqCode);
    }

    public void openGallery(FragmentActivity activity, int reqCode) {
        openGallery(activity, reqCode, false);
    }

    public void openGallery(Fragment fragment, int reqCode) {
        openGallery(fragment, reqCode, false);
    }


    private Intent getCameraIntent(Context context) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFileOnExternal(context, ".jpg");
                String authority = "com.jhfactory.aospimagepick.sample.fileprovider";
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

    public void openCamera(FragmentActivity activity, int reqCode) {
        Intent intent = getCameraIntent(activity);
        if (intent != null) {
            activity.startActivityForResult(intent, reqCode);
        }
    }

    public void openCamera(Fragment fragment, int reqCode) {
        Intent intent = getCameraIntent(fragment.getContext());
        if (intent != null) {
            fragment.startActivityForResult(intent, reqCode);
        }
    }

    /**
     * Open Image Capture app(AOSP)
     *
     * @param activity caller activity.
     * @param reqCode Request code for OnActivityResult.
     * @return image uri
     */
    /*@Nullable
    public static Uri openCamera(FragmentActivity activity, int reqCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFileOnExternal(activity, ".jpg");
                String authority = "com.jhfactory.aospimagepick.fileprovider";
                Uri photoUri = FileProvider.getUriForFile(activity, authority, photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                activity.startActivityForResult(takePictureIntent, reqCode);
                return photoUri;
            }
            catch (IOException ex) {
                return null;
            }
        }
        return null;
    }*/

    /**
     * Get first(single) image uri in list
     *
     * @param data result data
     * @return first image uri
     */
    public Uri pickSingleImageResult(@NonNull Intent data) {
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
    public List<Uri> pickImageResult(@NonNull Intent data) {
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
            final ArrayList<Uri> imageUris = data.getParcelableArrayListExtra(
                    Intent.EXTRA_STREAM);
            if (imageUris != null) {
                for (Uri uri : imageUris) {
                    Log.d(TAG, "### URI: " + uri.toString());
                    imgList.add(uri);
                }
            }
        }
        return imgList;
    }

    /**
     * Open cropper(AOSP)
     *
     * @param activity caller activity.
     * @param photoUri selected or captured
     */
    public Uri cropImage(FragmentActivity activity, Uri photoUri) {

        Uri targetUri;
        try {
            File photoFile = createImageFileOnExternal(activity, ".jpg");
            targetUri = Uri.fromFile(photoFile);
        }
        catch (IOException e) {
            return null;
        }
        if (targetUri == null) {
            return null;
        }
        Log.d(TAG, "Target Uri: " + targetUri);

        activity.grantUriPermission("com.android.camera", photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");
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
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQ_CODE_CROP_IMAGE);
        }
        return targetUri;
    }

    /**
     * Create temp file
     *
     * @param context context
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

    /**
     * Get File object from image url that stored
     *
     * @param imageUri Stored image uri
     * @return File object
     */
    private File getFileFromUri(Uri imageUri) {
        return new File(imageUri.getPath());
    }
}
