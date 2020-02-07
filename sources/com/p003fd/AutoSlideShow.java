package com.p003fd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/* renamed from: com.fd.AutoSlideShow */
public class AutoSlideShow extends Activity implements OnClickListener {
    static int currentViewIndex;
    String[] allUrls2 = null;
    /* access modifiers changed from: private */
    public AlphaAnimation buttonClick = new AlphaAnimation(1.0f, 0.2f);
    Context context;
    private final int imagesToLoad = 6;
    Button logoutButton;
    Button pauselideshowbutton;
    Button playSlideShowButton;
    Button startSlideShowButton;
    Button stopSlideshowbutton;
    ViewFlipper viewFlipper = null;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0073R.layout.activity_auto_slide_show);
        getWindow().addFlags(128);
        this.context = getApplicationContext();
        this.pauselideshowbutton = (Button) findViewById(C0073R.C0075id.pauseSlideShowButton);
        this.playSlideShowButton = (Button) findViewById(C0073R.C0075id.playSlideShowButton);
        this.logoutButton = (Button) findViewById(C0073R.C0075id.logoutbutton);
        this.stopSlideshowbutton = (Button) findViewById(C0073R.C0075id.stopSlideShowButton);
        this.startSlideShowButton = (Button) findViewById(C0073R.C0075id.startSlideShowButtonAuto);
        this.pauselideshowbutton.setOnClickListener(this);
        this.playSlideShowButton.setOnClickListener(this);
        this.logoutButton.setOnClickListener(this);
        this.stopSlideshowbutton.setOnClickListener(this);
        this.startSlideShowButton.setOnClickListener(this);
        Intent intent = getIntent();
        String[] allUrls = intent.getExtras().getStringArray("allImageUrls");
        int position = intent.getExtras().getInt("position");
        this.allUrls2 = allUrls;
        this.viewFlipper = (ViewFlipper) findViewById(C0073R.C0075id.viewFlipper);
        this.viewFlipper.setFlipInterval(2000);
        this.viewFlipper.setAutoStart(true);
        this.viewFlipper.setOutAnimation(AnimationUtils.makeInChildBottomAnimation(getApplicationContext()));
        Log.i("AutoSlideShow", "position ==  " + position);
        int i = position;
        while (i < position + 6 && i < allUrls.length) {
            setFlipperImage(i, allUrls[i]);
            currentViewIndex = i + 1;
            i++;
        }
        this.viewFlipper.getOutAnimation().setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                Log.d("AutoSlideShow", "on animation start ");
            }

            public void onAnimationRepeat(Animation animation) {
                Log.d("AutoSlideShow", "on animation repeat ");
            }

            public void onAnimationEnd(Animation animation) {
                Log.d("AutoSlideShow", "on animation end ");
                AutoSlideShow.this.viewFlipper.setAnimation(AutoSlideShow.this.buttonClick);
                int displayedChild = AutoSlideShow.this.viewFlipper.getDisplayedChild();
                Log.d("AutoSlideShow", "viewFlipper.getDisplayedChild() ==> " + displayedChild);
                if (displayedChild == AutoSlideShow.this.viewFlipper.getChildCount() - 1) {
                    AutoSlideShow.this.viewFlipper.stopFlipping();
                    AutoSlideShow.this.playSlideShowButton.setVisibility(4);
                    AutoSlideShow.this.pauselideshowbutton.setVisibility(0);
                    Log.d("AutoSlideShow", "Donee ");
                }
            }
        });
    }

    private void setFlipperImage(int i, String url) {
        ImageView image = new ImageView(getApplicationContext());
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
        } catch (MalformedURLException e) {
            Log.e("AutoSlideShow", "exception occurred ..  " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e2) {
            Log.e("AutoSlideShow", "exception occurred ..  " + e2.getMessage());
            e2.printStackTrace();
        }
        image.setImageBitmap(bitmap);
        this.viewFlipper.addView(image);
    }

    public void onClick(View view) {
        view.setAnimation(this.buttonClick);
        switch (view.getId()) {
            case C0073R.C0075id.logoutbutton /*2131427329*/:
                Log.d("ShowImageActivity", "logoutbutton clicked..");
                Toast.makeText(this, "Logging Out..", 0).show();
                Intent intent2 = new Intent(this.context, LoginActivity.class);
                intent2.addFlags(268435456);
                intent2.addFlags(32768);
                this.context.startActivity(intent2);
                return;
            case C0073R.C0075id.stopSlideShowButton /*2131427330*/:
                Log.d("AutoSlideShowActivity", "Stop Slide Show button.. currentViewIndex: " + currentViewIndex);
                Intent intent3 = new Intent(this.context, SlideShowActivity.class);
                intent3.putExtra("allImageUrls", this.allUrls2);
                intent3.putExtra("position", currentViewIndex);
                intent3.addFlags(268435456);
                intent3.addFlags(32768);
                this.context.startActivity(intent3);
                return;
            case C0073R.C0075id.pauseSlideShowButton /*2131427331*/:
                Log.d("AutoSlideShowActivity", "Pause Slide Show button.. ");
                Toast.makeText(this, "Slide Show Paused.. ", 0).show();
                this.viewFlipper.stopFlipping();
                this.playSlideShowButton.setVisibility(0);
                this.pauselideshowbutton.setVisibility(4);
                return;
            case C0073R.C0075id.playSlideShowButton /*2131427332*/:
                Log.d("AutoSlideShowActivity", "play Slide Show button.. ");
                Toast.makeText(this, "Slide Show Started..", 0).show();
                this.viewFlipper.startFlipping();
                this.playSlideShowButton.setVisibility(4);
                this.pauselideshowbutton.setVisibility(0);
                return;
            case C0073R.C0075id.startSlideShowButtonAuto /*2131427333*/:
                Log.d("AutoSlideShowActivity", "next slides..  Slide Show button.. ");
                Toast.makeText(this, "Next Image Set Loading...", 0).show();
                this.viewFlipper.removeAllViews();
                for (int i = 0; i < 6 && currentViewIndex < this.allUrls2.length; i++) {
                    if (currentViewIndex == this.allUrls2.length) {
                        currentViewIndex = 0;
                    }
                    setFlipperImage(currentViewIndex, this.allUrls2[currentViewIndex]);
                    currentViewIndex++;
                }
                if (currentViewIndex >= this.allUrls2.length) {
                    currentViewIndex = 0;
                }
                this.playSlideShowButton.setVisibility(4);
                this.pauselideshowbutton.setVisibility(0);
                this.viewFlipper.startFlipping();
                return;
            default:
                return;
        }
    }
}
