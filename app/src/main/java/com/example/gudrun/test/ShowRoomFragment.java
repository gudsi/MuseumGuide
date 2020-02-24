package com.example.gudrun.test;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ShowRoomFragment extends Fragment {

    TextView title, longDesc, exhibitonId, location;
    ImageView image;

    String roomNr = "";
    String roomsURL = "http://museum4all.integriert-studieren.jku.at/rest/rooms";
    String exhibitionURL = "http://museum4all.integriert-studieren.jku.at/rest/exhibitions";
    String imgURL = "http://museum4all.integriert-studieren.jku.at/sites/default/files";
    String imgPath;
    View view;
    String exId = "";
    String exhibitionName= "";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_show_room, container, false);
        view.setBackgroundColor(Color.WHITE);

        title = view.findViewById(R.id.shortdesc);
        image = view.findViewById(R.id.artImage);
        longDesc = view.findViewById(R.id.longdesc);
        location = view.findViewById(R.id.location);
        exhibitonId = view.findViewById(R.id.exhibition);

        longDesc.setMovementMethod(new ScrollingMovementMethod());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            roomNr = bundle.getString("room", "");
        }

        // get the details from CMS
        final NetworkAsyncTask httpTask = new NetworkAsyncTask(roomsURL + "/" + roomNr);
        httpTask.execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Object response = httpTask.get();
                    String myResponse = response.toString();
                    JSONObject description = null;
                    try {
                        description = new JSONObject(myResponse).getJSONObject(roomNr.toString());
                        title.setText(description.getString("short_desc"));
                        location.setText(description.getString("location"));

                        exId = description.getString("exhibition_id");

                        // lookup the exhibition name
                        final NetworkAsyncTask exhibitionhttpTask = new NetworkAsyncTask(exhibitionURL + "/" + exId);
                        exhibitionhttpTask.execute();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final Object exhibitionResponse = exhibitionhttpTask.get();
                                    String exResponse = exhibitionResponse.toString();
                                    JSONObject exhibition = null;
                                    try {
                                        exhibition = new JSONObject(exResponse).getJSONObject(exId.toString());
                                        exhibitionName = exhibition.getString("short_desc");
                                        exhibitonId.setText(exhibitionName);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                        String i = description.getString("long_desc");
                        byte[] data = Base64.decode(description.getString("long_desc"), Base64.DEFAULT);
                        String tmp = new String(data, "UTF-8");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            longDesc.setText(Html.fromHtml(tmp, Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            longDesc.setText(Html.fromHtml(tmp));
                        }
                        imgPath = description.get("picture").toString();
                        Path path = Paths.get(imgPath);
                        int r = path.toString().indexOf(':');
                        String pt = path.toString().substring(r + 1);
                        imgURL = imgURL.concat(pt);

                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Picasso.get().load(imgURL).into(image);
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
}