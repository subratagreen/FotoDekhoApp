package com.p003fd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.webkit.WebView;
import android.widget.Button;

@SuppressLint({"NewApi"})
/* renamed from: com.fd.MainActivity */
public class MainActivity extends Activity implements OnClickListener {
    private AlphaAnimation buttonClick = new AlphaAnimation(1.0f, 0.5f);
    Context context;
    WebView wView;
    Button welcomeEnter;

    /* access modifiers changed from: protected */
    @SuppressLint({"NewApi"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0073R.layout.activity_main);
        if (VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new Builder().permitAll().build());
        }
        this.wView = (WebView) findViewById(C0073R.C0075id.webView1);
        this.wView.loadUrl("file:///android_asset/html/test.html");
        this.welcomeEnter = (Button) findViewById(C0073R.C0075id.welcomeEnter);
        this.welcomeEnter.setOnClickListener(this);
    }

    public void onClick(View v) {
        v.startAnimation(this.buttonClick);
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }
}
