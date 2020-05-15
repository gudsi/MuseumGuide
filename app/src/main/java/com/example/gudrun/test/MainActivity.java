package com.example.gudrun.test;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
                        Bundle bundle = new Bundle();
                        bundle.putString("artefacts", Objects.toString(myResponse));
                        loadFragment(new ArtInAreaFragment(), bundle);
                        Toast.makeText(MainActivity.this, "Art in my Area",Toast.LENGTH_SHORT).show();break;
                    case R.id.find:
                        loadFragment(new FindArtFragment(), null);
                        Toast.makeText(MainActivity.this, "Art in my Area",Toast.LENGTH_SHORT).show();break;
                    case R.id.museum:
                        loadFragment(new ArtInMuseumFragment(), null);
                        Toast.makeText(MainActivity.this, "List of Art in our Museum",Toast.LENGTH_SHORT).show();break;
                    case R.id.info:
                        loadFragment(new InformationFragment(), null);
                        Toast.makeText(MainActivity.this, "Information",Toast.LENGTH_SHORT).show();break;
                    case R.id.manual:
                        startActivity(new Intent(getApplicationContext(), ManualActivity.class)); break;
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

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().beginTransaction().commit();
        }
        else {
            super.onBackPressed();
        }
    }

    public void loadFragment(Fragment fragment, Bundle bundle) {
        fragment.setArguments(bundle);
    // create a FragmentManager
        FragmentManager fm = getFragmentManager();
    // create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
    // replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit(); // save the changes
    }
}