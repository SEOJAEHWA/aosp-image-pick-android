package com.jhfactory.aospimagepick.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.jhfactory.aospimagepick.CropAfterImagePicked;
import com.jhfactory.aospimagepick.PickImage;
import com.jhfactory.aospimagepick.request.CropRequest;

/**
 * A placeholder fragment containing a simple view.
 */
public class SampleFragmentActivityFragment extends Fragment implements View.OnClickListener,
        PickImage.OnPickedPhotoUriCallback {

    private static final String TAG = SampleFragmentActivityFragment.class.getSimpleName();
    private AppCompatImageView mPickedPhotoView;
    private AppCompatCheckBox mCropCheckBox;
    private TextInputEditText mAspectRatioEditText;
    private TextInputEditText mOutputSizeEditText;
    private Group mCropGroup;

    private Uri currentPhotoUri;

    public SampleFragmentActivityFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_sample_fragment, container, false);
        root.findViewById(R.id.abtn_run_capture_intent).setOnClickListener(this);
        root.findViewById(R.id.abtn_run_gallery_intent).setOnClickListener(this);
        mPickedPhotoView = root.findViewById(R.id.aiv_picked_image);
        mCropGroup = root.findViewById(R.id.group_crop);
        mCropCheckBox = root.findViewById(R.id.acb_do_crop);
        mCropCheckBox.setChecked(false);
        mCropGroup.setVisibility(View.GONE);
        mCropCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCropGroup.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        mAspectRatioEditText = root.findViewById(R.id.tie_aspect_ratio);
        mOutputSizeEditText = root.findViewById(R.id.tie_output_size);
        return root;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            currentPhotoUri = savedInstanceState.getParcelable("uri");
            if (currentPhotoUri != null) {
                Log.i(TAG, "onRestoreInstanceState::CurrentUri: " + currentPhotoUri);
                showPickedPhoto(currentPhotoUri);
            }
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (currentPhotoUri != null) {
            outState.putParcelable("uri", currentPhotoUri);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        String aspectRatio = mAspectRatioEditText.getText().toString();
        String outputSize = mOutputSizeEditText.getText().toString();
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
        Log.i(TAG, "[onReceivePickedPhotoUri] resultCode: " + resultCode);
        Log.i(TAG, "[onReceivePickedPhotoUri] onReceiveImageUri: " + contentUri);
        if (contentUri == null) {
            Log.e(TAG, "content uri is null.");
            return;
        }
        currentPhotoUri = contentUri;
        showPickedPhotoInfo(contentUri);
        showPickedPhoto(contentUri);
    }

    private void showPickedPhotoInfo(@NonNull Uri contentUri) {
        if (getContext() == null) {
            Log.e(TAG, "Context is null. Cannot show a picked photo information.");
            return;
        }
        Log.i(TAG, "[showPickedPhotoInfo] Uri scheme: " + contentUri.getScheme());
        Log.i(TAG, "[showPickedPhotoInfo] getLastPathSegment: " + contentUri.getLastPathSegment());
        byte[] bytes = PickImage.getBytes(getContext(), contentUri);
        if (bytes != null) {
            String readableFileSize;
            readableFileSize = Formatter.formatFileSize(getContext(), bytes.length);
            Log.i(TAG, "[showPickedPhotoInfo] Size: " + readableFileSize);
        }
    }

    private void showPickedPhoto(@NonNull Uri contentUri) {
        if (getContext() == null) {
            Log.e(TAG, "Context is null. Cannot show a picked photo on view.");
            return;
        }
        String fileName = PickImage.getFileName(getContext(), contentUri);
        Log.i(TAG, "-- [showPickedPhoto] Image file name: " + fileName);
        byte[] bytes = PickImage.getBytes(getContext(), contentUri);
        if (bytes != null) {
            Glide.with(this)
                    .load(bytes)
                    .into(mPickedPhotoView);
        } else {
            throw new RuntimeException("Failed to get picked image bytes.");
        }
    }
}
