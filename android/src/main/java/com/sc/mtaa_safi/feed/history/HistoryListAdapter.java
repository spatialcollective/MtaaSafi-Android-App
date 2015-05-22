package com.sc.mtaa_safi.feed.history;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ishuah on 5/19/15.
 */
public class HistoryListAdapter extends BaseExpandableListAdapter {
    Context mContext;
    private List<String> mListHeader;
    private HashMap<String, List<String>> mListChild;

    public HistoryListAdapter(Context context, JSONObject reportHistory) throws JSONException {
        super();
        mContext = context;
        mListHeader = new ArrayList<>();
        mListChild = new HashMap<>();

        mListHeader.add("Report History");
        List<String> children = convertHistoryJSONToString(reportHistory);
        mListChild.put(mListHeader.get(0), children);
    }

    @Override
    public int getGroupCount() {
        return this.mListHeader.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return this.mListChild.get(this.mListHeader.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return this.mListHeader.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return this.mListChild.get(this.mListHeader.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        String headerTitle = (String) getGroup(i);
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.history_group_list, null);
        }

        TextView header = (TextView) view.findViewById(R.id.history_list_header);
        header.setTypeface(null, Typeface.BOLD);
        header.setText(headerTitle);

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        final String childText = (String) getChild(i, i1);

        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.history_list_item, null);
        }

        TextView txtListChild = (TextView) view.findViewById(R.id.history_list_item);

        txtListChild.setText(childText);
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    private List<String> convertHistoryJSONToString(JSONObject reportHistory) throws JSONException {
        String[] statusArray = new String[]{"'Broken'", "'In progress'", "'Fixed'"};

        List<String> reportHistoryString = new ArrayList<>();
        JSONArray reportHistoryArray = reportHistory.getJSONArray("reports");
        if (reportHistoryArray.length() == 0){
            reportHistoryString.add("No history.");
            return reportHistoryString;
        }
        for (int i = 0; i < reportHistoryArray.length() ; i++) {
            JSONObject j = (JSONObject) reportHistoryArray.get(i);
            if (i==0)
                reportHistoryString.add(j.getString("event")+" with status "+statusArray[j.getInt("status")]+" - "+Utils.getElapsedTime(j.getLong("timestamp")));
            else
                reportHistoryString.add(j.getString("event")+" to "+statusArray[j.getInt("status")]+" - "+Utils.getElapsedTime(j.getLong("timestamp")));
        }
        return reportHistoryString;
    }

}
