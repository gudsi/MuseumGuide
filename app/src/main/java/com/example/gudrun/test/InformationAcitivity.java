package com.example.gudrun.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class InformationAcitivity extends AppCompatActivity {

    String url = "http://museum4all.integriert-studieren.jku.at/rest/exhibitions";

    ListAdapterView adapter;
    ListView exhibitionsList;
    String viewExhibitions = "";

    ArrayList<String> listItems = new ArrayList<>();
    final ArrayList<String> nodeIDList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        exhibitionsList = (ListView) findViewById(R.id.exhibitionsList);
        final NetworkAsyncTask httpTask = new NetworkAsyncTask(url);
        httpTask.execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Object response = httpTask.get();
                    String myResponse = response.toString();
                    JSONObject exhibitions;
                    try {
                        exhibitions = new JSONObject(myResponse).getJSONObject("exhibitions");
                        Iterator<String> iterator = exhibitions.keys();
                        while (iterator.hasNext()) {
                            String nodeID = iterator.next();
                            nodeIDList.add(nodeID);
                            Object tmp = exhibitions.getJSONObject(nodeID);
                            viewExhibitions = ((JSONObject) tmp).getString("short_desc");
                            listItems.add(viewExhibitions);
                            InformationAcitivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter = new ListAdapterView(InformationAcitivity.this, listItems);
                                    exhibitionsList.setAdapter(adapter);
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

        exhibitionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Item Clicked:" + i, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ShowExhibitionActivity.class);
                intent.putExtra("exhibition", nodeIDList.get(i));
                startActivity(intent);
            }
        });
    }
}
