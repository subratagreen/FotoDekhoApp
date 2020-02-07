package com.p003fd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.p000v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.Toast;
import com.fotodekho.adapter.CustomPagerAdapter;

/* renamed from: com.fd.SlideShowActivity */
public class SlideShowActivity extends Activity implements OnClickListener {
    String[] allUrls;
    private AlphaAnimation buttonClick = new AlphaAnimation(1.0f, 0.5f);
    Context context;
    Button logoutButton;
    ViewPager mViewPager = null;
    int position = 0;
    MenuItem rotate = null;
    MenuItem rotateOpp = null;
    Button startslideshowbutton;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0073R.layout.activity_slide_show);
        this.context = getApplicationContext();
        Intent i = getIntent();
        this.logoutButton = (Button) findViewById(C0073R.C0075id.logoutbutton);
        this.startslideshowbutton = (Button) findViewById(C0073R.C0075id.startslideshowbutton);
        this.logoutButton.setOnClickListener(this);
        this.startslideshowbutton.setOnClickListener(this);
        this.allUrls = i.getExtras().getStringArray("allImageUrls");
        this.position = i.getExtras().getInt("position");
        CustomPagerAdapter mCustomPagerAdapter = new CustomPagerAdapter(getApplicationContext(), this.position);
        mCustomPagerAdapter.setImages(this.allUrls);
        this.mViewPager = (ViewPager) findViewById(C0073R.C0075id.pager);
        this.mViewPager.setAdapter(mCustomPagerAdapter);
        for (int i1 = 0; i1 < this.position; i1++) {
            mCustomPagerAdapter.instantiateItem(this.mViewPager, i1);
        }
        mCustomPagerAdapter.firstTime = false;
        Log.i("SlideShowActivity", "current image  ==  " + this.mViewPager.getCurrentItem());
    }

    public void onClick(View arg0) {
        arg0.setAnimation(this.buttonClick);
        switch (arg0.getId()) {
            case C0073R.C0075id.logoutbutton /*2131427329*/:
                Log.d("ShowImageActivity", "logoutbutton clicked..");
                Intent intent2 = new Intent(this.context, LoginActivity.class);
                intent2.addFlags(268435456);
                intent2.addFlags(32768);
                this.context.startActivity(intent2);
                return;
            case C0073R.C0075id.startslideshowbutton /*2131427355*/:
                Log.d("ShowImageActivity", "startslideshowbutton clicked..");
                Toast.makeText(this, "Starting Slide Show ...", 0).show();
                Intent i = new Intent(getApplicationContext(), AutoSlideShow.class);
                i.putExtra("allImageUrls", this.allUrls);
                i.putExtra("position", this.position);
                startActivity(i);
                return;
            default:
                return;
        }
    }
}
