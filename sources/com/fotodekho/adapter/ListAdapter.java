package com.fotodekho.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.p003fd.C0073R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListAdapter extends BaseAdapter {
    Context context;
    int[] imageId;
    String[] result;
    final List<String> urls = new ArrayList();

    public class Holder {
        ImageView img;

        /* renamed from: tv */
        TextView f2tv;

        public Holder() {
        }
    }

    static class ViewHolder {
        ImageView image;
        TextView text;

        ViewHolder() {
        }
    }

    public ListAdapter(Context context2, String[] urlString) {
        this.context = context2;
        Collections.addAll(this.urls, urlString);
        for (int i = 0; i < this.urls.size(); i++) {
            if (!((String) this.urls.get(i)).contains(".")) {
                this.urls.remove(i);
            }
        }
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

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(C0073R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(C0073R.C0075id.imageView111);
            holder.text = (TextView) convertView.findViewById(C0073R.C0075id.textView111);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String url = getItem(position);
        holder.text.setText(url.substring(url.lastIndexOf("/") + 1).replace(".png", "").replace(".jpg", ""));
        Picasso.with(this.context).load(url).placeholder((int) C0073R.C0074drawable.placeholder).error((int) C0073R.C0074drawable.error).into(holder.image);
        return convertView;
    }
}
