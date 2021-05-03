package com.hcmus.apum;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;

/**
 * Dynamic Dialog inflated by given layout id
 */
public class LayoutDialog extends Dialog {
    Context context;
    int layoutId;

    public LayoutDialog(@NonNull Context context, int layoutId) {
        super(context);
        this.context = context;
        this.layoutId = layoutId;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(layoutId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
