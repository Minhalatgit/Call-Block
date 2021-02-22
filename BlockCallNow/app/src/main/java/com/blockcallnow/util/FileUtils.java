package com.blockcallnow.util;
/*
  Created by Santosh Thorani
  Android Developer
  santosh.thorani@hotmail.com
  on 2/09/2018.
 */

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Peli
 * @author paulburke (ipaulpro)
 * @version 2013-12-11
 */
@SuppressWarnings("HardCodedStringLiteral")
public class FileUtils {
// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    public FileUtils() {
//    } //private constructor to enforce Singleton pattern
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)

    /**
     * TAG for log messages.
     */
    private static final String TAG = "FileUtils";
    private static final boolean DEBUG = false; // Set to true to enable logging

    // --Commented out by Inspection (18/11/2018 11:17 PM):public static final String MIME_TYPE_AUDIO = "audio/*";
    // --Commented out by Inspection (18/11/2018 11:17 PM):public static final String MIME_TYPE_TEXT = "text/*";
    private static final String MIME_TYPE_IMAGE = "image/*";
    // --Commented out by Inspection (18/11/2018 11:17 PM):public static final String MIME_TYPE_VIDEO = "video/*";
    // --Commented out by Inspection (18/11/2018 11:17 PM):public static final String MIME_TYPE_APP = "application/*";

    private static final String HIDDEN_PREFIX = ".";


// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    public static File createImageFile(Context context) {
//        return createFile(context, ".jpg");
//    }
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)


    public static File createMp4VideoFile(Context context) {
        return createFile(context, "", ".mp4");
    }

    public static File createMp4VideoFile(Context context, String fileName) {
        return createFile(context, fileName, ".mp4");
    }

    public static File createWAVFile(Context context, String fileName) {
        return createFile(context, fileName, ".wav");
    }

    private static File createFile(Context context, String fileName, String suffix) {
        LogUtil.Companion.e(TAG, context == null ? "null" : "not null");

        File file = null;
        try {
            // Determine current time stamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_hhmmss", Locale.CANADA)
                    .format(new Date());
            // Setup prefix, suffix and directory
            String prefix = (TextUtils.isEmpty(fileName)) ? "" : fileName + "_" + timeStamp + "_";
//        File directory = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES + "/example/");

            // Create temporary file
            file = File.createTempFile(prefix, suffix, getDirectory(context));
        } catch (Exception e) {
            LogUtil.Companion.e(TAG, "can't access storage " + e.toString());
        }

        // Return the file
        return file;
    }

    public static File getDirectory(Context context) throws Exception {

        String appName = "BlockCallsNow";
        File directory = Environment.getExternalStoragePublicDirectory("/" + appName + "/");
        // Create directory if it doesn't exist
        if (!directory.exists()) {
            directory.mkdir();
            // Create a .nomedia file so Android doesn't include it in the photo app
//            File nomedia = new File(directory, ".nomedia");
//            nomedia.createNewFile();
        }
        LogUtil.Companion.e(TAG, "file " + directory == null ? "null" : directory.toString());
        return directory;
    }

// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    public static String getStorageDir(Context context) {
//        try {
//            return getDirectory(context).getPath();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//
//    }
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)

// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    public static int[] getVideoDimension(String path) {
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(path);
//        int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
//        int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
//        retriever.release();
//        return new int[]{width, height};
//    }
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    private static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    /**
     * @return Whether the URI is a local one.
     */
    private static boolean isLocal(String url) {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://");
    }

    /**
     * @return True if Uri is a MediaStore Uri.
     * @author paulburke
     */
    private static boolean isMediaUri(Uri uri) {
        return "media".equalsIgnoreCase(uri.getAuthority());
    }

// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    /**
//     * Returns the path only (without file name).
//     *
//     * @param file
//     * @return
//     */
//    public static File getPathWithoutFilename(File file) {
//        if (file != null) {
//            if (file.isDirectory()) {
//                // no file to be split off. Return everything
//                return file;
//            } else {
//                String filename = file.getName();
//                String filepath = file.getAbsolutePath();
//
//                // Construct path without file name.
//                String pathwithoutname = filepath.substring(0,
//                        filepath.length() - filename.length());
//                if (pathwithoutname.endsWith("/")) {
//                    pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
//                }
//                return new File(pathwithoutname);
//            }
//        }
//        return null;
//    }
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)

// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    /**
//     * @return The MIME type for the given file.
//     */
//    private static String getMimeType(File file) {
//
//        String extension = getExtension(file.getName());
//
//        if (extension.length() > 0)
//            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
//
//        return "application/octet-stream";
//    }
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)

    /**
     * @param uri The Uri to check.
     *            .
     * @author paulburke
     */
    private static boolean isLocalStorageDocument(Uri uri) {
//        return LocalStorageProvider.AUTHORITY.equals(uri.getAuthority());
//        return LocalStorageProvider.AUTHORITY.equals(uri.getAuthority());
        return true;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
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
                if (DEBUG)
                    DatabaseUtils.dumpCursor(cursor);

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br>
     * <br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     * @see #isLocal(String)
     */
    public static String getPath(final Context context, final Uri uri) {

        if (DEBUG)
            Log.d(TAG + " File -",
                    "Authority: " + uri.getAuthority() +
                            ", Fragment: " + uri.getFragment() +
                            ", Port: " + uri.getPort() +
                            ", Query: " + uri.getQuery() +
                            ", Scheme: " + uri.getScheme() +
                            ", Host: " + uri.getHost() +
                            ", Segments: " + uri.getPathSegments().toString()
            );

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // LocalStorageProvider
            if (isLocalStorageDocument(uri)) {
                // The path is the id
                return DocumentsContract.getDocumentId(uri);
            }
            // ExternalStorageProvider
            else if (isExternalStorageDocument(uri)) {
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

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                switch (type) {
                    case "image":
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "video":
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "audio":
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        break;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
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


// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    /**
//     * Convert Uri into File, if possible.
//     *
//     * @return file A local file that the Uri was pointing to, or null if the
//     * Uri is unsupported or pointed to a remote resource.
//     * @author paulburke
//     * @see #getPath(Context, Uri)
//     */
//    public static File getFile(Context context, Uri uri) {
//        if (uri != null) {
//            String path = getPath(context, uri);
//            if (path != null && isLocal(path)) {
//                return new File(path);
//            }
//        }
//        return null;
//    }
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)

// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    /**
//     * Get the file size in a human-readable string.
//     *
//     * @param size
//     * @return
//     * @author paulburke
//     */
//    public static String getReadableFileSize(int size) {
//        final int BYTES_IN_KILOBYTES = 1024;
//        final DecimalFormat dec = new DecimalFormat("###.#");
//        final String KILOBYTES = " KB";
//        final String MEGABYTES = " MB";
//        final String GIGABYTES = " GB";
//        float fileSize = 0;
//        String suffix = KILOBYTES;
//
//        if (size > BYTES_IN_KILOBYTES) {
//            fileSize = size / BYTES_IN_KILOBYTES;
//            if (fileSize > BYTES_IN_KILOBYTES) {
//                fileSize = fileSize / BYTES_IN_KILOBYTES;
//                if (fileSize > BYTES_IN_KILOBYTES) {
//                    fileSize = fileSize / BYTES_IN_KILOBYTES;
//                    suffix = GIGABYTES;
//                } else {
//                    suffix = MEGABYTES;
//                }
//            }
//        }
//        return String.valueOf(dec.format(fileSize) + suffix);
//    }
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)

//

//
//    /**
//     * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
//     * should not be called on the UI thread.
//     *
//     * @param context
//     * @param uri
//     * @return
//     * @author paulburke
//     */
//    public static Bitmap getThumbnail(Context context, Uri uri) {
//        return getThumbnail(context, uri, getMimeType(context, uri));
//    }

// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    /**
//     * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
//     * should not be called on the UI thread.
//     *
//     * @param context
//     * @param uri
//     * @param mimeType
//     * @return
//     * @author paulburke
//     */
//    private static Bitmap getThumbnail(Context context, Uri uri, String mimeType) {
//        if (DEBUG)
//            Log.d(TAG, "Attempting to get thumbnail");
//
//        if (!isMediaUri(uri)) {
//            LogUtil.e(TAG, "You can only retrieve thumbnails for images and videos.");
//            return null;
//        }
//
//        Bitmap bm = null;
//        if (uri != null) {
//            final ContentResolver resolver = context.getContentResolver();
//            Cursor cursor = null;
//            try {
//                cursor = resolver.query(uri, null, null, null, null);
//                if (cursor.moveToFirst()) {
//                    final int id = cursor.getInt(0);
//                    if (DEBUG)
//                        Log.d(TAG, "Got thumb ID: " + id);
//
//                    if (mimeType.contains("video")) {
//                        bm = MediaStore.Video.Thumbnails.getThumbnail(
//                                resolver,
//                                id,
//                                MediaStore.Video.Thumbnails.MINI_KIND,
//                                null);
//                    } else if (mimeType.contains(FileUtils.MIME_TYPE_IMAGE)) {
//                        bm = MediaStore.Images.Thumbnails.getThumbnail(
//                                resolver,
//                                id,
//                                MediaStore.Images.Thumbnails.MINI_KIND,
//                                null);
//                    }
//                }
//            } catch (Exception e) {
//                if (DEBUG)
//                    LogUtil.e(TAG, "getThumbnail " + e);
//            } finally {
//                if (cursor != null)
//                    cursor.close();
//            }
//        }
//        return bm;
//    }
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)

// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    /**
//     * File and folder comparator. TODO Expose sorting option method
//     *
//     * @author paulburke
//     */
//    public static Comparator<File> sComparator = (f1, f2) -> {
//        // Sort alphabetically by lower case, which is much cleaner
//        return f1.getName().toLowerCase().compareTo(
//                f2.getName().toLowerCase());
//    };
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)

// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    /**
//     * File (not directories) filter.
//     *
//     * @author paulburke
//     */
//    public static FileFilter sFileFilter = file -> {
//        final String fileName = file.getName();
//        // Return files only (not directories) and skip hidden files
//        return file.isFile() && !fileName.startsWith(HIDDEN_PREFIX);
//    };
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)

// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    /**
//     * Folder (directories) filter.
//     *
//     * @author paulburke
//     */
//    public static FileFilter sDirFilter = file -> {
//        final String fileName = file.getName();
//        // Return directories only and skip hidden directories
//        return file.isDirectory() && !fileName.startsWith(HIDDEN_PREFIX);
//    };
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)

// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    /**
//     * Get the Intent for selecting content to be used in an Intent Chooser.
//     *
//     * @return The intent for opening a file with Intent.createChooser()
//     * @author paulburke
//     */
//    public static Intent createGetContentIntent() {
//        // Implicitly allow the user to select a particular kind of data
//        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        // The MIME data type filter
//        intent.setType("*/*");
//        // Only return URIs that can be opened with ContentResolver
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        return intent;
//    }
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)

// --Commented out by Inspection START (18/11/2018 11:17 PM):
//    public static String getRealPathFromURI(Uri uri, Context ctx) {
//        Cursor cursor = ctx.getContentResolver().query(uri, null, null, null, null);
//        cursor.moveToFirst();
//        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
//        return cursor.getString(idx);
//    }
// --Commented out by Inspection STOP (18/11/2018 11:17 PM)
}
