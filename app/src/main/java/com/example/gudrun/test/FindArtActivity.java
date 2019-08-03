package com.example.gudrun.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;


public class FindArtActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    String url = "http://museum4all.integriert-studieren.jku.at/rest/artefacts";
    String viewArtefacts = "";

    ArrayList<String> listItems = new ArrayList<>();
    final ArrayList<String> nodeIDList = new ArrayList<>();

    ListAdapterView adapter;

    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_art);

        SearchView simpleSearchView = (SearchView) findViewById(R.id.search_view); // inititate a search view
        simpleSearchView.setQueryHint("Type a keyword");

        list = (ListView) findViewById(R.id.listview);

        final NetworkAsyncTask httpTask = new NetworkAsyncTask(url);
        httpTask.execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Object response = httpTask.get();
                    String myResponse = response.toString();
                    JSONObject artefacts;
                    try {
                        artefacts = new JSONObject(myResponse).getJSONObject("artefacts");
                        Iterator<String> iterator = artefacts.keys();
                        while (iterator.hasNext()) {
                            String nodeID = iterator.next();
                            nodeIDList.add(nodeID);
                            Object tmp = artefacts.getJSONObject(nodeID);
                            viewArtefacts = ((JSONObject) tmp).getString("short_desc");
                            listItems.add(viewArtefacts);
                            //System.out.println("artefacts: " + artefacts.getJSONObject(iterator.next()).getString("short_desc"));
                            FindArtActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter = new ListAdapterView(FindArtActivity.this, listItems);
                                    list.setAdapter(adapter);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        simpleSearchView = findViewById(R.id.search_view);
        simpleSearchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Item Clicked:" + i, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ShowArtefactActivity.class);
                intent.putExtra("artefact", nodeIDList.get(i));
                startActivity(intent);
            }
        });
        return false;
    }
}
