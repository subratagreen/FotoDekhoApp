package com.fotodekho.util;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import com.squareup.picasso.Picasso;

public class SampleScrollListener implements OnScrollListener {
    private final Context context;

    public SampleScrollListener(Context context2) {
        this.context = context2;
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Picasso picasso = Picasso.with(this.context);
        if (scrollState == 0 || scrollState == 1) {
            picasso.resumeTag(this.context);
        } else {
            picasso.pauseTag(this.context);
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }
}
