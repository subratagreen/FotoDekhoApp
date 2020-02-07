package com.fotodekho.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.p003fd.C0073R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GridViewAdapter extends BaseAdapter {
    final Context context;
    final List<String> urls = new ArrayList();

    public GridViewAdapter(Context context2, String[] urlString) {
        this.context = context2;
        Collections.addAll(this.urls, urlString);
        for (int i = 0; i < this.urls.size(); i++) {
            if (!((String) this.urls.get(i)).contains(".")) {
                this.urls.remove(i);
            }
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(this.context);
            view.setScaleType(ScaleType.CENTER_CROP);
        }
        Picasso.with(this.context).load(getItem(position)).placeholder((int) C0073R.C0074drawable.placeholder).error((int) C0073R.C0074drawable.error).fit().into((ImageView) view);
        return view;
    }

    public int getCount() {
        return this.urls.size();
    }

    public String getItem(int position) {
        return (String) this.urls.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }
}
