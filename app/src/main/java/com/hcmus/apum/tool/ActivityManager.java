package com.hcmus.apum.tool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.common.util.Strings;
import com.hcmus.apum.AboutActivity;
import com.hcmus.apum.MainActivity;
import com.hcmus.apum.component.ContentActivity;
import com.hcmus.apum.component.PreviewActivity;
import com.hcmus.apum.component.SearchActivity;
import com.hcmus.apum.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static com.hcmus.apum.MainActivity.*;

public class ActivityManager {
    private final AppCompatActivity activity;
    private final BaseFragment fragment;
    private final Context context;
    private final String caller;

    private final HashMap<Integer, ActivityResultLauncher<Intent>> launchers;
    private final ActivityResultContract<Intent, ActivityResult> activityResultContract;
    private final ActivityResultContract<Void, Bitmap> cameraResultContract;
    private final ActivityResultCallback<ActivityResult> activityResultCallback;
    private final ActivityResultCallback<Bitmap> cameraResultCallback;

    private static final String TAG = "ACTIVITY_MANAGER";

    // <editor-fold desc="CONSTRUCTOR GROUP">
    public ActivityManager(@Nullable AppCompatActivity activity, @Nullable BaseFragment fragment, @NonNull String caller) {
        this.activity = activity;
        this.fragment = fragment;
        this.caller = caller;
        launchers = new HashMap<>();

        // Determine if this context is an activity or a fragment
        if ((activity != null && fragment != null) || (activity == null && fragment == null)) {
            context = null;
            activityResultContract = null;
            cameraResultContract = null;
            activityResultCallback = null;
            cameraResultCallback = null;
            Log.w(TAG, "ActivityManager: No clear context, activity and fragment cannot be both null/non-null");
            return;
        } else if (activity != null) {
            context = activity;
        } else {
            context = fragment.requireContext();
        }

        // Create template items
        activityResultContract = new ActivityResultContracts.StartActivityForResult();
        activityResultCallback = result -> {
            Intent data = result.getData();

            // Check for which activity returned to MainActivity
            if (data != null) {
                if (data.hasExtra("caller")) {
                    switchFragmentRequest();
                    return;
                }
            }
            switchFragmentRequest();
        };
        cameraResultContract = new ActivityResultContracts.TakePicturePreview();
        cameraResultCallback = result -> {
            if (result != null) {
                mediaManager.saveCaptured(context, result);
            }
            switchFragmentRequest();
        };

        // Start registering
        if (caller.equals(fragNames.get(0))) {
            registerForOverview();
        } else if (caller.equals(fragNames.get(1))) {
            registerForAlbums();
        } else if (caller.equals(fragNames.get(2))) {
            registerForFaces();
        } else if (caller.equals(fragNames.get(3))) {
            registerForFavorite();
        } else if (caller.equals("search")) {
            registerForSearch();
        } else if (caller.equals("content")) {
            registerForContent();
        } else {
            Log.w(TAG, "ActivityManager: Unknown caller " + caller);
        }
    }

    public ActivityManager(@NonNull AppCompatActivity activity, @NonNull String caller) {
        this(activity, null, caller);
    }

    public ActivityManager(@NonNull BaseFragment fragment, @NonNull String caller) {
        this(null, fragment, caller);
    }
    // </editor-fold>

    // <editor-fold desc="REGISTER FOR GROUP">
    private void registerForOverview() {
        registerAbout();
        registerCamera();
        registerPreview();
        registerSearch();
    }

    private void registerForAlbums() {
        registerAbout();
        registerContent();
        registerSearch();
    }

    private void registerForFaces() {
        registerAbout();
        registerContent();
        registerSearch();
    }

    private void registerForFavorite() {
        registerAbout();
        registerPreview();
        registerSearch();
    }

    private void registerForSearch() {
        registerContent();
        registerPreview();
    }

    private void registerForContent() {
        registerPreview();
    }
    // </editor-fold>

