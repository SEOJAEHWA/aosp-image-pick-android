package com.jhfactory.aospimagepick.request;


import android.support.annotation.NonNull;

import com.jhfactory.aospimagepick.helper.PickImageHelper;

public class ImagePickRequest {

    private PickImageHelper mHelper;

    public ImagePickRequest(PickImageHelper helper) {
        this.mHelper = helper;
    }

    @NonNull
    public PickImageHelper getHelper() {
        return mHelper;
    }
}
