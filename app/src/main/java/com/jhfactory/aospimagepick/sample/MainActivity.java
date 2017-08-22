package com.jhfactory.aospimagepick.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        PickImage.OnPickedImageUriCallback {

    private static final String TAG = "MainActivity";
    private PickImage pickImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pickImage = new PickImage(this, true);
        findViewById(R.id.abtn_run_capture_intent).setOnClickListener(this);
        findViewById(R.id.abtn_run_gallery_intent).setOnClickListener(this);

        // Request permissions
        // READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.abtn_run_capture_intent: // Capture button has been clicked
                pickImage.openCamera();
                break;
            case R.id.abtn_run_gallery_intent: // Gallery button has been clicked
                pickImage.openGallery();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PickImage.REQ_CODE_PICK_IMAGE_FROM_CAMERA:
            case PickImage.REQ_CODE_PICK_IMAGE_FROM_GALLERY:
            case PickImage.REQ_CODE_CROP_IMAGE:
                pickImage.onActivityResult(requestCode, resultCode, data);
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PickImage.REQ_CODE_PERMISSION_IMAGE_PICK:
                pickImage.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onReceiveImageUri(int resultCode, @Nullable Uri contentUri) {
        Log.i(TAG, "resultCode: " + resultCode);
        Log.i(TAG, "onReceiveImageUri: " + contentUri);
//        Log.i(TAG, "FilePath: " + pickImage.getFileFromUri(contentUri).);
    }

    /**
     * Show rounded bitmap on image view.
     *
     * @param imageUri image uri you want to show
     * @param imageView target view
     */
    private void showRoundedImageOnView(Uri imageUri, final ImageView imageView) {
//        Glide.with(this)
//                .load(imageUri)
//                .asBitmap()
//                .centerCrop()
//                .into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(Bitmap resource,
//                                                GlideAnimation<? super Bitmap> glideAnimation) {
//                        Bitmap roundedBitmap = getRoundedBitmap(resource);
//                        Log.d(TAG, "roundedBitmap : " + roundedBitmap);
//                        imageView.setImageBitmap(roundedBitmap);
//                    }
//                });
    }
}
