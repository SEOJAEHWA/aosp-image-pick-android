package com.jhfactory.aospimagepick.sample;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.bumptech.glide.Glide;
import com.jhfactory.aospimagepick.AospPickImage;

import java.io.File;
import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        AospPickImage.OnPickedImageUriCallback {

    private static final String TAG = "MainActivity";
    private AospPickImage aospPickImage;
    private AppCompatImageView pickedImageView;
    private AppCompatCheckBox cropCheckBox;
    private TextInputEditText aspectRatioEditText;
    private TextInputEditText outputSizeEditText;
    private Group cropGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aospPickImage = new AospPickImage(this);

        findViewById(R.id.abtn_run_capture_intent).setOnClickListener(this);
        findViewById(R.id.abtn_run_gallery_intent).setOnClickListener(this);
        pickedImageView = findViewById(R.id.aiv_picked_image);
        cropGroup = findViewById(R.id.group_crop);
        cropCheckBox = findViewById(R.id.acb_do_crop);
        cropCheckBox.setChecked(true);
        cropCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cropGroup.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        aspectRatioEditText = findViewById(R.id.tie_aspect_ratio);
        outputSizeEditText = findViewById(R.id.tie_output_size);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.abtn_run_capture_intent: // Capture button has been clicked
                if (cropCheckBox.isChecked()) {
                    aospPickImage.openCamera(getImageCropExtras());
                } else {
                    aospPickImage.openCamera();
                }
                break;
            case R.id.abtn_run_gallery_intent: // Gallery button has been clicked
                if (cropCheckBox.isChecked()) {
                    aospPickImage.openGallery(getImageCropExtras());
                } else {
                    aospPickImage.openGallery();
                }
                break;
        }
    }

    private Bundle getImageCropExtras() {
        AospPickImage.ImageCropExtraBuilder builder = new AospPickImage.ImageCropExtraBuilder();
        String aspectRatio = aspectRatioEditText.getText().toString();
        if (!TextUtils.isEmpty(aspectRatio)) {
            builder.aspectRatio(aspectRatio);
        }
        String outputSize = outputSizeEditText.getText().toString();
        if (!TextUtils.isEmpty(outputSize)) {
            builder.outputSize(outputSize);
        }
        builder.scale(true);
        builder.outputFormat(Bitmap.CompressFormat.JPEG);
        return builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AospPickImage.REQ_CODE_PICK_IMAGE_FROM_CAMERA:
            case AospPickImage.REQ_CODE_PICK_IMAGE_FROM_GALLERY:
            case AospPickImage.REQ_CODE_CROP_IMAGE:
                aospPickImage.onActivityResult(requestCode, resultCode, data);
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onReceiveImageUri(int resultCode, @Nullable Uri contentUri) {
        Log.i(TAG, "resultCode: " + resultCode);
        Log.i(TAG, "onReceiveImageUri: " + contentUri);
        if (contentUri != null) {
            Log.i(TAG, "Uri scheme: " + contentUri.getScheme());
//            if (TextUtils.equals(contentUri.getScheme(), "file")) {
//                contentUri = getUriForFile(getFileNameFromUri(contentUri));
//            }
//            else {
//                retrieveFileInfo(contentUri);
//            }
//            if (TextUtils.equals(contentUri.getScheme(), "content")) {
//                contentUri = getUriForFile(getFileNameFromUri(contentUri));
//                Log.e(TAG, "-- contentUri: " + contentUri);
//            }
        }
        Glide.with(this)
                .load(contentUri)
                .into(pickedImageView);
    }

//    private Uri getUriForFile(String fileName) {
//        File newFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
//        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", newFile);
//    }


    private String getFileNameFromUri(Uri contentUri) {
        if (getContentResolver() == null) {
            return null;
        }
        Cursor returnCursor = getContentResolver().query(contentUri, null, null, null, null);
        if (returnCursor == null) {
            return null;
        }
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String fileName = returnCursor.getString(nameIndex);
        returnCursor.close();
        return fileName;
    }

    private void retrieveFileInfo(Uri contentUri) {
        if (getContentResolver() == null) {
            return;
        }
        Cursor returnCursor = getContentResolver().query(contentUri, null, null, null, null);
        if (returnCursor == null) {
            return;
        }
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String fileName = returnCursor.getString(nameIndex);
        Long fileSize = returnCursor.getLong(sizeIndex);
        Log.i(TAG, "fileName: " + fileName);
        Log.i(TAG, "fileSize: " + getFormattedFileSize(fileSize));
        returnCursor.close();
    }

    private String getFormattedFileSize(long size) {
        String hrSize;
        double m = size / 1024.0 / 1024.0;
        DecimalFormat dec = new DecimalFormat("0.00");
        if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else {
            hrSize = dec.format(size).concat(" KB");
        }
        return hrSize;
    }
}
