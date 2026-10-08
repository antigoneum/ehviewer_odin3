package com.hippo.ehviewer.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.hippo.ehviewer.R;

/** One focus target per preview, retaining the image's open and retry actions. */
public class GalleryPreviewView extends LinearLayout {
    public GalleryPreviewView(Context context) {
        super(context);
    }

    public GalleryPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GalleryPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View image = findViewById(R.id.image);
        setOnClickListener(v -> image.performClick());
        setOnLongClickListener(v -> image.performLongClick());
    }
}
