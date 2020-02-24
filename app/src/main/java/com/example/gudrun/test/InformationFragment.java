package com.example.gudrun.test;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class InformationFragment extends Fragment {

    String exhibitonsURL = "http://museum4all.integriert-studieren.jku.at/rest/exhibitions";
    String roomsURL = "http://museum4all.integriert-studieren.jku.at/rest/rooms";
    String amenitiesURL = "http://museum4all.integriert-studieren.jku.at/rest/amenities";

    ListAdapterView adapter;
    ListView exhibitionsList, roomsList, amenitiesList;
    String viewExhibitions = "";
    String viewRooms = "";
    String viewAmenities = "";

    ArrayList<String> listOfExhibitions = new ArrayList<>();
    ArrayList<String> listOfRooms = new ArrayList<>();
    ArrayList<String> listOfAmenities = new ArrayList<>();
    final ArrayList<String> exhibitionsNodeIDList = new ArrayList<>();
    final ArrayList<String> roomsNodeIDList = new ArrayList<>();
    final ArrayList<String> amenitiesNodeIDList = new ArrayList<>();

    View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_information, container, false);
        view.setBackgroundColor(Color.WHITE);
        exhibitionsList = (ListView) view.findViewById(R.id.exhibitionsList);
        roomsList = (ListView) view.findViewById(R.id.roomsList);
        amenitiesList = (ListView) view.findViewById(R.id.amenitiesList);

        listOfExhibitions.clear();
        listOfAmenities.clear();
        listOfRooms.clear();

        // get the exhibitions
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
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter = new ListAdapterView(getActivity(), listOfExhibitions);
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

        //get the rooms
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
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter = new ListAdapterView(getActivity(), listOfRooms);
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

        //get the amenities
        final NetworkAsyncTask amenitiesHttpsTask = new NetworkAsyncTask(amenitiesURL);
        amenitiesHttpsTask.execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Object response = amenitiesHttpsTask.get();
                    String myResponse = response.toString();
                    JSONObject exhibitions;
                    try {
                        exhibitions = new JSONObject(myResponse).getJSONObject("amenities");
                        Iterator<String> iterator = exhibitions.keys();
                        while (iterator.hasNext()) {
                            String nodeID = iterator.next();
                            amenitiesNodeIDList.add(nodeID);
                            Object tmp = exhibitions.getJSONObject(nodeID);
                            viewAmenities = ((JSONObject) tmp).getString("short_desc");
                            listOfAmenities.add(viewAmenities);
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter = new ListAdapterView(getActivity(), listOfAmenities);
                                    amenitiesList.setAdapter(adapter);
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
                Bundle bundle = new Bundle();
                bundle.putString("exhibition", Objects.toString(exhibitionsNodeIDList.get(i)));
                ((MainActivity)getActivity()).loadFragment(new ShowExhibitionFragment(), bundle);
            }
        });

        roomsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putString("room", Objects.toString(roomsNodeIDList.get(i)));
                ((MainActivity)getActivity()).loadFragment(new ShowRoomFragment(), bundle);
            }
        });

        amenitiesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putString("amenity", Objects.toString(amenitiesNodeIDList.get(i)));
                ((MainActivity)getActivity()).loadFragment(new ShowAmenityFragment(), bundle);
            }
        });
        return view;
    }
}
