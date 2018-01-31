package com.jhfactory.aospimagepick.request;


import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

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

        public Builder(@Nullable android.support.v4.app.Fragment host) {
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
