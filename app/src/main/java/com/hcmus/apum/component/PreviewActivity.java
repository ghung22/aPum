package com.hcmus.apum.component;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hcmus.apum.DatabaseFavorites;
import com.hcmus.apum.MainActivity;
import com.hcmus.apum.PathUtils;
import com.hcmus.apum.R;
import com.hcmus.apum.adapter.PreviewAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import static com.hcmus.apum.MainActivity.COPY_CHOOSER_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.MOVE_CHOOSER_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.PREVIEW_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.mediaManager;

public class PreviewActivity extends AppCompatActivity {

    // GUI controls
    private Toolbar toolbar;
    private BottomNavigationView bottomToolbar;

    // Elements
    private ViewPager imgPreview;
    private PreviewAdapter adapter;
    private LayoutDialog dialog;

    // Data
    ArrayList<String> mediaList;
    int pos;
    double latitude, longitude;

    //DB
    private DatabaseFavorites db_fav = MainActivity.db_fav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        // Get values from bundle
        Intent mainPreview = getIntent();
        String action = mainPreview.getAction();
        Bundle bundle = null;
        if (action == null) {
            // Preview opened from aPum
            bundle = mainPreview.getExtras();
            mediaList = bundle.getStringArrayList("thumbnails");
            pos = bundle.getInt("position");

            // Set values to return
            Intent previewMain = new Intent();
            Bundle returnBundle = new Bundle();
            returnBundle.putString("caller", bundle.getString("caller"));
            previewMain.putExtras(returnBundle);
            setResult(Activity.RESULT_OK, previewMain);
        } else if (action.equals(Intent.ACTION_VIEW)) {
            // Preview opened from outside
            mediaList = new ArrayList<>();
            pos = 0;
            Uri mediaUri = mainPreview.getData();
            String temp = PathUtils.fromUri(mediaUri.getPath());
            if (Files.exists(Paths.get(temp))) {
                mediaList.add(temp);
            } else {
                adapter = new PreviewAdapter(this, mediaUri);
                mediaList.add("null");
            }
        }

        // Init preview layout
        if (adapter == null) {
            adapter = new PreviewAdapter(this, mediaList);
        }
        imgPreview = findViewById(R.id.img_preview);
        imgPreview.setAdapter(adapter);
        imgPreview.setCurrentItem(pos, true);
        imgPreview.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            // Snap to page based on how much user have scrolled
            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {}

