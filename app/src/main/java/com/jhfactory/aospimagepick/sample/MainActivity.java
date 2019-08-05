package com.jhfactory.aospimagepick.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.jhfactory.aospimagepick.CropAfterImagePicked;
import com.jhfactory.aospimagepick.PickImage;
import com.jhfactory.aospimagepick.request.CropRequest;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import java.util.Objects;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        PickImage.OnPickedPhotoUriCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private AppCompatImageView mPickedPhotoView;
    private AppCompatCheckBox mCropCheckBox;
    private TextInputEditText mAspectRatioEditText;
    private TextInputEditText mOutputSizeEditText;
    private Group mCropGroup;

    private Uri currentPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Logger.addLogAdapter(new AndroidLogAdapter(
                PrettyFormatStrategy.newBuilder()
                        .tag("SEOJAEHWA")
                        .build()
        ) {
            @Override
            public boolean isLoggable(int priority, @Nullable String tag) {
                return BuildConfig.DEBUG;
            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.activity_name_activity);
        setSupportActionBar(toolbar);

        findViewById(R.id.abtn_run_capture_intent).setOnClickListener(this);
        findViewById(R.id.abtn_run_gallery_intent).setOnClickListener(this);
        mPickedPhotoView = findViewById(R.id.aiv_picked_image);
        mCropGroup = findViewById(R.id.group_crop);
        mCropCheckBox = findViewById(R.id.acb_do_crop);
        mCropCheckBox.setChecked(false);
        mCropGroup.setVisibility(View.GONE);
        mCropCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                mCropGroup.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        mAspectRatioEditText = findViewById(R.id.tie_aspect_ratio);
        mOutputSizeEditText = findViewById(R.id.tie_output_size);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPhotoUri = savedInstanceState.getParcelable("uri");
        if (currentPhotoUri != null) {
            Logger.i("onRestoreInstanceState::CurrentUri: " + currentPhotoUri);
            showPickedPhoto(currentPhotoUri);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.abtn_run_capture_intent: // Capture button has been clicked
                if (mCropCheckBox.isChecked()) {
                    PickImage.cameraWithCrop(this);
                } else {
                    PickImage.camera(this);
                }
                break;
            case R.id.abtn_run_gallery_intent: // Gallery button has been clicked
                if (mCropCheckBox.isChecked()) {
                    PickImage.galleryWithCrop(this);
                } else {
                    PickImage.gallery(this);
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (currentPhotoUri != null) {
            outState.putParcelable("uri", currentPhotoUri);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PickImage.onActivityResult(requestCode, resultCode, data, this);
        super.onActivityResult(requestCode, resultCode, data);

    }

    @CropAfterImagePicked(requestCodes = {
            PickImage.REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP,
            PickImage.REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP})
    public void startCropAfterImagePicked() {
        String aspectRatio = Objects.requireNonNull(mAspectRatioEditText.getText()).toString();
        String outputSize = Objects.requireNonNull(mOutputSizeEditText.getText()).toString();
        if (TextUtils.isEmpty(aspectRatio) && TextUtils.isEmpty(outputSize)) {
            PickImage.crop(this);
            return;
        }

        CropRequest.Builder builder = new CropRequest.Builder(this);
        if (!TextUtils.isEmpty(aspectRatio)) {
            builder.aspectRatio(aspectRatio);
        }
        if (!TextUtils.isEmpty(outputSize)) {
            builder.outputSize(outputSize);
        }
        builder.scale(true);
        CropRequest request = builder.build();
        PickImage.crop(request);
    }

    @Override
    public void onReceivePickedPhotoUri(int resultCode, @Nullable Uri contentUri) {
        Logger.i("[onReceivePickedPhotoUri] resultCode: " + resultCode);
        Logger.i("[onReceivePickedPhotoUri] onReceiveImageUri: " + contentUri);
        if (contentUri == null) {
            Log.e(TAG, "content uri is null.");
            return;
        }
        currentPhotoUri = contentUri;
        showPickedPhotoInfo(contentUri);
        showPickedPhoto(contentUri);
    }

    private void showPickedPhotoInfo(@NonNull Uri contentUri) {
        Logger.d("[showPickedPhotoInfo] Uri scheme: " + contentUri.getScheme());
        Logger.d("[showPickedPhotoInfo] getLastPathSegment: " + contentUri.getLastPathSegment());
        byte[] bytes = PickImage.getBytes(this, contentUri);
        if (bytes != null) {
            String readableFileSize;
            readableFileSize = Formatter.formatFileSize(this, bytes.length);
            Logger.d("[showPickedPhotoInfo] Size: " + readableFileSize);
        }
    }

    private void showPickedPhoto(@NonNull Uri contentUri) {
        String fileName = PickImage.getFileName(this, contentUri);
        Logger.i("[showPickedPhoto] Image file name: " + fileName);
        byte[] bytes = PickImage.getBytes(this, contentUri);
        if (bytes != null) {
            Glide.with(this)
                    .load(bytes)
                    .into(mPickedPhotoView);
        } else {
            throw new RuntimeException("Failed to get picked image bytes.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_fragment) {
            startActivity(new Intent(this, SampleFragmentActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
