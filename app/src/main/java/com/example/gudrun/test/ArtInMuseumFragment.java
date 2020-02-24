package com.example.gudrun.test;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;


public class ArtInMuseumFragment extends Fragment {
    String url = "http://museum4all.integriert-studieren.jku.at/rest/artefacts";

    ListAdapterView adapter;
    ListView artList;
    String viewArtefacts = "";
    ArrayList<String> listItems = new ArrayList<>();
    final ArrayList<String> nodeIDList = new ArrayList<>();
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_art_in_museum, container, false);
        artList = (ListView) view.findViewById(R.id.artList);
        view.setBackgroundColor(Color.WHITE);

        //get all artefacts stored in CMS
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
                        listItems.clear();
                        artefacts = new JSONObject(myResponse).getJSONObject("artefacts");
                        Iterator<String> iterator = artefacts.keys();
                        while (iterator.hasNext()) {
                            String nodeID = iterator.next();
                            nodeIDList.add(nodeID);
                            Object tmp = artefacts.getJSONObject(nodeID);
                            viewArtefacts = ((JSONObject) tmp).getString("short_desc");
                            listItems.add(viewArtefacts);
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter = new ListAdapterView(getActivity(), listItems);
                                    artList.setAdapter(adapter);
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

        artList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), "Item Clicked:" + i, Toast.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                bundle.putString("artefact", Objects.toString(nodeIDList.get(i)));
                ((MainActivity)getActivity()).loadFragment(new ShowArtefactFragment(), bundle);
            }
        });
        return view;
    }
}