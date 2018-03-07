package com.jhfactory.aospimagepick.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


final class HelperUtils {

    private static final String TAG = HelperUtils.class.getSimpleName();

    /**
     * @param context       context
     * @param fileExtension file extension
     * @return file Uri that cropped image would be stored
     */
    @Nullable
    static Uri getImageTargetUri(Context context, String fileExtension) {
        try {
            final String extension = "." + fileExtension;
            File photoFile = createTempImageFileOnExternal(context, extension);
            return getUriForFile(context, photoFile);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    static Uri getImageTargetFileUri(Context context, String fileExtension) {
        try {
            final String extension = "." + fileExtension;
            File photoFile = createTempImageFileOnExternal(context, extension);
            return Uri.fromFile(photoFile);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * @param context context
     * @return file Uri that cropped image would be stored
     */
    @Nullable
    static Uri getImageTargetUri(Context context) {
        return getImageTargetUri(context, Bitmap.CompressFormat.JPEG.name());
    }

    /**
     * @param context context
     * @param file    absolute photo path File
     * @return Uri from file
     */
    static Uri getUriForFile(Context context, File file) {
        String authority = context.getPackageName() + ".fileprovider";
        Log.d(TAG, "authority: " + authority);
        return FileProvider.getUriForFile(context, authority, file);
    }

    /**
     * Create image file that would be saved temporary.
     *
     * @param context   context
     * @param extension file extension name
     * @return File that has been created.
     * @throws IOException Exception
     */
    static File createTempImageFileOnExternal(Context context, String extension) throws IOException {
        final String pattern = "yyyyMMdd_HHmmss";
        String timeStamp = new SimpleDateFormat(pattern, Locale.getDefault()).format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, extension, storageDir);
    }

    static File createTempImageFileOnExternal(Context context) throws IOException {
        return createTempImageFileOnExternal(context, Bitmap.CompressFormat.JPEG.name());
    }

    /**
     * Delete image file that was saved temporary.
     *
     * @param context  context
     * @param filePath file path
     * @return If delete file success, return true. If not, return false.
     */
    @SuppressWarnings("unused")
    static boolean deleteImageFileFromExternal(Context context, String filePath) {
        // TODO: Does file delete automatically? Not sure yet.
        return false;
    }
}
