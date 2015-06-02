package com.sc.mtaa_safi;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.database.SyncUtils;

public class Feed { // Singleton for keeping track of feed state
    private static Feed mInstance = null;

    public final static String  SORT_RECENT = Contract.Entry.COLUMN_SERVER_ID + " DESC",
                                SORT_UPVOTES = Contract.Entry.COLUMN_UPVOTE_COUNT + " DESC",
                                LOAD_ALL = Contract.Entry.COLUMN_PENDINGFLAG  + " < " + 0,
                                LOAD_USER = Contract.Entry.COLUMN_USERID  + " == ",
                                LOAD_ADMIN = Contract.Entry.COLUMN_ADMIN_ID  + " == ",
                                LOAD_NEARBY_ADMINS = Contract.Entry.COLUMN_ADMIN_ID  + " IN(";
    public String feedContent = Contract.Entry.COLUMN_PENDINGFLAG  + " < " + 0;
    public int index = 0, top = 0, navIndex = 0, navPos = 0;
    public CharSequence title;
    public String sortOrder = SORT_RECENT;

    public interface ResortListener {
        void triggerReload();
    }
    private ResortListener listener;
    public void setListener(ResortListener sl) {
        listener = sl;
    }

    private Feed(Context c) {
        title = c.getResources().getString(R.string.nearby);
    }

    public static Feed getInstance(Context c) {
        if (mInstance == null)
            mInstance = new Feed(c);
        return mInstance;
    }

    public void setSection(int val) {
        navPos = val;
    }

    public void setTitle(CharSequence val, View view) {
        title = val;
        try { ((TextView) view.findViewById(R.id.title)).setText(title); }
        catch (Exception e) { Log.e("Feed", "Failed to set title"); }
    }

    public void setLocation(String name, long id, Context c, View v) {
        Utils.saveSelectedAdmin(c, name, id);
        setTitle(name, v);
        SyncUtils.AttemptRefresh(c);
        listener.triggerReload();
    }

    public void sort(String sorting) {
        if (sorting != sortOrder) {
            sortOrder = sorting;
            listener.triggerReload();
        }
    }

    public class SortListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            navIndex = pos;
            if (pos == 0) sort(SORT_RECENT);
            else sort(SORT_UPVOTES);
        }
        public void onNothingSelected(AdapterView<?> parent) { }
    }

//    public void restoreState(Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            title = savedInstanceState.getCharSequence("title");
//            index = savedInstanceState.getInt("index");
//            top = savedInstanceState.getInt("top");
//            feedContent = savedInstanceState.getString("feedContent");
//            sortOrder = savedInstanceState.getString("sortOrder");
//            navIndex = savedInstanceState.getInt("navIndex");
//        }
//    }
//    public Bundle saveState(Bundle outstate) {
//        outstate.putCharSequence("title", title);
//        outstate.putInt("top", top);
//        outstate.putInt("index", index);
//        outstate.putString("feedContent", feedContent);
//        outstate.putString("sortOrder", sortOrder);
//        outstate.putInt("navIndex", navIndex);
//        return outstate;
//    }
}