            // Update UI according to of current image
            @Override
            public void onPageSelected(int position) {
                updateUI(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // Init actionbar buttons
        toolbar = findViewById(R.id.menu_preview);
        toolbar.inflateMenu(R.menu.menu_preview);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        String path = mediaList.get(pos);
        actionBar.setTitle(path.substring(path.lastIndexOf('/') + 1));
        actionBar.setDisplayHomeAsUpEnabled(true);
        bottomToolbar = findViewById(R.id.bottomBar_preview);
        bottomToolbar.setOnNavigationItemSelectedListener(item -> bottomToolbarAction((String) item.getTitle()));
    }

    private void updateUI(int pos) {
        // Data
        this.pos = pos;

        // Top toolbar
        ActionBar actionBar = getSupportActionBar();
        String path = mediaList.get(pos);
        actionBar.setTitle(path.substring(path.lastIndexOf('/') + 1));

        // Bottom toolbar
        Menu menu = bottomToolbar.getMenu();
        MenuItem fav = menu.findItem(R.id.action_favorite);
        if (mediaManager.isFavorite(mediaList.get(pos))) {
            fav.setIcon(R.drawable.ic_fav);
        } else {
            fav.setIcon(R.drawable.ic_fav_outline);
        }
    }

    private void initInfoDialog() {
        // INIT ELEMENTS
        dialog = new LayoutDialog(this, R.layout.layout_preview_info_dialog);
        LinearLayout info_err_row = dialog.findViewById(R.id.info_err_row),
                info_fileName_row = dialog.findViewById(R.id.info_fileName_row),
                info_fileLocation_row = dialog.findViewById(R.id.info_fileLocation_row),
                info_description_row = dialog.findViewById(R.id.info_description_row),
                info_imageSize_row = dialog.findViewById(R.id.info_imageSize_row),
                info_camera_row = dialog.findViewById(R.id.info_camera_row),
                info_map_row = dialog.findViewById(R.id.info_map_row);
        ImageView info_fileName_img = dialog.findViewById(R.id.info_fileName_img);
        TextView info_err = dialog.findViewById(R.id.info_err),
                info_fileName = dialog.findViewById(R.id.info_fileName),
                info_fileLocation = dialog.findViewById(R.id.info_fileLocation),
                info_description = dialog.findViewById(R.id.info_description),
                info_imageSize = dialog.findViewById(R.id.info_imageSize),
                info_camera = dialog.findViewById(R.id.info_camera),
                info_map = dialog.findViewById(R.id.info_map);
        MapView info_map_view = dialog.findViewById(R.id.info_map_view);
        Button info_edit_btn = dialog.findViewById(R.id.info_edit_btn),
                info_close_btn = dialog.findViewById(R.id.info_close_btn);

        // APPLY DATA
        // Get data and check if occurred errors
        HashMap<String, String> data = mediaManager.getInfo(this, mediaList.get(pos));
        if (data.containsKey("err")) {
            info_fileName_row.setVisibility(View.GONE);
            info_fileLocation_row.setVisibility(View.GONE);
            info_description_row.setVisibility(View.GONE);
            info_imageSize_row.setVisibility(View.GONE);
            info_camera_row.setVisibility(View.GONE);
            info_map_row.setVisibility(View.GONE);
            info_err.setText(data.get("err"));
            dialog.show();
            return;
        } else {
            info_err_row.setVisibility(View.GONE);
        }
        String fileName = data.get("fileName"), fileLocation = data.get("fileLocation"),
                fileSize = data.get("fileSize"), description = data.get("description"),
                imageSize = data.get("imageSize"), camera = data.get("camera"),
                artist = data.get("artist"), imageLocation = data.get("imageLocation"),
                imageLocationLat = data.get("imageLocationLat"), imageLocationLong = data.get("imageLocationLong");
        // File attribute
        info_fileName.setText(HtmlCompat.fromHtml(fileName + " <i>(" + fileSize + ")</i>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        info_fileLocation.setText(fileLocation);
        // Media attribute
        if (!TextUtils.isEmpty(description)) {
            info_description.setText(description);
        } else {
            info_description_row.setVisibility(View.GONE);
        }
        info_imageSize.setText(imageSize);
        if (!TextUtils.isEmpty(camera) && !TextUtils.isEmpty(artist)) {
            info_camera.setText(HtmlCompat.fromHtml("Taken with <b>" + camera + "</b> by <b>" + artist + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else if (!TextUtils.isEmpty(camera)) {
            info_camera.setText(HtmlCompat.fromHtml("Taken with <b>" + camera + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else if (!TextUtils.isEmpty(artist)) {
            info_camera.setText(HtmlCompat.fromHtml("Taken by <b>" + artist + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            info_camera_row.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(imageLocationLat)) {
            latitude = Double.parseDouble(imageLocationLat);
            longitude = Double.parseDouble(imageLocationLong);
            if (!TextUtils.isEmpty(imageLocation)) {
                info_map.setText(imageLocation);
            } else {
                info_map.setText(getResources().getString(R.string.info_unknown_location));
            }
            MapsInitializer.initialize(this);
            info_map_view.onCreate(dialog.onSaveInstanceState());
            info_map_view.onResume();
            info_map_view.getMapAsync(googleMap -> {
                LatLng location = new LatLng(latitude, longitude);
                Marker marker = googleMap.addMarker(new MarkerOptions().position(location));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
            });
        } else {
            info_map_row.setVisibility(View.GONE);
        }

        // INIT CONTROLS
        info_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: View edit dialog
            }
        });
        info_close_btn.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        // Fix menu color in Light mode
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            SpannableString string = new SpannableString(item.getTitle());
            int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int color = (nightMode == Configuration.UI_MODE_NIGHT_YES) ? Color.WHITE : Color.BLACK;
            string.setSpan(new ForegroundColorSpan(color), 0, string.length(), 0);
            item.setTitle(string);
        }
        return true;
    }

    // TOP TOOLBAR ACTION
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Intent previewChooser;
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_copy:
                previewChooser = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                previewChooser.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(Intent.createChooser(previewChooser, "Choose a directory"), COPY_CHOOSER_REQUEST_CODE);
                break;
            case R.id.action_move:
                previewChooser = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                previewChooser.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(Intent.createChooser(previewChooser, "Choose a directory"), MOVE_CHOOSER_REQUEST_CODE);
                break;
            case R.id.action_info:
                initInfoDialog();
                break;
            case R.id.action_wallpaper:
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setDataAndType(Uri.parse(mediaList.get(pos)), "image/*");
                intent.putExtra("mimeType", "image/*");
                this.startActivity(Intent.createChooser(intent, getResources().getString(R.string.action_wallpaper)));
                break;
            default:
                Toast.makeText(this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    // BOTTOM TOOLBAR ACTION
    private boolean bottomToolbarAction(String title) {
        Menu menu = bottomToolbar.getMenu();
        MenuItem fav = menu.findItem(R.id.action_favorite);
        if (title.equals(getResources().getString(R.string.fragment_favorite))) {
            mediaManager.addFavorites(mediaList, pos, db_fav);
            if(mediaManager.isFavorite(mediaList.get(pos))) {
                fav.setIcon(R.drawable.ic_fav);
                Toast.makeText(this, "Added to Favorite", Toast.LENGTH_LONG).show();
            } else {
                fav.setIcon(R.drawable.ic_fav_outline);
                Toast.makeText(this, "Removed from Favorite", Toast.LENGTH_LONG).show();
            }
        } else if (title.equals(getResources().getString(R.string.action_edit))) {
            Toast.makeText(this, mediaList.get(pos), Toast.LENGTH_LONG).show();
        } else if (title.equals(getResources().getString(R.string.action_share))) {
            mediaManager.share(this, mediaList.get(pos));
        } else if (title.equals(getResources().getString(R.string.action_delete))) {
            if (!mediaManager.delete(this, mediaList.get(pos))) {
                Toast.makeText(this, R.string.err_generic, Toast.LENGTH_SHORT).show();
            } else {
                int curItem = imgPreview.getCurrentItem();
                if (curItem > 0) {
                    imgPreview.setCurrentItem(imgPreview.getCurrentItem() - 1, true);
                } else if (mediaList.size() > 1) {
                    imgPreview.setCurrentItem(imgPreview.getCurrentItem() + 1, true);
                } else {
                    onBackPressed();
                }
            }
        } else {
            Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void callBroadCast() {
        MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.e("ExternalStorage", "Scanned " + path + ":");
                Log.e("ExternalStorage", "-> uri=" + uri);
            }
        });
    }
    @Override

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == COPY_CHOOSER_REQUEST_CODE || requestCode == MOVE_CHOOSER_REQUEST_CODE) {
            if (data != null) {
                Uri dest = data.getData();
                String destination = PathUtils.fromUri(dest.getPath());
                try {
                    if (requestCode == COPY_CHOOSER_REQUEST_CODE) {
                        if (mediaManager.copy(this, mediaList.get(pos), destination)) {
                            Toast.makeText(this, "Copied.", Toast.LENGTH_SHORT).show();
                        } else {
                            throw new Throwable();
                        }
                    } else if (requestCode == MOVE_CHOOSER_REQUEST_CODE) {
                        if (mediaManager.move(this, mediaList.get(pos), destination)) {
                            Toast.makeText(this, "Moved.", Toast.LENGTH_SHORT).show();
                        } else {
                            throw new Throwable();
                        }
                    }
                } catch (Throwable throwable) {
                    Toast.makeText(this, R.string.err_generic, Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity(PREVIEW_REQUEST_CODE);
    }
}