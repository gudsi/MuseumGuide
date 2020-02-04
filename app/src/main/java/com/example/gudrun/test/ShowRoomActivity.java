package com.example.gudrun.test;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class ShowRoomActivity extends AppCompatActivity {

    TextView title, longDesc, exhibitonId, location;
    ImageView image;

    String roomNr = "";
    String url = "http://museum4all.integriert-studieren.jku.at/rest/rooms";
    String img_url = "http://museum4all.integriert-studieren.jku.at/sites/default/files";
    String img_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_room);
        title = findViewById(R.id.shortdesc);
        image = findViewById(R.id.artImage);
        longDesc = findViewById(R.id.longdesc);
        location = findViewById(R.id.location);
        exhibitonId = findViewById(R.id.exhibitionId);

        longDesc.setMovementMethod(new ScrollingMovementMethod());

        roomNr = getIntent().getStringExtra("room");

        final NetworkAsyncTask httpTask = new NetworkAsyncTask(url + "/" + roomNr);
        httpTask.execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Object response = httpTask.get();
                    String myResponse = response.toString();
                    System.out.println(myResponse);
                    JSONObject description = null;
                    try {
                        description = new JSONObject(myResponse).getJSONObject(roomNr.toString());
                        title.setText(description.getString("short_desc"));
                        location.setText(description.getString("location"));

                        exhibitonId.setText(description.getString("exhibition_id"));

                        String i = description.getString("long_desc");
                        byte[] data = Base64.decode(description.getString("long_desc"), Base64.DEFAULT);
                        String tmp = new String(data, "UTF-8");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            longDesc.setText(Html.fromHtml(tmp, Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            longDesc.setText(Html.fromHtml(tmp));
                        }
                        img_path = description.get("picture").toString();
                        Path path = Paths.get(img_path);
                        int r = path.toString().indexOf(':');
                        String pt = path.toString().substring(r + 1);
                        img_url = img_url.concat(pt);

                        ShowRoomActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Picasso.get().load(img_url).into(image);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

