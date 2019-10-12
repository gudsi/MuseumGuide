package com.example.gudrun.test;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import com.estimote.coresdk.service.BeaconManager;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class ArtInAreaActivity extends AppCompatActivity {

    static final String APPID = "museum4all-f8y";
    static final String APPTOKEN = "40a915a4bb95894fb98fa382eef15c9c";

    String url = "http://museum4all.integriert-studieren.jku.at/rest/artefacts";

    private BeaconManager beaconManager;
    private BeaconRegion regionMint;
    //TODO regionMint = region

    ListAdapterView adapter;
    ListView artList;
    LinkedHashSet<String> listItems = new LinkedHashSet<>();
    //ArrayList<String> listItems = new ArrayList<>();
    String artefacts;
    ArrayList<Integer> beacons = new ArrayList<>();

    String nodeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_in_area);

        final Button button = findViewById(R.id.button);
        final TextView debug = findViewById(R.id.debug);
        artList = (ListView) findViewById(R.id.artList);
        artefacts = getIntent().getStringExtra("artefacts");

        try {
            JSONObject description = null;
            description = new JSONObject(artefacts).getJSONObject("artefacts");
            Iterator<String> iterator = description.keys();
            while (iterator.hasNext()) {
                String nodeID = iterator.next();
                Object tmp = description.getJSONObject(nodeID);
                String beaconID = ((JSONObject) tmp).getString("beacon_id");
                beacons.add(Integer.parseInt(beaconID));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        debug.setText("No Beacons found so far");

        beaconManager = new BeaconManager(this);
        regionMint = new BeaconRegion("regionMint",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {

            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Log.d("Beacon found", "Count" + list.size() );
                    debug.setText("Beacons found: " + list.size());
                    // Show list of artefacts found
                    //TODO show short_desc
                    for (int i = 0; i < list.size(); i ++) {
                        Beacon b = list.get(i);
                        listItems.add(Integer.toString(b.getMajor()));
                        final List<String> myList = new ArrayList<>(listItems);
                        ArtInAreaActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                adapter = new ListAdapterView(ArtInAreaActivity.this, myList);
                                artList.setAdapter(adapter);
                            }
                        });
                        if (beacons.contains(b.getMajor())) {
                            String name ="";
                            try {
                                JSONObject description = null;
                                description = new JSONObject(artefacts).getJSONObject("artefacts");
                                Iterator<String> iterator = description.keys();
                                while (iterator.hasNext()) {
                                    nodeID = iterator.next();
                                    Object tmp = description.getJSONObject(nodeID);
                                    String id;
                                    id = ((JSONObject) tmp).getString("beacon_id");
                                    if(Integer.parseInt(id) == b.getMajor()) {
                                        final NetworkAsyncTask httpTask = new NetworkAsyncTask(url + "/" + nodeID);
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
                                                        description = new JSONObject(myResponse).getJSONObject(nodeID.toString());
                                                        System.out.println("Das ist: " + description.getString("short_desc") + " beacon_id: " + description.getString("beacon_id") + " node Id " + nodeID);
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
                                System.out.println(name);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(regionMint);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(regionMint);
        super.onPause();
    }
}
