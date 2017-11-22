package com.jhfactory.aospimagepick.sample;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.jhfactory.aospimagepick.AospPickImage;

import java.io.IOException;


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
        cropCheckBox.setChecked(false);
        cropGroup.setVisibility(View.GONE);
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
            Log.i(TAG, "getLastPathSegment: " + contentUri.getLastPathSegment());
            if (TextUtils.equals(contentUri.getScheme(), "content")) {
                dumpImageMetaData(contentUri);
            }
            try {
                byte[] bytes = aospPickImage.getBytes(this, contentUri);
                Log.e(TAG, "Bytes length: " + Formatter.formatFileSize(this, bytes.length));
//                GlideApp.with(this)
//                        .load(bytes)
//                        .skipMemoryCache(true)
//                        .into(pickedImageView);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String fileName = getFileNameFromUri(contentUri);
            Log.e(TAG, "-- Image file name: " + fileName);
        }
        GlideApp.with(this)
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

    public void dumpImageMetaData(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i(TAG, "Display Name: " + displayName);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                String size;
                if (!cursor.isNull(sizeIndex)) {
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i(TAG, "Size: " + Formatter.formatFileSize(this, Long.valueOf(size)));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
