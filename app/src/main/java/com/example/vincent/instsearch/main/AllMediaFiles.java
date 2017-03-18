package com.example.vincent.instsearch.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;


import com.example.vincent.instsearch.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class AllMediaFiles extends Activity {
    private InstagramSession mSession;
    private GridView gvAllImages;
    private HashMap<String, String> userInfo;
    private ArrayList<String> imageThumbList = new ArrayList<String>();
    private Context context;
    private int WHAT_FINALIZE = 0;
    private static int WHAT_ERROR = 1;
    private ProgressDialog pd;
    public static final String TAG_DATA = "data";
    public static final String TAG_IMAGES = "images";
    public static final String TAG_THUMBNAIL = "thumbnail";
    public static final String TAG_URL = "url";
    private Handler handler = new Handler(new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (pd != null && pd.isShowing())
                pd.dismiss();
            if (msg.what == WHAT_FINALIZE) {
                setImageGridAdapter();
            } else {
                Toast.makeText(context, "Check your network.",
                        Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_media_list_files);
        gvAllImages = (GridView) findViewById(R.id.gvAllImages);
        userInfo = (HashMap<String, String>) getIntent().getSerializableExtra(
                "userInfo");

        context = AllMediaFiles.this;

        mSession = new InstagramSession(context);
        String token = mSession.getAccessToken();


        getAllMediaImages(token);
    }

    private void setImageGridAdapter() {
        gvAllImages.setAdapter(new MyGridListAdapter(context,imageThumbList));
    }

    private void getAllMediaImages(final String token) {
        pd = ProgressDialog.show(context, "", "Loading images...");
        new Thread(new Runnable() {
            HttpURLConnection urlConnection = null;
            String resultJson;

            @Override
            public void run() {
                int what = WHAT_FINALIZE;

                try {
                    URL url = new URL("https://api.instagram.com/v1/users/"
                            + userInfo.get(InstagramApp.TAG_ID)
                            + "/media/recent/?access_token="
                            + token);

                    Log.d("STRINGFORMAAAAAAAAAAT", token);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    inputStream.close();
                    resultJson = buffer.toString();

                    JSONObject json = new JSONObject(resultJson);


                    JSONArray data = json.getJSONArray(TAG_DATA);
                    for (int data_i = 0; data_i < data.length(); data_i++) {
                        JSONObject data_obj = data.getJSONObject(data_i);

                        JSONObject images_obj = data_obj
                                .getJSONObject(TAG_IMAGES);

                        JSONObject thumbnail_obj = images_obj
                                .getJSONObject(TAG_THUMBNAIL);

                        String str_url = thumbnail_obj.getString(TAG_URL);

                        imageThumbList.add(str_url);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    what = WHAT_ERROR;
                }
                handler.sendEmptyMessage(what);
            }
        }).start();
    }
}