package com.example.gudrun.test;

import android.app.IntentService;
import android.content.Intent;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.List;
import java.util.UUID;


public class Snacktest extends IntentService {

    private BeaconManager beaconManager;
    private BeaconRegion region;
    int cnt;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public Snacktest() {
        super("test");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        beaconManager = new BeaconManager(this);
        beaconManager.setForegroundScanPeriod(1000, 0);
        region = new BeaconRegion("region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        Toast.makeText(this, "I am here", Toast.LENGTH_SHORT).show();
        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {

            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> detectedBeacons) {

                if (!detectedBeacons.isEmpty()) {
                    // TODO Find proper place to put
                    //Snackbar.make(findViewById((android.R.id.content), "BEACON", Snackbar.LENGTH_LONG).show();
                    System.out.print("Snackbar");


                    Log.d("Beacons found: ", "Number" + detectedBeacons.size());
                    // Show list of artefacts found
                    cnt = detectedBeacons.size();
                    for (int i = 0; i < detectedBeacons.size(); i++) {
                        Beacon beaconInRange = detectedBeacons.get(i);
                    }
                }
            }
        });
        //Toast.makeText(this, cnt, Toast.LENGTH_SHORT).show();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onHandleIntent(@Nullable Intent intent) {

    }
}