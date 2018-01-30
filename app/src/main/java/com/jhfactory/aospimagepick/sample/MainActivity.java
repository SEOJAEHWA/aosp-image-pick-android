package com.jhfactory.aospimagepick.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import com.jhfactory.aospimagepick.AospPickImage2;
import com.jhfactory.aospimagepick.ImagePickUtils;
import com.jhfactory.aospimagepick.request.CropRequest;
import com.jhfactory.aospimagepick.request.GalleryRequest;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        AospPickImage.OnPickedImageUriCallback, AospPickImage2.OnPickedImageUriCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private AppCompatImageView pickedImageView;
    private AppCompatCheckBox cropCheckBox;
    private TextInputEditText aspectRatioEditText;
    private TextInputEditText outputSizeEditText;
    private Group cropGroup;

    /**
     * Current picked photo Uri
     */
    private Uri mCurrentPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                    mCurrentPhotoUri = AospPickImage2.cameraWithCrop(this);
                } else {
                    mCurrentPhotoUri = AospPickImage2.camera(this);
                }
                break;
            case R.id.abtn_run_gallery_intent: // Gallery button has been clicked
                if (cropCheckBox.isChecked()) {
                    AospPickImage2.galleryWithCrop(this);
                } else {
                    AospPickImage2.gallery(this);
                }
                break;
        }
    }

    private Bundle getImageCropExtras() {
        CropRequest.Builder builder = new CropRequest.Builder(this);
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
        return builder.build().toBundle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AospPickImage2.REQ_CODE_PICK_IMAGE_FROM_CAMERA:
            case AospPickImage2.REQ_CODE_PICK_IMAGE_FROM_GALLERY:
            case AospPickImage2.REQ_CODE_CROP_IMAGE:
                AospPickImage2.onActivityResult(requestCode, resultCode, data, mCurrentPhotoUri, this);
                break;
            case AospPickImage2.REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP:
                mCurrentPhotoUri = GalleryRequest.pickSingleImageResult(data);
            case AospPickImage2.REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP:
                mCurrentPhotoUri = AospPickImage2.crop(this, mCurrentPhotoUri, getImageCropExtras());
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onReceiveImageUri(int resultCode, @Nullable Uri contentUri) {
        Log.i(TAG, "[onReceiveImageUri] resultCode: " + resultCode);
        Log.i(TAG, "[onReceiveImageUri] onReceiveImageUri: " + contentUri);
        if (contentUri != null) {
            Log.i(TAG, "[onReceiveImageUri] Uri scheme: " + contentUri.getScheme());
            Log.i(TAG, "[onReceiveImageUri] getLastPathSegment: " + contentUri.getLastPathSegment());
            if (TextUtils.equals(contentUri.getScheme(), "content")) {
                ImagePickUtils.dumpImageMetaData(this, contentUri);
            }
            try {
                byte[] bytes = ImagePickUtils.getBytes(this, contentUri);
                String readableFileSize = Formatter.formatFileSize(this, bytes.length);
                Log.i(TAG, "[onReceiveImageUri] Size: " + readableFileSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String fileName = ImagePickUtils.getFileNameFromUri(this, contentUri);
            Log.e(TAG, "-- [onReceiveImageUri] Image file name: " + fileName);
        }
        GlideApp.with(this)
                .load(contentUri)
                .into(pickedImageView);
        mCurrentPhotoUri = null;
    }
}
