package com.example.gudrun.test;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    JSONObject artefacts;
    String myResponse;
    String url = "http://museum4all.integriert-studieren.jku.at/rest/artefacts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dl = (DrawerLayout)findViewById(R.id.activity_main);
        t = new ActionBarDrawerToggle(this, dl,R.string.open, R.string.close);

        dl.addDrawerListener(t);
        t.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Load all artefacts
        final NetworkAsyncTask httpTask = new NetworkAsyncTask(url);
        httpTask.execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Object response = httpTask.get();
                    myResponse = response.toString();

                    try {
                        artefacts = new JSONObject(myResponse).getJSONObject("artefacts");
                       /* Iterator<String> iterator = artefacts.keys();
                        while (iterator.hasNext()) {
                            String nodeID = iterator.next();
                            nodeIDList.add(nodeID);
                            Object tmp = artefacts.getJSONObject(nodeID);
                        }*/
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        nv = (NavigationView)findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.area:
                        Intent intent = new Intent(getApplicationContext(), ArtInAreaActivity.class);
                        intent.putExtra("artefacts", Objects.toString(myResponse));
                        startActivity(intent);
                        Toast.makeText(MainActivity.this, "Art in my Area",Toast.LENGTH_SHORT).show();break;
                    case R.id.find:
                        Intent intentFindArt = new Intent(getApplicationContext(), FindArtActivity.class);
                        //intent.putExtra("nodeList", Objects.toString(response));
                        startActivity(intentFindArt);
                        Toast.makeText(MainActivity.this, "Find Art",Toast.LENGTH_SHORT).show();break;
                    case R.id.inspect:
                        Toast.makeText(MainActivity.this, "Inspect Area",Toast.LENGTH_SHORT).show();break;
                    case R.id.museum:
                        Intent intentArtObjects = new Intent(getApplicationContext(), ArtInMuseum.class);
                        //intent.putExtra("nodeList", Objects.toString(response));
                        startActivity(intentArtObjects);
                        Toast.makeText(MainActivity.this, "List of Art in our Museum",Toast.LENGTH_SHORT).show();break;
                    default:
                        return true;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }
}