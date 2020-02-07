package com.p003fd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.support.p000v4.view.ViewCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;
import com.fotodekho.adapter.GridViewAdapter;
import com.fotodekho.util.SampleScrollListener;
import com.fotodekho.util.Utils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressLint({"NewApi"})
/* renamed from: com.fd.LoginActivity */
public class LoginActivity extends Activity implements OnClickListener {
    private AlphaAnimation buttonClick = new AlphaAnimation(1.0f, 0.5f);
    Context context;
    Button demoAlbum;
    String[] imageUrls;
    Button loginButton;
    private EditText passwordField;
    private EditText usernameField;

    /* access modifiers changed from: protected */
    @SuppressLint({"NewApi"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0073R.layout.activity_login);
        if (VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new Builder().permitAll().build());
        }
        this.context = getApplicationContext();
        boolean internetConnection = checkInternetConnection();
        Log.i("LoginActivity", "checkInternetConnection() >> " + internetConnection);
        if (internetConnection) {
            showrecentImages();
        }
        this.usernameField = (EditText) findViewById(C0073R.C0075id.username);
        this.passwordField = (EditText) findViewById(C0073R.C0075id.pasword);
        this.usernameField.setOnClickListener(this);
        this.passwordField.setOnClickListener(this);
        this.loginButton = (Button) findViewById(C0073R.C0075id.loginButton);
        this.demoAlbum = (Button) findViewById(C0073R.C0075id.demoAlbum);
        this.loginButton.setOnClickListener(this);
        this.demoAlbum.setOnClickListener(this);
    }

    private void showrecentImages() {
        String text = null;
        try {
            text = Utils.GetText("mobile/latest_Images/");
            Log.d("LoginActivity", "latest Images ==> " + text);
        } catch (UnsupportedEncodingException e) {
            Log.e("LoginActivity", e.getMessage());
            e.printStackTrace();
        }
        String[] tempImageUrls = text.split("\n");
        this.imageUrls = new String[(tempImageUrls.length - 1)];
        int i = 1;
        int j = 0;
        while (i < tempImageUrls.length) {
            this.imageUrls[j] = "http://mb.fotodekho.com/" + tempImageUrls[i];
            Log.i("LoginActivity", "imageUrls[j] => " + this.imageUrls[j]);
            i++;
            j++;
        }
        GridView gv = (GridView) findViewById(C0073R.C0075id.gridViewMainPage);
        gv.setAdapter(new GridViewAdapter(this, this.imageUrls));
        gv.setOnScrollListener(new SampleScrollListener(this));
    }

    private boolean checkInternetConnection() {
        NetworkInfo netInfo = ((ConnectivityManager) getSystemService("connectivity")).getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            return false;
        }
        try {
            HttpURLConnection urlc = (HttpURLConnection) new URL("http://www.google.com").openConnection();
            Toast.makeText(this.context, "Checking Internet settings.. ", 0).show();
            urlc.setConnectTimeout(3000);
            urlc.setReadTimeout(3000);
            urlc.connect();
            if (urlc.getResponseCode() % 100 >= 4) {
                return false;
            }
            Log.i("LoginActivity", "google.com responded..");
            return true;
        } catch (MalformedURLException e1) {
            Log.e("LoginActivity", e1.getMessage());
            e1.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.e("LoginActivity", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void onClick(View arg0) {
        arg0.startAnimation(this.buttonClick);
        switch (arg0.getId()) {
            case C0073R.C0075id.demoAlbum /*2131427336*/:
                if (checkInternetConnection()) {
                    Intent intent = new Intent(this, ShowOptions.class);
                    intent.putExtra("publicContent", "publicContent");
                    startActivity(intent);
                    return;
                }
                showNetNotConnectedDialog();
                return;
            case C0073R.C0075id.username /*2131427337*/:
                this.usernameField.setText("");
                return;
            case C0073R.C0075id.pasword /*2131427338*/:
                this.passwordField.setText("");
                return;
            case C0073R.C0075id.loginButton /*2131427339*/:
                loginPost(arg0);
                return;
            default:
                return;
        }
    }

    public void loginPost(View view) {
        Log.d("LoginActivity", "checkInternetConnection() >> " + checkInternetConnection());
        try {
            getAlbumPaths();
        } catch (Exception ex) {
            Log.e("LoginActivity", "exception occurred ..  " + ex.getMessage());
            this.usernameField.setText("");
            this.passwordField.setText("");
        }
        AlphaAnimation buttonClick2 = new AlphaAnimation(1.0f, 0.5f);
        buttonClick2.setBackgroundColor(17170445);
        view.setAnimation(buttonClick2);
        String username = this.usernameField.getText().toString();
        String password = this.passwordField.getText().toString();
        if (username.equals("") || password.equals("")) {
            Toast.makeText(this.context, "Please Enter Username & Password", 1).show();
        } else if (checkInternetConnection()) {
            new SigninActivity(this).execute(new String[]{username, password});
        } else {
            showNetNotConnectedDialog();
        }
    }

    private void getAlbumPaths() {
        String albumNames = null;
        try {
            albumNames = Utils.GetAlbumNames(this.usernameField.getText().toString());
            Log.i("LoginActivity", "albumNames ==>  " + albumNames);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("LoginActivity", "exception occurred ..  " + e.getMessage());
        }
        setAlbumPathMap(albumNames);
    }

    private void setAlbumPathMap(String albumNames) {
        String[] albumNameArray = albumNames.replaceAll("\\[", "").split("\\]");
        for (int i = 0; i < albumNameArray.length - 1; i++) {
            String[] split = albumNameArray[i].split(":");
            Utils.albumPathsMap.put(split[0], split[1]);
        }
    }

    private void showNetNotConnectedDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        SpannableString ss1 = new SpannableString("Internet is not connected !! ");
        ss1.setSpan(new ForegroundColorSpan(ViewCompat.MEASURED_STATE_MASK), 0, ss1.length(), 0);
        SpannableString ss2 = new SpannableString("Connect internet then click Retry.. ");
        ss2.setSpan(new ForegroundColorSpan(-7829368), 0, ss2.length(), 0);
        alertDialogBuilder.setTitle(ss1);
        alertDialogBuilder.setMessage(ss2).setCancelable(true).setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        }).setNegativeButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                Intent intent = LoginActivity.this.getIntent();
                LoginActivity.this.finish();
                LoginActivity.this.startActivity(intent);
            }
        });
        alertDialogBuilder.create().show();
    }
}
