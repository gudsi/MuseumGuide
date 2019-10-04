package com.example.gudrun.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.estimote.coresdk.service.BeaconManager;

public class ArtInAreaActivity extends AppCompatActivity {

    static final String APPID = "museum4all-f8y";
    static final String APPTOKEN = "40a915a4bb95894fb98fa382eef15c9c";

    private BeaconManager beaconManager;
    private BeaconRegion regionMint, regionBlue;

    ListAdapterView adapter;
    ListView artList;
    ArrayList<String> listItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_in_area);

        final Button button = findViewById(R.id.button);
        final TextView debug = findViewById(R.id.debug);
        artList = (ListView) findViewById(R.id.artList);


        debug.setText("No Beacons found so far");

        beaconManager = new BeaconManager(this);
        regionMint = new BeaconRegion("regionMint",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
        //regionBlue = new BeaconRegion("regionBlue", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        //TODO get the major. Make a table (key value pair) key is the major, value is the title
        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {

            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {
                if (!list.isEmpty()) {

                    Log.d("Beacon found", "Count" + list.size() );
                    debug.setText("Beacons found: " + list.size());
                    // Show list of artefacts found
                    //TODO match Major with artefacts.
                    for (int i = 0; i < list.size(); i ++) {
                        Beacon b = list.get(i);
                        listItems.add(Integer.toString(b.getMajor()));
                        ArtInAreaActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                adapter = new ListAdapterView(ArtInAreaActivity.this, listItems);
                                artList.setAdapter(adapter);
                            }
                        });
                        if (b.getMajor() == 62122) {
                            System.out.println("This is Monet");
                        }
                        if (b.getMajor() == 2) {
                            System.out.println("This is the tree art");
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
