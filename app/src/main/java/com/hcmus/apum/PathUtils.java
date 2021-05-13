package com.hcmus.apum;

import android.util.Log;

/*
* SOME URI FORMAT:
* /tree/primary:Music
* /tree/OFFC-0209:Test
* /tree/downloads
* /tree/raw:/storage/emulated/0/Downloads/Photos
* /document/43
* /document/image:254
* /document/primary:Music/file.png
* /document/OFFC-0209:Test/file.png
*/
public final class PathUtils {
    private final static String TAG = "PATH_UTILS";
    public static String fromUri(String uri) {
        Log.i(TAG, "Received path: " + uri);
        String path = uri.substring(uri.indexOf('/', 1) + 1);
        if (uri.indexOf("/tree/") == 0) {
            switch (path.substring(0, path.lastIndexOf(':'))) {
                case "primary":
                    path = "/storage/emulated/0/" + path.substring(path.lastIndexOf(':') + 1);
                    break;
                case "downloads":
                    path = "/storage/emulated/0/Download/";
                    break;
                case "raw":
                    path = path.substring(path.lastIndexOf(':') + 1);
                    break;
                default:
                    // SD Card
                    path = "/storage/" + path.replace(':', '/');
                    break;
            }
        } else if (uri.indexOf("/document/") == 0) {
            switch (path.substring(0, path.lastIndexOf(':'))) {
                case "primary":
                    path = "/storage/emulated/0/" + path.substring(path.lastIndexOf(':') + 1);
                    break;
                case "raw":
                    path = path.substring(path.lastIndexOf(':') + 1);
                    break;
                case "image":
                    Log.e(TAG, "Unsupported uri format: " + uri);
                default:
                    // SD Card
                    Log.w(TAG, "Possible unsupported uri format: " + uri);
                    path = "/storage/" + path.replace(':', '/');
                    break;
            }
        }
        Log.i(TAG, "Processed path: " + path);
        return path;
    }
}