# aosp-image-pick-android
You can pick images from your default camera or default gallery. 
Also You can get the cropped selected image by default cropper.

## Installation

Download:
```groovy
dependencies {
    implementation 'com.jhfactory:aospimagepick:0.9.1'
}
```
This library depends on Android Support Library `27.0.2` so you should use `compileSdkVersion 27` or higher.

## How to use

Your Activity or Fragmrent(v4) implement the PickImage.OnPickedPhotoUriCallback
```java
public class SampleActivity extends Activity implements PickImage.OnPickedPhotoUriCallback {
    @Override
    public void onReceivePickedPhotoUri(int resultCode, @Nullable Uri contentUri) {
  
    }
}
```
```java
public class SampleFragment extends android.support.v4.app.Fragment implements PickImage.OnPickedPhotoUriCallback {
    @Override
    public void onReceivePickedPhotoUri(int resultCode, @Nullable Uri contentUri) {
 
    }
}
```

Select an image only from a camera or gallery...
```java
// Camera or Gallery only
PickImage.camera(Activity);
PickImage.gallery(Activity);
PickImage.camera(android.support.v4.app.Fragment);
PickImage.gallery(android.support.v4.app.Fragment);
```
override OnActivityResult..
Same as Activity or Fragment
```java
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PickImage.REQ_CODE_PICK_IMAGE_FROM_CAMERA:
            case PickImage.REQ_CODE_PICK_IMAGE_FROM_GALLERY:
                PickImage.onActivityResult(requestCode, resultCode, data, this);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
```

Select and crop image
```java
// Camera or Gallery & Cropper
PickImage.cameraWithCrop(Activity);
PickImage.galleryWithCrop(Activity);
PickImage.cameraWithCrop(android.support.v4.app.Fragment);
PickImage.galleryWithCrop(android.support.v4.app.Fragment);
```

override OnActivityResult..
Same as Activity or Fragment
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
        case PickImage.REQ_CODE_CROP_IMAGE:
        case PickImage.REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP:
        case PickImage.REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP:
            PickImage.onActivityResult(requestCode, resultCode, data, this);
            break;
        default:
            super.onActivityResult(requestCode, resultCode, data);
    }
}
```

Start cropper using of the `CropAfterImagePicked` annotation.
Build a CropRequest to start aosp cropper.
```java
@CropAfterImagePicked(requestCodes = {
            PickImage.REQ_CODE_PICK_IMAGE_FROM_GALLERY_WITH_CROP,
            PickImage.REQ_CODE_PICK_IMAGE_FROM_CAMERA_WITH_CROP})
public void startCropAfterImagePicked() {
    CropRequest.Builder builder = new CropRequest.Builder(this);
    String aspectRatio = mAspectRatioEditText.getText().toString();
    if (!TextUtils.isEmpty(aspectRatio)) {
        builder.aspectRatio(aspectRatio);
    }
    String outputSize = mOutputSizeEditText.getText().toString();
    if (!TextUtils.isEmpty(outputSize)) {
        builder.outputSize(outputSize);
    }
    builder.scale(true);
    CropRequest request = builder.build();
    PickImage.crop(this, request);
}
```
Finally,
You can receive selected image Uri. If contentUri is null, the image pick failed.
```java
@Override
public void onReceivePickedPhotoUri(int resultCode, @Nullable Uri contentUri) {
    Log.i(TAG, "[onReceivePickedPhotoUri] resultCode: " + resultCode);
    Log.i(TAG, "[onReceivePickedPhotoUri] onReceiveImageUri: " + contentUri);
}
```


### License
<pre>
Copyright 2017 SEOJAEHWA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>
