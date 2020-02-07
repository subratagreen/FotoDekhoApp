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
import android.widget.ListView;
import com.fotodekho.adapter.ListAdapter;
import com.fotodekho.util.SampleScrollListener;
import com.fotodekho.util.Utils;
import java.io.UnsupportedEncodingException;

/* renamed from: com.fd.ShowVideoImagesActivity */
public class ShowVideoImagesActivity extends Activity implements OnClickListener {
    private AlphaAnimation buttonClick = new AlphaAnimation(1.0f, 0.5f);
    Context context;
    String[] imageUrls;
    private Button logoutbuttonVideoList;

    /* access modifiers changed from: protected */
    @SuppressLint({"NewApi"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0073R.layout.activity_show_video_images);
        this.context = getApplicationContext();
        if (VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new Builder().permitAll().build());
        }
        this.logoutbuttonVideoList = (Button) findViewById(C0073R.C0075id.logoutbuttonVideoList);
        this.logoutbuttonVideoList.setOnClickListener(this);
        String text = null;
        try {
            text = Utils.GetText("mobile/public_video_images/");
            Log.i("ShowPublicVideoImageActivity", "text ==> " + text);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.imageUrls = text.split("\n");
        for (int i = 1; i < this.imageUrls.length; i++) {
            this.imageUrls[i] = "http://mb.fotodekho.com/" + this.imageUrls[i];
            Log.i("ShowPublicVideoImageActivity", "imageUrls[i] => " + this.imageUrls[i]);
        }
        ListView gv = (ListView) findViewById(C0073R.C0075id.VideoList);
        gv.setAdapter(new ListAdapter(this, this.imageUrls));
        gv.setOnScrollListener(new SampleScrollListener(this));
        gv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                Intent i = new Intent(ShowVideoImagesActivity.this.getApplicationContext(), ShowVideos.class);
                i.putExtra("id", position);
                ShowVideoImagesActivity.this.startActivity(i);
            }
        });
    }

    public void onClick(View v) {
        v.setAnimation(this.buttonClick);
        switch (v.getId()) {
            case C0073R.C0075id.logoutbuttonVideoList /*2131427352*/:
                Log.d("ShowVideoImagesActivity", "logoutbuttonVideoList");
                Intent intent = new Intent(this.context, LoginActivity.class);
                intent.addFlags(268435456);
                intent.addFlags(32768);
                this.context.startActivity(intent);
                return;
            default:
                return;
        }
    }
}
