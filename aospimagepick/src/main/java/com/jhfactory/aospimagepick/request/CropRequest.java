package com.jhfactory.aospimagepick.request;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.jhfactory.aospimagepick.helper.PickImageHelper;

public class CropRequest extends ImagePickRequest {

    public static final String ACTION_CROP = "com.android.camera.action.CROP";
    private static final String KEY_CROPPED_RECT = "cropped-rect";
    private static final String KEY_OUTPUT_X = "outputX";
    private static final String KEY_OUTPUT_Y = "outputY";
    private static final String KEY_SCALE = "scale";
    private static final String KEY_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
    private static final String KEY_ASPECT_X = "aspectX";
    private static final String KEY_ASPECT_Y = "aspectY";
    private static final String KEY_SET_AS_WALLPAPER = "set-as-wallpaper";
    private static final String KEY_RETURN_DATA = "return-data";
    private static final String KEY_DATA = "data";
    private static final String KEY_SPOTLIGHT_X = "spotlightX";
    private static final String KEY_SPOTLIGHT_Y = "spotlightY";
    private static final String KEY_SHOW_WHEN_LOCKED = "showWhenLocked";
    private static final String KEY_OUTPUT_FORMAT = "outputFormat";

    private int mOutputX;
    private int mOutputY;
    private int mAspectX;
    private int mAspectY;
    private boolean mScale;
    private boolean mReturnData;
    //        private Uri mExtraOutput = null;
    private Bitmap.CompressFormat mOutputFormat;
    //        private float mSpotlightX = 0;
    //        private float mSpotlightY = 0;

    private CropRequest(PickImageHelper helper, int outputX, int outputY, int aspectX, int aspectY,
                        boolean scale, boolean returnData, Bitmap.CompressFormat outputFormat) {
        super(helper);
        this.mOutputX = outputX;
        this.mOutputY = outputY;
        this.mAspectX = aspectX;
        this.mAspectY = aspectY;
        this.mScale = scale;
        this.mReturnData = returnData;
        this.mOutputFormat = outputFormat;
    }

    public static final class Builder {
        private final PickImageHelper mHelper;
        private int mOutputX = 0;
        private int mOutputY = 0;
        private int mAspectX = 0;
        private int mAspectY = 0;
        private boolean mScale = true;
        private boolean mReturnData = false;
        //        private Uri mExtraOutput = null;
        private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG;
        //        private float mSpotlightX = 0;
        //        private float mSpotlightY = 0;

        public Builder(@Nullable Activity host) {
            this.mHelper = PickImageHelper.newInstance(host);
        }

        public Builder(@Nullable Fragment host) {
            this.mHelper = PickImageHelper.newInstance(host);
        }

        public Builder(@Nullable android.app.Fragment host) {
            this.mHelper = PickImageHelper.newInstance(host);
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final Builder aspectX(@IntRange(from = 1) int aspectX) {
            this.mAspectX = aspectX;
            return this;
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final Builder aspectY(@IntRange(from = 1) int aspectY) {
            this.mAspectY = aspectY;
            return this;
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final Builder aspectRatio(@NonNull String aspectRatio) {
            String[] ratioArray = aspectRatio.split(":");
            if (ratioArray.length != 2) {
                throw new IllegalArgumentException("Image aspect ratio String is not suitable. ex) \"16:9\" >>" + aspectRatio);
            }
            this.mAspectX = Integer.valueOf(ratioArray[0]);
            this.mAspectY = Integer.valueOf(ratioArray[1]);
            return this;
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final Builder outputX(@IntRange(from = 1) int outputX) {
            this.mOutputX = outputX;
            return this;
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final Builder outputY(@IntRange(from = 1) int outputY) {
            this.mOutputY = outputY;
            return this;
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final Builder outputSize(@NonNull String outputSize) {
            String[] outputSizeArray = outputSize.split("\\*");
            if (outputSizeArray.length != 2) {
                throw new IllegalArgumentException("Output image size String is not suitable. ex) \"160*90\" >>" + outputSize);
            }
            this.mOutputX = Integer.valueOf(outputSizeArray[0]);
            this.mOutputY = Integer.valueOf(outputSizeArray[1]);
            return this;
        }

        @SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final Builder scale(boolean scale) {
            this.mScale = scale;
            return this;
        }

        /*@SuppressWarnings({"SameParameterValue", "unused", "UnusedReturnValue"})
        public final Builder outputFormat(Bitmap.CompressFormat format) {
            this.mOutputFormat = format;
            return this;
        }*/

        /*private double getAspectRatio() {
            if (mAspectX < 1 || mAspectY < 1) {
                return 0;
            }
            return (double) mAspectY / (double) mAspectX;
        }*/

        public CropRequest build() {
            return new CropRequest(mHelper, mOutputX, mOutputY, mAspectX, mAspectY, mScale,
                    mReturnData, mOutputFormat);
        }
    }

    public final Bundle toBundle() {
        Bundle extras = new Bundle();
        if (mAspectX > 0 || mAspectY > 0) {
            extras.putInt(KEY_ASPECT_X, mAspectX);
            extras.putInt(KEY_ASPECT_Y, mAspectY);
        }
        if (mOutputX > 0 || mOutputY > 0) {
            extras.putInt(KEY_OUTPUT_X, mOutputX);
            extras.putInt(KEY_OUTPUT_Y, mOutputY);
        }
        extras.putBoolean(KEY_SCALE, mScale);
        extras.putBoolean(KEY_RETURN_DATA, mReturnData);
//            extras.putParcelable(MediaStore.EXTRA_OUTPUT, targetImageUri);
        extras.putString(KEY_OUTPUT_FORMAT, mOutputFormat.toString());
        return extras;
    }
}
