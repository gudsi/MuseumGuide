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

    String exhibitonsURL = "http://museum4all.integriert-studieren.jku.at/rest/exhibitions";
    String roomsURL = "http://museum4all.integriert-studieren.jku.at/rest/rooms";

    ListAdapterView adapter;
    ListView exhibitionsList;
    ListView roomsList;
    String viewExhibitions = "";
    String viewRooms = "";

    ArrayList<String> listOfExhibitions = new ArrayList<>();
    ArrayList<String> listOfRooms = new ArrayList<>();
    final ArrayList<String> exhibitionsNodeIDList = new ArrayList<>();
    final ArrayList<String> roomsNodeIDList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        exhibitionsList = (ListView) findViewById(R.id.exhibitionsList);
        roomsList = (ListView) findViewById(R.id.roomsList);
        final NetworkAsyncTask exhibitionsHttpTask = new NetworkAsyncTask(exhibitonsURL);
        exhibitionsHttpTask.execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Object response = exhibitionsHttpTask.get();
                    String myResponse = response.toString();
                    JSONObject exhibitions;
                    try {
                        exhibitions = new JSONObject(myResponse).getJSONObject("exhibitions");
                        Iterator<String> iterator = exhibitions.keys();
                        while (iterator.hasNext()) {
                            String nodeID = iterator.next();
                            exhibitionsNodeIDList.add(nodeID);
                            Object tmp = exhibitions.getJSONObject(nodeID);
                            viewExhibitions = ((JSONObject) tmp).getString("short_desc");
                            listOfExhibitions.add(viewExhibitions);
                            InformationAcitivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter = new ListAdapterView(InformationAcitivity.this, listOfExhibitions);
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

        final NetworkAsyncTask roomsHTttpTask = new NetworkAsyncTask(roomsURL);
        roomsHTttpTask.execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Object response = roomsHTttpTask.get();
                    String myResponse = response.toString();
                    JSONObject exhibitions;
                    try {
                        exhibitions = new JSONObject(myResponse).getJSONObject("rooms");
                        Iterator<String> iterator = exhibitions.keys();
                        while (iterator.hasNext()) {
                            String nodeID = iterator.next();
                            roomsNodeIDList.add(nodeID);
                            Object tmp = exhibitions.getJSONObject(nodeID);
                            viewRooms = ((JSONObject) tmp).getString("short_desc");
                            listOfRooms.add(viewRooms);
                            InformationAcitivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter = new ListAdapterView(InformationAcitivity.this, listOfRooms);
                                    roomsList.setAdapter(adapter);
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
                //Toast.makeText(getApplicationContext(), "Item Clicked:" + i, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ShowExhibitionActivity.class);
                intent.putExtra("exhibition", exhibitionsNodeIDList.get(i));
                startActivity(intent);
            }
        });

        roomsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getApplicationContext(), "Item Clicked:" + i, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ShowRoomActivity.class);
                intent.putExtra("room", roomsNodeIDList.get(i));
                startActivity(intent);
            }
        });
    }
}
