package com.fotodekho.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.p000v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.p003fd.C0073R;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class CustomPagerAdapter extends PagerAdapter {
    String[] allUrls = null;
    public boolean firstTime = true;
    Context mContext;
    LayoutInflater mLayoutInflater;
    int position = 0;

    public CustomPagerAdapter(Context context, int position2) {
        this.mContext = context;
        this.mLayoutInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.position = position2;
    }

    public void setImages(String[] allUrls2) {
        this.allUrls = allUrls2;
    }

    public int getCount() {
        return this.allUrls.length;
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    public Object instantiateItem(ViewGroup container, int position2) {
        InputStream content;
        View itemView = this.mLayoutInflater.inflate(C0073R.layout.pager_item, container, false);
        ImageView imageView = (ImageView) itemView.findViewById(C0073R.C0075id.imageView);
        Bitmap bitmap = null;
        int pos = this.position + position2;
        if (pos >= this.allUrls.length - 1) {
            pos = this.allUrls.length - 1;
        }
        try {
            if (this.firstTime) {
                content = (InputStream) new URL(this.allUrls[position2]).getContent();
            } else {
                content = (InputStream) new URL(this.allUrls[pos]).getContent();
            }
            bitmap = BitmapFactory.decodeStream(content);
        } catch (MalformedURLException e) {
            Log.e("CustomPageAdapter", "exception occurred ..  " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e2) {
            Log.e("CustomPageAdapter", "exception occurred ..  " + e2.getMessage());
            e2.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
        container.addView(itemView);
        return itemView;
    }

    public void destroyItem(ViewGroup container, int position2, Object object) {
        container.removeView((LinearLayout) object);
    }
}
