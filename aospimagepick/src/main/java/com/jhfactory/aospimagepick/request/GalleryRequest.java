package com.jhfactory.aospimagepick.request;


import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;


import com.jhfactory.aospimagepick.helper.PickImageHelper;

import java.util.ArrayList;
import java.util.List;

public class GalleryRequest extends ImagePickRequest {

    private static final String TAG = GalleryRequest.class.getSimpleName();

    private GalleryRequest(PickImageHelper helper) {
        super(helper);
    }

    /**
     * Get first(single) image uri in list
     *
     * @param data result data
     * @return first image uri
     */
    public static Uri pickSinglePhotoUri(@NonNull Intent data) {
        return pickPhotoUris(data).get(0);
    }

    /**
     * Get image uri
     *
     * @param data result data
     * @return Image uri list
     */
    @NonNull
    private static List<Uri> pickPhotoUris(@NonNull Intent data) {
        List<Uri> imgList = new ArrayList<>();
        final ClipData clipData = data.getClipData();
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                Log.d(TAG, "### [getClipData] URI: " + uri);
                imgList.add(uri);
            }
        } else {
            Uri uri = data.getData();
            Log.d(TAG, "### [getData] URI: " + uri);
            imgList.add(uri);
        }
        return imgList;
    }

    public static final class Builder {
        private final PickImageHelper mHelper;

        public Builder(@Nullable Activity host) {
            this.mHelper = PickImageHelper.newInstance(host);
        }

        public Builder(@Nullable Fragment host) {
            this.mHelper = PickImageHelper.newInstance(host);
        }

        public Builder(@Nullable android.app.Fragment host) {
            this.mHelper = PickImageHelper.newInstance(host);
        }

        public GalleryRequest build() {
            return new GalleryRequest(mHelper);
        }
    }
}
