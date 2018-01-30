package com.jhfactory.aospimagepick;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.format.Formatter;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImagePickUtils {

    private static final String TAG = ImagePickUtils.class.getSimpleName();

    /**
     * @param context       context
     * @param fileExtension file extension
     * @return file Uri that cropped image would be stored
     */
    @Nullable
    public static Uri getImageTargetUri(Context context, String fileExtension) {
        try {
            final String extension = "." + fileExtension;
            File photoFile = createTempImageFileOnExternal(context, extension);
            return getUriForFile(context, photoFile);
//            return Uri.fromFile(photoFile);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static Uri getImageTargetUri2(Context context, String fileExtension) {
        try {
            final String extension = "." + fileExtension;
            File photoFile = createTempImageFileOnExternal(context, extension);
//            return getUriForFile(context, photoFile);
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
    public static Uri getImageTargetUri(Context context) {
        return getImageTargetUri(context, "jpg");
    }

    /**
     * @param context context
     * @param file    absolute photo path File
     * @return Uri from file
     */
    public static Uri getUriForFile(Context context, File file) {
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
    public static File createTempImageFileOnExternal(Context context, String extension) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, extension, storageDir);
    }

    /**
     * Delete image file that was saved temporary.
     *
     * @param context  context
     * @param filePath file path
     * @return If delete file success, return true. If not, return false.
     */
    @SuppressWarnings("unused")
    public static boolean deleteImageFileFromExternal(Context context, String filePath) {
        // TODO: Does file delete automatically? Not sure yet.
        return false;
    }

    /**
     * get bytes from content uri.
     *
     * @param context    context
     * @param contentUri content uri
     * @return byte array read from the inputStream.
     * @throws IOException ioexception
     */
    public static byte[] getBytes(@NonNull Context context, Uri contentUri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(contentUri, "r");
        if (parcelFileDescriptor == null) {
            return null;
        }
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        InputStream inputStream = new FileInputStream(fileDescriptor);

        byte[] bytesResult;
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        try {
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            bytesResult = byteBuffer.toByteArray();
        } finally {
            // close the stream
            try {
                byteBuffer.close();
            } catch (IOException ignored) { /* do nothing */ }
        }
        return bytesResult;
    }

    /**
     * @param context    context
     * @param contentUri file uri
     * @return get file name from uri
     */
    public static String getFileNameFromUri(@NonNull Context context, Uri contentUri) {
        if (context.getContentResolver() == null) {
            return null;
        }
        Cursor returnCursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (returnCursor == null) {
            return null;
        }
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String fileName = returnCursor.getString(nameIndex);
        returnCursor.close();
        return fileName;
    }

    /**
     * Show image meta data
     *
     * @param context    context
     * @param contentUri file uri
     */
    public static void dumpImageMetaData(@NonNull Context context, Uri contentUri) {
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.d(TAG, "[dumpImageMetaData] Display Name: " + displayName);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                String size;
                if (!cursor.isNull(sizeIndex)) {
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                String readableFileSize = Formatter.formatFileSize(context, Long.valueOf(size));
                Log.d(TAG, "[dumpImageMetaData] Size: " + readableFileSize);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
