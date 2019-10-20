package com.example.gudrun.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.estimote.coresdk.service.BeaconManager;

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
    ArrayList<Integer> beaconsOfHTTPRequest = new ArrayList<>();

    Map<String, String> beaconsMajorToBID =  new HashMap<String, String>();

    String nodeId;

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
                String node = iterator.next();
                Object tmp = description.getJSONObject(node);
                String beaconID = ((JSONObject) tmp).getString("beacon_id");
                beaconsOfHTTPRequest.add(Integer.parseInt(beaconID));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        debug.setText("No Beacons found so far");

        beaconManager = new BeaconManager(this);
        beaconManager.setForegroundScanPeriod(1000, 0);
        regionMint = new BeaconRegion("regionMint",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {

            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {

                if (!list.isEmpty()) {
                    extractMapNodeIDToBID(list);
                    Log.d("Beacon found", "Count" + list.size() );
                    debug.setText("Beacons found: " + list.size());
                    // Show list of artefacts found
                    for (int i = 0; i < list.size(); i ++) {
                        Beacon beaconInRange = list.get(i);
                        int major = beaconInRange.getMajor();
                        nodeId = beaconsMajorToBID.get(Integer.toString(major));
                        final NetworkAsyncTask httpTask = new NetworkAsyncTask(url + "/" + nodeId);
                        httpTask.execute();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    final Object response = httpTask.get();
                                    String myResponse = response.toString();
                                    JSONObject description = null;
                                    try {
                                        description = new JSONObject(myResponse).getJSONObject(nodeId);
                                        listItems.add(description.getString("short_desc"));
                                        final List<String> myList = new ArrayList<>(listItems);
                                        ArtInAreaActivity.this.runOnUiThread(new Runnable() {
                                            public void run() {
                                                adapter = new ListAdapterView(ArtInAreaActivity.this, myList);
                                                artList.setAdapter(adapter);
                                            }
                                        });
                                        System.out.println("Das ist: " + description.getString("short_desc") + " beacon_id: " + description.getString("beacon_id") + " node Id " + nodeId);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                        artList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Toast.makeText(getApplicationContext(), "Item Clicked:" + i, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), ShowArtefactActivity.class);
                                //TODO nodeID is null in showArtefact
                                intent.putExtra("artefact", beaconsMajorToBID.get(nodeId));
                                System.out.println(intent.getStringExtra("artefact"));
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
        });

    }

    private void extractMapNodeIDToBID(List<Beacon> list) {
        String nodeIterator;
        try {
            //get information of each artefact
            JSONObject description = null;
            description = new JSONObject(artefacts).getJSONObject("artefacts");
            Iterator<String> iterator = description.keys();
            while (iterator.hasNext()) {
                nodeIterator = iterator.next();
                Object tmp = description.getJSONObject(nodeIterator);
                String id;
                id = ((JSONObject) tmp).getString("beacon_id");
                beaconsMajorToBID.put(id, nodeIterator);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
