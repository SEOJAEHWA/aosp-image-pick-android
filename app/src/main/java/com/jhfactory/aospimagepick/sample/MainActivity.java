package com.jhfactory.aospimagepick.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.jhfactory.aospimagepick.CropAfterImagePicked;
import com.jhfactory.aospimagepick.ImagePickUtils;
import com.jhfactory.aospimagepick.PickImage;
import com.jhfactory.aospimagepick.request.CropRequest;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        PickImage.OnPickedImageUriCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private AppCompatImageView pickedImageView;
    private AppCompatCheckBox cropCheckBox;
    private TextInputEditText aspectRatioEditText;
    private TextInputEditText outputSizeEditText;
    private Group cropGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            currentPhotoUri = savedInstanceState.getParcelable("uri");
            if (currentPhotoUri != null) {
                Log.i(TAG, "onRestoreInstanceState::CurrentUri: " + currentPhotoUri);
                showPickedImage(currentPhotoUri);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.abtn_run_capture_intent: // Capture button has been clicked
                if (cropCheckBox.isChecked()) {
                    PickImage.cameraWithCrop(this);
                } else {
                    PickImage.camera(this);
                }
                break;
            case R.id.abtn_run_gallery_intent: // Gallery button has been clicked
                if (cropCheckBox.isChecked()) {
                    PickImage.galleryWithCrop(this);
                } else {
                    PickImage.gallery(this);
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (currentPhotoUri != null) {
            outState.putParcelable("uri", currentPhotoUri);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PickImage.REQ_CODE_PICK_IMAGE_FROM_CAMERA:
            case PickImage.REQ_CODE_PICK_IMAGE_FROM_GALLERY:
            case PickImage.REQ_CODE_CROP_IMAGE:
            case PickImage.REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP:
            case PickImage.REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP:
                PickImage.onActivityResult(requestCode, resultCode, data, this);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @CropAfterImagePicked(requestCodes = {
            PickImage.REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP,
            PickImage.REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP})
    public void startCropAfterImagePicked() {
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
        CropRequest request = builder.build();
        PickImage.crop(this, request);
    }

    @Override
    public void onReceivePickedImageUri(int resultCode, @Nullable Uri contentUri) {
        Log.i(TAG, "[onReceiveImageUri] resultCode: " + resultCode);
        Log.i(TAG, "[onReceiveImageUri] onReceiveImageUri: " + contentUri);
        if (contentUri == null) {
            Log.e(TAG, "content uri is null.");
            return;
        }
        currentPhotoUri = contentUri;
        showPickedImageInfo(contentUri);
        showPickedImage(contentUri);
    }

    private void showPickedImageInfo(@NonNull Uri contentUri) {
        Log.i(TAG, "[showPickedImageInfo] Uri scheme: " + contentUri.getScheme());
        Log.i(TAG, "[showPickedImageInfo] getLastPathSegment: " + contentUri.getLastPathSegment());
        if (TextUtils.equals(contentUri.getScheme(), "content")) {
            ImagePickUtils.dumpImageMetaData(this, contentUri);
        }
        try {
            byte[] bytes = ImagePickUtils.getBytes(this, contentUri);
            if (bytes != null) {
                String readableFileSize;
                readableFileSize = Formatter.formatFileSize(this, bytes.length);
                Log.i(TAG, "[showPickedImageInfo] Size: " + readableFileSize);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Uri currentPhotoUri;
    private void showPickedImage(@NonNull Uri contentUri) {
        String fileName = ImagePickUtils.getFileNameFromUri(this, contentUri);
        Log.i(TAG, "-- [showPickedImage] Image file name: " + fileName);
        GlideApp.with(this)
                .load(contentUri)
                .into(pickedImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_fragment) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
