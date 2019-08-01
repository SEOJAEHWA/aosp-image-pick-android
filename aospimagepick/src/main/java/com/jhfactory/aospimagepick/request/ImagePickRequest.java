package com.jhfactory.aospimagepick.request;


import androidx.annotation.NonNull;

import com.jhfactory.aospimagepick.helper.PickImageHelper;

public class ImagePickRequest {

    private PickImageHelper mHelper;

    ImagePickRequest(PickImageHelper helper) {
        this.mHelper = helper;
    }

    @NonNull
    public PickImageHelper getHelper() {
        return mHelper;
    }
}
