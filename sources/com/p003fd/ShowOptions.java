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
import android.widget.Button;

/* renamed from: com.fd.ShowOptions */
public class ShowOptions extends Activity implements OnClickListener {
    private Button backButton;
    private AlphaAnimation buttonClick = new AlphaAnimation(1.0f, 0.5f);
    Context context;
    private String publicContent = "blank";
    private Button showImageButton;
    private Button showVideoButton;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0073R.layout.activity_show_options);
        if (VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new Builder().permitAll().build());
        }
        this.context = getApplicationContext();
        this.showImageButton = (Button) findViewById(C0073R.C0075id.showImageButton);
        this.showVideoButton = (Button) findViewById(C0073R.C0075id.showVideoButton);
        this.backButton = (Button) findViewById(C0073R.C0075id.backButton);
        Intent i = getIntent();
        if (i.getExtras() != null) {
            this.publicContent = i.getExtras().getString("publicContent");
        }
        this.showImageButton.setOnClickListener(this);
        this.showVideoButton.setOnClickListener(this);
        this.backButton.setOnClickListener(this);
    }

    @SuppressLint({"NewApi"})
    public void onClick(View arg0) {
        arg0.setAnimation(this.buttonClick);
        switch (arg0.getId()) {
            case C0073R.C0075id.backButton /*2131427344*/:
                Log.d("ContactActivity", "backButton");
                Intent intent2 = new Intent(this.context, LoginActivity.class);
                intent2.addFlags(268435456);
                intent2.addFlags(32768);
                this.context.startActivity(intent2);
                return;
            case C0073R.C0075id.showVideoButton /*2131427347*/:
                Log.d("ContactActivity", "showVideoButton");
                startActivity(new Intent(getApplicationContext(), ShowVideoImagesActivity.class));
                return;
            case C0073R.C0075id.showImageButton /*2131427348*/:
                Log.d("ContactActivity", "showImageButton");
                if (this.publicContent == null || !this.publicContent.equals("publicContent")) {
                    startActivity(new Intent(getApplicationContext(), ShowImage.class));
                    return;
                } else {
                    startActivity(new Intent(getApplicationContext(), ShowPublicImages.class));
                    return;
                }
            default:
                return;
        }
    }
}