    // <editor-fold desc="REGISTER GROUP">
    private void registerAbout() {
        if (fragment != null) {
            launchers.put(ABOUT_REQUEST_CODE,
                    fragment.registerForActivityResult(activityResultContract, activityResultCallback));
        } else {
            launchers.put(ABOUT_REQUEST_CODE,
                    activity.registerForActivityResult(activityResultContract, activityResultCallback));
        }
    }

    private void registerCamera() {
        if (fragment != null) {
            fragment.registerForActivityResult(cameraResultContract, cameraResultCallback);
        } else {
            activity.registerForActivityResult(cameraResultContract, cameraResultCallback);
        }
        launchers.put(CAMERA_REQUEST_CODE, null);
    }

    private void registerContent() {
        if (fragment != null) {
            launchers.put(CONTENT_REQUEST_CODE,
                    fragment.registerForActivityResult(activityResultContract, activityResultCallback));
        } else {
            launchers.put(CONTENT_REQUEST_CODE,
                    activity.registerForActivityResult(activityResultContract, activityResultCallback));
        }
    }

    private void registerPreview() {
        if (fragment != null) {
            launchers.put(PREVIEW_REQUEST_CODE,
                    fragment.registerForActivityResult(activityResultContract, activityResultCallback));
        } else {
            launchers.put(PREVIEW_REQUEST_CODE,
                    activity.registerForActivityResult(activityResultContract, activityResultCallback));
        }
    }

    private void registerSearch() {
        if (fragment != null) {
            launchers.put(SEARCH_REQUEST_CODE,
                    fragment.registerForActivityResult(activityResultContract, activityResultCallback));
        } else {
            launchers.put(SEARCH_REQUEST_CODE,
                    activity.registerForActivityResult(activityResultContract, activityResultCallback));
        }
    }
    // </editor-fold>

    // <editor-fold desc="SHOW ACTIVITY GROUP">
    public void showAbout() {
        Intent intent = new Intent(context, AboutActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", caller);
        intent.putExtras(bundle);
        intent.setFlags(0);
        startActivity(intent, CONTENT_REQUEST_CODE);
    }

    public void showCamera() {
        startActivity(null, CAMERA_REQUEST_CODE);
    }

    public void showContent(String host, ArrayList<String> container) {
        Intent intent = new Intent(context, ContentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", caller);
        bundle.putString("host", host);
        bundle.putStringArrayList("container", container);
        intent.putExtras(bundle);
        startActivity(intent, CONTENT_REQUEST_CODE);
    }

    public void showPreview(ArrayList<String> mediaList, int pos) {
        Intent intent = new Intent(context, PreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", caller);
        bundle.putStringArrayList("mediaList", mediaList);
        bundle.putInt("position", pos);
        intent.putExtras(bundle);
        startActivity(intent, PREVIEW_REQUEST_CODE);
    }

    public void showSearch(String query, String searchScope, ArrayList<String> results) {
        Intent intent = new Intent(context, SearchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", caller);
        bundle.putString("query", query);
        bundle.putStringArrayList("results", results);
        bundle.putString("scope", searchScope);
        intent.putExtras(bundle);
        startActivity(intent, SEARCH_REQUEST_CODE);
    }
    // </editor-fold>

    // <editor-fold desc="RESULT GROUP"
    public void setResult(String caller) {
        // Check for context
        if (context == null) {
            Log.w(TAG, "setResult: context is null");
            return;
        }

        // Check for caller and create new Intent
        if (Strings.emptyToNull(caller) == null) {
            caller = fragNames.get(0);
        }
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("caller", caller);
        intent.putExtras(bundle);

        // Put Intent into context's result
        ((AppCompatActivity) context).setResult(Activity.RESULT_OK, intent);
    }
    // </editor-fold>

    // <editor-fold desc="PRIVATE HANDLE GROUP">
    private void startActivity(Intent intent, int requestCode) {
        Objects.requireNonNull(launchers.get(requestCode)).launch(intent);
    }

    private void switchFragmentRequest() {
        if (fragNames.contains(caller)) {
            Bundle bundle = new Bundle();
            bundle.putString("caller", caller);
            bundle.putString("action", "switch");
            ((MainActivity) context).fragToMain(caller, bundle);
        }
    }
    // </editor-fold>
}