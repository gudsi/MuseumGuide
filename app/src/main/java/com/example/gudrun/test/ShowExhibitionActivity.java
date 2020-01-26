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


public class ShowExhibitionActivity extends AppCompatActivity {

        TextView title, longDesc, start_date, end_date, opening_hours;
        ImageView image;

        String exhibitionNr = "";
        String url = "http://museum4all.integriert-studieren.jku.at/rest/exhibitions";
        String img_url = "http://museum4all.integriert-studieren.jku.at/sites/default/files";
        String img_path;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_show_exhibition);
            title = findViewById(R.id.shortdesc);
            image = findViewById(R.id.artImage);
            longDesc = findViewById(R.id.longdesc);
            opening_hours = findViewById(R.id.opening_hours);
            start_date = findViewById(R.id.start_date);
            end_date = findViewById(R.id.end_date);

            longDesc.setMovementMethod(new ScrollingMovementMethod());

            exhibitionNr = getIntent().getStringExtra("exhibition");
            System.out.println("Got it"  + exhibitionNr);

            final NetworkAsyncTask httpTask = new NetworkAsyncTask(url + "/" + exhibitionNr);
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
                            description = new JSONObject(myResponse).getJSONObject(exhibitionNr.toString());
                            title.setText(description.getString("short_desc"));
                            //TODO decode
                            description.getJSONArray("opening_hours");
                            StringBuilder sb = new StringBuilder();
                            JSONArray array = description.getJSONArray("opening_hours");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject fromTo = array.getJSONObject(i);
                                int from = fromTo.getInt("from");
                                int to = fromTo.getInt("to");
                                if (from == 0 && to == 0) {
                                    continue;
                                }
                                sb.append(toHourString(from))
                                        .append('-')
                                        .append(toHourString(to))
                                        .append('\n');

                            }
                            opening_hours.setText(sb.toString());
                            String test = description.getString("opening_hours");


                            //TODO font of long_desc (bold text font lost)
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
                            String hopi = path.toString().substring(r + 1);
                            img_url = img_url.concat(hopi);
                            System.out.println(img_url);

                            String beaconID = description.getString("beacon_id");

                            com.example.gudrun.test.ShowExhibitionActivity.this.runOnUiThread(new Runnable() {
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

    private String toHourString(int seconds) {
            int hour = seconds / 3600;
            int minute = (seconds / 60) % 60;
            NumberFormat format = DecimalFormat.getInstance();
            format.setMinimumIntegerDigits(2);
            return hour + ":" + format.format(minute);
    }

    public static String pad(int num) {
        String res = null;
        if(num < 10)
            res = "0" + num;
        else
            res =  "" + num;

        return res;
    }

    static public String getStringTime(int time) {
        String res = null;
        int hour, min, sec;
        if (time > 0) {
            hour = time / 3600;
            time %= 3600;
            min = time / 60;
            sec = time % 60;

            res = pad(hour) +":" + pad(min) + ":" + pad(sec) + ":";
        }
        // TODO Auto-generated method stub
        return res;
    }
    }

