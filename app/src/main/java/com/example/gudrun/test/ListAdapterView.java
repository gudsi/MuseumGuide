package com.example.gudrun.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListAdapterView extends BaseAdapter {

    Context mContext;
    LayoutInflater inflater;
    private List<String> artefactsNameList;
    private ArrayList<String> arraylist;

    public ListAdapterView(Context context, List<String> artefactsNameList) {
        mContext = context;
        this.artefactsNameList = artefactsNameList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<String>();
        this.arraylist.addAll(this.artefactsNameList);
        System.out.println("list: " + arraylist);
    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        return artefactsNameList.size();
    }

    @Override
    public String getItem(int position) {
        return artefactsNameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.listview_item, null);
            holder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextView
        holder.name.setText(artefactsNameList.get(position));
        return view;
    }

    // Filter
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        artefactsNameList.clear();
        if (charText.isEmpty()) {
            artefactsNameList.addAll(arraylist);
        } else {
            for (String s : arraylist) {
                if (s.toLowerCase(Locale.getDefault()).contains(charText)) {
                    artefactsNameList.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }
}
