package com.p003fd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.p000v4.view.ViewCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.MediaController;
import android.widget.VideoView;
import com.fotodekho.util.Utils;
import java.io.UnsupportedEncodingException;

/* renamed from: com.fd.ShowVideos */
public class ShowVideos extends Activity implements OnClickListener {
    private static int videoSeq = 0;
    ProgressDialog pDialog;
    String[] videoURL;
    VideoView videoview;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0073R.layout.activity_show_videos);
        getWindow().addFlags(128);
        String text = null;
        try {
            text = Utils.GetText("mobile/public_video/");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String[] tempVideoUrls = text.split("\n");
        this.videoURL = new String[(tempVideoUrls.length - 1)];
        int i = 1;
        int j = 0;
        while (i < tempVideoUrls.length) {
            this.videoURL[j] = "http://mb.fotodekho.com/" + tempVideoUrls[i];
            Log.i("ShowVideo", "videoURL[i] => " + this.videoURL[j]);
            i++;
            j++;
        }
        videoSeq = getIntent().getIntExtra("id", 0);
        this.videoview = (VideoView) findViewById(C0073R.C0075id.VideoView);
        String[] strArr = this.videoURL;
        int i2 = videoSeq;
        videoSeq = i2 + 1;
        playVideo(strArr[i2]);
    }

    private void playVideo(String videoURL2) {
        this.pDialog = new ProgressDialog(this);
        SpannableString ss1 = new SpannableString("Your Video is Streaming...");
        ss1.setSpan(new ForegroundColorSpan(ViewCompat.MEASURED_STATE_MASK), 0, ss1.length(), 0);
        SpannableString ss2 = new SpannableString("Buffering...");
        ss2.setSpan(new ForegroundColorSpan(-7829368), 0, ss2.length(), 0);
        this.pDialog.setTitle(ss1);
        this.pDialog.setMessage(ss2);
        this.pDialog.setIndeterminate(false);
        this.pDialog.setCancelable(false);
        this.pDialog.show();
        try {
            MediaController mediacontroller = new MediaController(this);
            mediacontroller.setAnchorView(this.videoview);
            Uri video = Uri.parse(videoURL2);
            this.videoview.setMediaController(mediacontroller);
            this.videoview.setVideoURI(video);
        } catch (Exception e) {
            Log.e("ShowVideo", e.getMessage());
            e.printStackTrace();
        }
        this.videoview.requestFocus();
        this.videoview.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                ShowVideos.this.pDialog.dismiss();
                ShowVideos.this.videoview.start();
            }
        });
    }

    public void onClick(View v) {
    }
}
