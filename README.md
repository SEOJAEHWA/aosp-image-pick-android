# aosp-image-pick-android
You can pick images from your default camera or default gallery. 
Also You can get the cropped selected image by default cropper.


## Installation

Download:
```groovy
dependencies {
    implementation 'com.jhfactory:aospimagepick:0.9.3'
}
```
This library depends on Android Support Library `27.0.2` so you should use `compileSdkVersion 27` or higher.


## How to use

Your Activity or Fragmrent(v4) implement the PickImage.OnPickedPhotoUriCallback to get result.
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
And set up FileProvider...
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.myapp">
    <application
        ...>
        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="com.example.myapp.fileprovider"
            android:grantUriPermissions="true"
            android:exported="false">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/filepaths" />
        </provider>
        ...
    </application>
</manifest>
```
```xml
<paths>
    <external-path path="images/" name="myimages" />
</paths>
```

### Select an image from a camera or gallery only...
```java
// Camera or Gallery only
PickImage.camera(Activity);
PickImage.gallery(Activity);
PickImage.camera(android.support.v4.app.Fragment);
PickImage.gallery(android.support.v4.app.Fragment);
```
Override OnActivityResult..
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

### Select and crop image
```java
// Camera or Gallery & Cropper
PickImage.cameraWithCrop(Activity);
PickImage.galleryWithCrop(Activity);
PickImage.cameraWithCrop(android.support.v4.app.Fragment);
PickImage.galleryWithCrop(android.support.v4.app.Fragment);
```

Override OnActivityResult..
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
    builder.aspectRatio(aspectRatio); // ex) "aspect-x : aspect-y"
    builder.outputSize(outputSize);   // ex) "width * height
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
### How to get image bytes & image file name from content uri for server uploading?
```java
String fileName = PickImage.getFileName(context, uri);
byte[] bytes = bytePickImage.getBytes(context, uri);
```




## License
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
