package com.example.gudrun.test;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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


public class ShowArtefactFragment extends Fragment {

    TextView title, longDesc, location;
    ImageView image;

    String artefactNr = "";
    String url = "http://museum4all.integriert-studieren.jku.at/rest/artefacts";
    String img_url = "http://museum4all.integriert-studieren.jku.at/sites/default/files";
    String img_path;

    View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_show_artefact, container, false);
        view.setBackgroundColor(Color.WHITE);

        title = view.findViewById(R.id.shortdesc);
        image = view.findViewById(R.id.artImage);
        longDesc = view.findViewById(R.id.longdesc);
        location = view.findViewById(R.id.location);

        longDesc.setMovementMethod(new ScrollingMovementMethod());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            artefactNr = bundle.getString("artefact", "");
        }

        // get details from CMS about artefact
        final NetworkAsyncTask httpTask = new NetworkAsyncTask(url + "/" + artefactNr);
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
                        description = new JSONObject(myResponse).getJSONObject(artefactNr.toString());
                        title.setText(description.getString("short_desc"));
                        location.setText(description.getString("location"));

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
}
