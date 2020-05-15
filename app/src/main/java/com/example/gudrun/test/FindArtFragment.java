package com.example.gudrun.test;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;


public class FindArtFragment extends Fragment implements SearchView.OnQueryTextListener {

    String url = "http://museum4all.integriert-studieren.jku.at/rest/artefacts";
    String viewArtefacts = "";
    ArrayList<String> listItems = new ArrayList<>();
    final ArrayList<String> nodeIDList = new ArrayList<>();
    ListAdapterView adapter;
    ListView list;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_find_art, container, false);
        view.setBackgroundColor(Color.WHITE);

        SearchView simpleSearchView = (SearchView) view.findViewById(R.id.search_view); // inititate a search view
        simpleSearchView.setQueryHint("Type a keyword");

        list = (ListView) view.findViewById(R.id.listview);

        //Call all artefacts stored to show in list
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
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter = new ListAdapterView(getActivity(), listItems);
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

        simpleSearchView = view.findViewById(R.id.search_view);
        simpleSearchView.setOnQueryTextListener(this);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putString("artefact", Objects.toString(nodeIDList.get(i)));
                ((MainActivity)getActivity()).loadFragment(new ShowArtefactFragment(), bundle);
            }
        });
        return view;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        return false;
    }
}
