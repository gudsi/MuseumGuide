package com.example.gudrun.test;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class ShowExhibitionFragment extends Fragment {

    TextView title, longDesc, startDate, endDate, openingHours;
    ImageView image;

    String exhibitionNr = "";
    String url = "http://museum4all.integriert-studieren.jku.at/rest/exhibitions";
    String img_url = "http://museum4all.integriert-studieren.jku.at/sites/default/files";
    String img_path;

    View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_show_exhibition, container, false);
        view.setBackgroundColor(Color.LTGRAY);
        title = view.findViewById(R.id.shortdesc);
        image = view.findViewById(R.id.artImage);
        longDesc = view.findViewById(R.id.longdesc);
        openingHours = view.findViewById(R.id.opening_hours);
        startDate = view.findViewById(R.id.start_date);
        endDate = view.findViewById(R.id.end_date);

        longDesc.setMovementMethod(new ScrollingMovementMethod());

        //get ID of the exhibition selected
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            exhibitionNr = bundle.getString("exhibition", "");
        }
        // get details about the exhibition
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
                        openingHours.setText(sb.toString());

                        startDate.setText(description.getString("start_date"));
                        endDate.setText(description.getString("end_date"));

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
                        System.out.println(img_url);

                        getActivity().runOnUiThread(new Runnable() {
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
        return view;
    }

    // Method to convert opening hours for displaying
    private String toHourString(int seconds) {
        int hour = seconds / 3600;
        int minute = (seconds / 60) % 60;
        NumberFormat format = DecimalFormat.getInstance();
        format.setMinimumIntegerDigits(2);
        return hour + ":" + format.format(minute);
    }
}

