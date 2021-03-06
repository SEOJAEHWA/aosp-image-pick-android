package com.jhfactory.aospimagepick;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ImagePickUtils {

    private static final String TAG = ImagePickUtils.class.getSimpleName();

    /**
     * get bytes from content uri.
     *
     * @param context    context
     * @param contentUri content uri
     * @return byte array read from the inputStream.
     * @throws IOException ioexception
     */
    static byte[] getBytes(@NonNull Context context, Uri contentUri) throws IOException {
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
    static String getFileNameFromUri(@NonNull Context context, Uri contentUri) {
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
     static void dumpImageMetaData(@NonNull Context context, Uri contentUri) {
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
