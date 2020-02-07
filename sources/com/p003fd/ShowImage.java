package com.p003fd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import com.fotodekho.adapter.GridViewAdapter;
import com.fotodekho.util.SampleScrollListener;
import com.fotodekho.util.Utils;
import java.io.UnsupportedEncodingException;

/* renamed from: com.fd.ShowImage */
public class ShowImage extends Activity implements OnClickListener {
    private AlphaAnimation buttonClick = new AlphaAnimation(1.0f, 0.5f);
    Context context;
    String[] imageUrls;
    private Button logoutbutton;

    /* access modifiers changed from: protected */
    @SuppressLint({"NewApi"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0073R.layout.activity_show_image);
        if (VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new Builder().permitAll().build());
        }
        this.context = getApplicationContext();
        Log.i("ShowImage", "Utils.albumPathsMap ==>>> " + Utils.albumPathsMap);
        String text = null;
        try {
            text = Utils.GetText((String) Utils.albumPathsMap.get(Utils.mobile_image_path));
            Log.i("ShowImage", "text ==> " + text);
        } catch (UnsupportedEncodingException e) {
            Log.e("ShowImage", "exception occurred ..  " + e.getMessage());
            e.printStackTrace();
        }
        String[] tempImageUrls = text.split("\n");
        this.imageUrls = new String[(tempImageUrls.length - 1)];
        int i = 1;
        int j = 0;
        while (i < tempImageUrls.length) {
            this.imageUrls[j] = "http://mb.fotodekho.com/" + tempImageUrls[i];
            Log.i("ShowImage", "imageUrls[j] => " + this.imageUrls[j]);
            i++;
            j++;
        }
        final String[] allImageUrls = this.imageUrls;
        this.logoutbutton = (Button) findViewById(C0073R.C0075id.logoutbutton);
        this.logoutbutton.setOnClickListener(this);
        GridView gv = (GridView) findViewById(C0073R.C0075id.grid);
        gv.setAdapter(new GridViewAdapter(this, this.imageUrls));
        gv.setOnScrollListener(new SampleScrollListener(this));
        gv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                Log.d("ShowImage", "clicked on image..");
                Intent i1 = new Intent(ShowImage.this.getApplicationContext(), SlideShowActivity.class);
                i1.putExtra("allImageUrls", allImageUrls);
                i1.putExtra("position", position);
                ShowImage.this.startActivity(i1);
            }
        });
    }

    public void onClick(View v) {
        v.setAnimation(this.buttonClick);
        switch (v.getId()) {
            case C0073R.C0075id.logoutbutton /*2131427329*/:
                Log.d("ShowImageActivity", "logoutbutton clicked..");
                Intent intent2 = new Intent(this.context, LoginActivity.class);
                intent2.addFlags(268435456);
                intent2.addFlags(32768);
                this.context.startActivity(intent2);
                return;
            default:
                return;
        }
    }
}
