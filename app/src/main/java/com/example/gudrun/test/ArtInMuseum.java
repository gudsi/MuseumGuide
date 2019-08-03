package com.example.gudrun.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;

public class ArtInMuseum extends AppCompatActivity {

    String url = "http://museum4all.integriert-studieren.jku.at/rest/artefacts";

    Button getArtButton;

    ListView artList;
    String viewArtefacts = "";

    ArrayList<String> listItems = new ArrayList<>();
    final ArrayList<String> nodeIDList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_in_museum);

        artList = (ListView) findViewById(R.id.artList);
        getArtButton = (Button) findViewById(R.id.getButton);


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
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        getArtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ArtInMuseum.this, android.R.layout.simple_list_item_1, listItems);
                artList.setAdapter(adapter);
            }
        });

        artList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Item Clicked:" + i, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ShowArtefactActivity.class);
                intent.putExtra("artefact", nodeIDList.get(i));
                startActivity(intent);
            }
        });
    }
}
