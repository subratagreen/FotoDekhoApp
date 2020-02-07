package com.fotodekho.util;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import com.p003fd.MainActivity;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static Map<String, String> albumPathsMap = new HashMap();
    public static final String mobile_image_path = "mobile_image_path";
    public static final String mobile_video_image_path = "mobile_video_image_path";
    public static final String mobile_video_path = "mobile_video_path";

    public static void CopyStream(InputStream is, OutputStream os) {
        try {
            byte[] bytes = new byte[1024];
            while (true) {
                int count = is.read(bytes, 0, 1024);
                if (count != -1) {
                    os.write(bytes, 0, count);
                } else {
                    return;
                }
            }
        } catch (Exception ex) {
            Log.e("Utils", "exception occurred ..  " + ex.getMessage());
        }
    }

    public static String GetText(String album_path) throws UnsupportedEncodingException {
        String data = new StringBuilder(String.valueOf(URLEncoder.encode("album_path", "UTF-8"))).append("=").append(URLEncoder.encode(album_path, "UTF-8")).toString();
        String text = "";
        BufferedReader reader = null;
        try {
            URLConnection conn = new URL("http://mb.fotodekho.com/getAllFiles.php").openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            try {
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line1 = reader2.readLine();
                    if (line1 == null) {
                        break;
                    }
                    sb.append(new StringBuilder(String.valueOf(line1)).append("\n").toString());
                    Log.i("Utils", "line1 => " + line1);
                }
                text = sb.toString();
                try {
                    reader2.close();
                    BufferedReader bufferedReader = reader2;
                } catch (Exception ex) {
                    Log.e("Utils", "exception occurred ..  " + ex.getMessage());
                    ex.printStackTrace();
                    BufferedReader bufferedReader2 = reader2;
                }
            } catch (Exception e) {
                ex = e;
                reader = reader2;
            } catch (Throwable th) {
                th = th;
                reader = reader2;
                try {
                    reader.close();
                } catch (Exception ex2) {
                    Log.e("Utils", "exception occurred ..  " + ex2.getMessage());
                    ex2.printStackTrace();
                }
                throw th;
            }
        } catch (Exception e2) {
            ex = e2;
            try {
                Log.e("Utils", "exception occurred ..  " + ex.getMessage());
                ex.printStackTrace();
                try {
                    reader.close();
                } catch (Exception ex3) {
                    Log.e("Utils", "exception occurred ..  " + ex3.getMessage());
                    ex3.printStackTrace();
                }
                return text;
            } catch (Throwable th2) {
                th = th2;
                reader.close();
                throw th;
            }
        }
        return text;
    }

    public static String GetAlbumNames(String username) throws UnsupportedEncodingException {
        String data = new StringBuilder(String.valueOf(URLEncoder.encode("myusername", "UTF-8"))).append("=").append(URLEncoder.encode(username, "UTF-8")).toString();
        String albumNames = "";
        BufferedReader reader = null;
        try {
            URLConnection conn = new URL("http://mb.fotodekho.com/getAlbumPath.php").openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            try {
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line1 = reader2.readLine();
                    if (line1 == null) {
                        break;
                    }
                    sb.append(new StringBuilder(String.valueOf(line1)).append("\n").toString());
                }
                albumNames = sb.toString();
                try {
                    reader2.close();
                    BufferedReader bufferedReader = reader2;
                } catch (Exception ex) {
                    Log.e("Utils", "exception occurred ..  " + ex.getMessage());
                    ex.printStackTrace();
                    BufferedReader bufferedReader2 = reader2;
                }
            } catch (Exception e) {
                ex = e;
                reader = reader2;
            } catch (Throwable th) {
                th = th;
                reader = reader2;
                try {
                    reader.close();
                } catch (Exception ex2) {
                    Log.e("Utils", "exception occurred ..  " + ex2.getMessage());
                    ex2.printStackTrace();
                }
                throw th;
            }
        } catch (Exception e2) {
            ex = e2;
            try {
                Log.e("Utils", "exception occurred ..  " + ex.getMessage());
                ex.printStackTrace();
                try {
                    reader.close();
                } catch (Exception ex3) {
                    Log.e("Utils", "exception occurred ..  " + ex3.getMessage());
                    ex3.printStackTrace();
                }
                return albumNames;
            } catch (Throwable th2) {
                th = th2;
                reader.close();
                throw th;
            }
        }
        return albumNames;
    }

    public void logout(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(268435456);
        intent.addFlags(32768);
        context.startActivity(intent);
    }

    public void showNetNotConnectedDialog(final Activity activity) {
        Builder alertDialogBuilder = new Builder(activity);
        alertDialogBuilder.setTitle("Internet is not working.. Please check internet connectivity and retry");
        alertDialogBuilder.setMessage("If Internet is connected click Retry to reload the application.. ").setCancelable(false).setPositiveButton("Exit", new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                activity.finish();
            }
        }).setNegativeButton("Retry", new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                Intent intent = activity.getIntent();
                activity.finish();
                activity.startActivity(intent);
            }
        });
        alertDialogBuilder.create().show();
    }
}
