package com.p003fd;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/* renamed from: com.fd.SigninActivity */
public class SigninActivity extends AsyncTask<String, Void, String> {
    private Context context;

    public SigninActivity(Context context2) {
        this.context = context2;
    }

    /* access modifiers changed from: protected */
    public void onPreExecute() {
    }

    /* access modifiers changed from: protected */
    public String doInBackground(String... arg0) {
        try {
            String data = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(URLEncoder.encode("username", "UTF-8"))).append("=").append(URLEncoder.encode(arg0[0], "UTF-8")).toString())).append("&").append(URLEncoder.encode("password", "UTF-8")).append("=").append(URLEncoder.encode(arg0[1], "UTF-8")).toString();
            URLConnection conn = new URL("http://mb.fotodekho.com/loginDataAndroid.php").openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            wr.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            if (line != null) {
                Log.i("SigninActivity", "line => " + line);
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return new String("Exception: " + e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(String result) {
        if (result == null || !result.equals("user")) {
            Toast.makeText(this.context, "Login Failed..check login credentials", 1).show();
            return;
        }
        Toast.makeText(this.context, "Login Success", 1).show();
        this.context.startActivity(new Intent(this.context, ShowOptions.class));
    }
}
