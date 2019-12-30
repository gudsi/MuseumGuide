package com.example.gudrun.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.estimote.coresdk.service.BeaconManager;

import org.json.JSONException;
import org.json.JSONObject;

public class ArtInAreaActivity extends AppCompatActivity {

   // static final String APPID = "museum4all-f8y";
 //   static final String APPTOKEN = "40a915a4bb95894fb98fa382eef15c9c";

    String url = "http://museum4all.integriert-studieren.jku.at/rest/artefacts";

    private BeaconManager beaconManager;
    private BeaconRegion region;

    ListAdapterView adapter;
    ListView artList;
    LinkedHashSet<String> listItems = new LinkedHashSet<>();
    ArrayList<String> nodes = new ArrayList<>();
    String artefacts;
    ArrayList<Integer> beaconsOfHTTPRequest = new ArrayList<>();
    HashMap<String, String> beaconsMajorToBID =  new HashMap<>();
    HashMap<String, Beacon> nodeIDToBeacon =  new HashMap<String, Beacon>();
    String nodeId;
    JSONObject description = null;
    List<String> myList = new ArrayList<>();
    // Map of beacon major to info string, major = beaconID
    HashMap<Integer, JSONObject> majorToInfo = new HashMap<>();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_in_area);
        final DrawerLayout dl = (DrawerLayout)findViewById(R.id.activity_main);
        final TextView debug = findViewById(R.id.debug);
        artList = findViewById(R.id.artList);
        artefacts = getIntent().getStringExtra("artefacts");

        try {
            description = new JSONObject(artefacts).getJSONObject("artefacts");
            Iterator<String> iterator = description.keys();
            while (iterator.hasNext()) {
                String node = iterator.next();
                JSONObject tmp = description.getJSONObject(node);
                String beaconID = tmp.getString("beacon_id");
                beaconsOfHTTPRequest.add(Integer.parseInt(beaconID));
                majorToInfo.put(Integer.parseInt(beaconID), tmp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        debug.setText("No Beacons found so far");

        beaconManager = new BeaconManager(this);
        beaconManager.setForegroundScanPeriod(1000, 0);
        region = new BeaconRegion("region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {

            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> detectedBeacons) {
                myList.clear();
                if (!detectedBeacons.isEmpty()) {
                    extractMapNodeIDToBID(detectedBeacons);
                    //sortieren
                    final List<Integer> majorsSortedByDistance = sortBeacon(detectedBeacons);

                    Log.d("Beacon found", "Count" + detectedBeacons.size() );
                    debug.setText("Beacons found: " + detectedBeacons.size());
                    // Show list of artefacts found
                    for (int i = 0; i < detectedBeacons.size(); i ++) {
                        // Crashes!!!!!!!!!!!!!
                      //  Snackbar snackbar = Snackbar.make(dl, "BEACON", Snackbar.LENGTH_LONG);
                      //  snackbar.show();
                        Beacon beaconInRange = detectedBeacons.get(i);
                        final int major = beaconInRange.getMajor();
                        nodeId = beaconsMajorToBID.get(Integer.toString(major));
                        final NetworkAsyncTask httpTask = new NetworkAsyncTask(url + "/" + nodeId);
                        nodes.add(nodeId);
                        nodeIDToBeacon.put(nodeId, detectedBeacons.get(i));
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
                                        //myList = new ArrayList<>(listItems);
                                        for(Integer beaconId : majorsSortedByDistance)  {
                                            String tmp = majorToInfo.get(beaconId).getString("short_desc");
                                            myList.add(tmp);
                                        }
                                        ArtInAreaActivity.this.runOnUiThread(new Runnable() {
                                            public void run() {
                                                adapter = new ListAdapterView(ArtInAreaActivity.this, myList);
                                                artList.setAdapter(adapter);
                                            }

                                        });

                                       // System.out.println("Das ist: " + description.getString("short_desc") + " beacon_id: " + description.getString("beacon_id") + " node Id " + nodeId);
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
                                intent.putExtra("artefact", nodes.get(i));
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
            JSONObject description = new JSONObject(artefacts).getJSONObject("artefacts");
            Iterator<String> iterator = description.keys();
            while (iterator.hasNext()) {
                nodeIterator = iterator.next();
                JSONObject tmp = description.getJSONObject(nodeIterator);
                String id;
                id = tmp.getString("beacon_id");
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
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);
        super.onPause();
    }

    protected Map<Integer, Double> getDistance(List nodeList) {
        Map<Integer, Double> BeaconMajorToDistance = new HashMap<>();
        double distance;
        Beacon bbb;
        int power, rssi;
        for (int i = 0; i < nodeList.size(); i ++) {
            bbb = (Beacon) nodeList.get(i);
            power = bbb.getMeasuredPower();
            rssi = bbb.getRssi();
            distance = Math.pow(10.0, (double)(power - rssi) / 20.0);
            BeaconMajorToDistance.put(bbb.getMajor(), distance);
        }
        return BeaconMajorToDistance;
    }

    protected LinkedList<Integer> sortBeacon(List nodeList) {
        Map<Integer, Double> beaconMajorToDistance = getDistance(nodeList);
       // ArrayList<Integer, Beacon> beaconsSortedByDistance = new ArrayList<>();
        List<Double> distances = new ArrayList<>(beaconMajorToDistance.values());
        Collections.sort(distances);
        Set<Map.Entry<Integer, Double>> entries = beaconMajorToDistance.entrySet();


        Comparator<Map.Entry<Integer, Double>> valueComparator = new Comparator<Map.Entry<Integer, Double>>() {

            @Override
            public int compare(Map.Entry<Integer, Double> e1, Map.Entry<Integer, Double> e2) {
                Double v1 = e1.getValue();
                Double v2 = e2.getValue();
                return v1.compareTo(v2);
            }
        };

        // Sort method needs a List, so let's first convert Set to List in Java
        List<Map.Entry<Integer, Double>> listOfEntries = new ArrayList<Map.Entry<Integer, Double>>(entries);

        // sorting HashMap by values using comparator
        Collections.sort(listOfEntries, valueComparator);

        LinkedHashMap<Integer, Double> sortedByValue = new LinkedHashMap<Integer, Double>(listOfEntries.size());

        // copying entries from List to Map
        for(Map.Entry<Integer, Double> entry : listOfEntries){
            sortedByValue.put(entry.getKey(), entry.getValue());
        }

        LinkedList<Integer> majorsSortedByDistance = new LinkedList<>();

        for(Integer major : sortedByValue.keySet()) {
            majorsSortedByDistance.add(major);
        }

        return majorsSortedByDistance;
    }
}
