package com.jhfactory.aospimagepick.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.jhfactory.aospimagepick.AospPickImage;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        AospPickImage.OnPickedImageUriCallback {

    private static final String TAG = "MainActivity";
    private AospPickImage aospPickImage;
    private AppCompatImageView pickedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle imageCropExtras = new AospPickImage.ImageCropExtraBuilder()
                .aspectRatio("16:9")
//                .outputSize("300*300")
                .outputFormat(Bitmap.CompressFormat.JPEG)
                .build();
        aospPickImage = new AospPickImage(this, imageCropExtras);

        findViewById(R.id.abtn_run_capture_intent).setOnClickListener(this);
        findViewById(R.id.abtn_run_gallery_intent).setOnClickListener(this);
        pickedImageView = findViewById(R.id.aiv_picked_image);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.abtn_run_capture_intent: // Capture button has been clicked
                aospPickImage.openCamera();
                break;
            case R.id.abtn_run_gallery_intent: // Gallery button has been clicked
                aospPickImage.openGallery();
                break;
        }
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
        Glide.with(this)
                .load(contentUri)
                .into(pickedImageView);
    }
}
