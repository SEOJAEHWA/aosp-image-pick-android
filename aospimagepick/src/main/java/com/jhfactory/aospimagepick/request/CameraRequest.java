package com.jhfactory.aospimagepick.request;


import android.app.Activity;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jhfactory.aospimagepick.helper.PickImageHelper;

public final class CameraRequest extends ImagePickRequest {

    CameraRequest(PickImageHelper helper) {
        super(helper);
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

        public CameraRequest build() {
            return new CameraRequest(mHelper);
        }
    }
}
