package org.potados.study_in_java;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UriHelper {
    private static final String TAG = "UriHelper";

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        final boolean isOreo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

        Log.d(TAG, "uri: " + uri.toString());

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }

            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                // Okay, we are going to get a real path of the downloaded file.
                // Beware the 'GOD DAMN' signal.

                Log.d(TAG, "this uri is download document.");

                // GOD DAMN 1:
                // Fucking Android 8 sometimes gives us non-number string when called
                // DocumentsContract.getDocumentId.
                // In this case the id starts with "raw:".
                // We need to handle it.
                final String id = DocumentsContract.getDocumentId(uri);

                if (id.isEmpty()) {
                    return null;
                }

                Log.d(TAG, "id of this uri: " + id);

                // This is the case Android 8 returns id starting with 'raw:'.
                if (id.startsWith("raw:")) {
                    // Android 8(Oreo)
                    Log.d(TAG, "It's oreo, the path is given as id, returning " + id.replaceFirst("raw:", "") + ".");

                    return id.replaceFirst("raw:", "");
                }

                // This is the other case where Android 8 returns correct integer id.
                if (isOreo) {
                    // WOW GOD DAMN 2:
                    // When Android 8(Oreo) gives us an id consisting of pure integer,
                    // we CANNOT use 'content://downloads/public_downloads' uri!!
                    // But at least we can open the input stream of the file the uri is pointing.
                    // So the we are going to copy the file to the cache directory.
                    Log.d(TAG, "It's oreo, integer id is given. we need to copy the file to the cache directory. fuck.");

                    return copyToCacheDir(context, uri);
                }

                // This is the case where Android under 8 returns correct integer id.
                // Only few actions are required.
                try {
                    String[] contentUriPrefixesToTry = new String[]{
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads",
                            "content://downloads/all_downloads"
                    };

                    for (String prefix : contentUriPrefixesToTry) {
                        final Uri contentUri = ContentUris.withAppendedId(Uri.parse(prefix), 999);
                        String path = getDataColumn(context, contentUri, null, null);

                        if (path != null) {
                            return path;
                        }
                    }

                    return null;
                } catch (Exception e) {
                    // id does not start with "raw:", but not parsable!
                    e.printStackTrace();
                    return null;
                }
            }

            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }

        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }

        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static String copyToCacheDir(Context context, Uri uri) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            File cacheFile = new File(context.getCacheDir().getAbsolutePath() + "/downloaded" + getFileId(uri));
            writeFile(is, cacheFile);

            return cacheFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            return null;
        }
    }

    private static Long getFileId(Uri uri) {
        long result = 0;
        try {
            String path = uri.getPath();
            Log.d(TAG, "Path: " + path);
            String[] paths = path.split("/");
            if (paths.length >= 3) {
                result = Long.parseLong(paths[2]);
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            result = Long.parseLong(new File(uri.getPath()).getName());
        }
        return result;
    }

    private static void writeFile(InputStream in, File file) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
