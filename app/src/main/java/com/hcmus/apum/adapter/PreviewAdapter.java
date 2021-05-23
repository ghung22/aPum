package com.hcmus.apum.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.hcmus.apum.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.debugEnabled;

public class PreviewAdapter extends PagerAdapter {
    private final Context context;
    private final LayoutInflater inflater;
    private final ArrayList<String> mediaList;
    private final Uri mediaUri;

    public PreviewAdapter(Context context, ArrayList<String> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
        this.mediaUri = null;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public PreviewAdapter(Context context, Uri mediaUri) {
        this.context = context;
        this.mediaList = new ArrayList<>();
        this.mediaUri = mediaUri;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mediaList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int pos) {
        View view = inflater.inflate(R.layout.layout_visual_viewpager, null);
        ImageView img = view.findViewById(R.id.imgViewPager);
        Picasso picasso = Picasso.get();
        picasso.setLoggingEnabled(debugEnabled);
        RequestCreator requestCreator;
        if (mediaUri == null) {
            requestCreator = picasso.load(new File(mediaList.get(pos)));
        } else {
            requestCreator = picasso.load(mediaUri);
        }
        requestCreator
                .fit()
                .centerInside()
                .placeholder(R.drawable.ic_image)
                .into(img);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int pos, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }

    public void add(String media) {
        mediaList.add(media);
        notifyDataSetChanged();
    }
}