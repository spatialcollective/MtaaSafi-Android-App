// based on: https://gist.github.com/nidhi1608/104b31cb0ebc1f7b3f69
package com.sc.mtaa_safi.feed;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.settings.SettingsActivity;

import java.util.ArrayList;

public class NavigationDrawer extends DrawerLayout implements View.OnClickListener {
    NewsFeedFragment frag;
    private NavArrayAdapter adapter;
    private ListView list;
    private Feed mFeed;

    public NavigationDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public NavigationDrawer(Context context, AttributeSet attrs) { super(context, attrs); }
    public NavigationDrawer(Context context) { super(context); }

    public void setupDrawer(Toolbar drawerToolbar, NewsFeedFragment nff) {
        frag = nff;
        mFeed = Feed.getInstance(getContext());
        setDrawerListener(new ActionBarDrawerToggle(getActivity(), this, drawerToolbar, R.string.open, R.string.close));
        ((TextView) this.findViewById(R.id.user_name)).setText(Utils.getUserName(getActivity()));
        ((TextView) this.findViewById(R.id.user_email)).setText(Utils.getEmail(getActivity()));
        this.findViewById(R.id.settings_btn).setOnClickListener(new SettingsListener());

        createNavList();
        addNavItems();
    }

    private void createNavList() {
        list = (ListView) this.findViewById(R.id.nav_list);
        adapter = new NavArrayAdapter(getActivity(), new ArrayList<NavItem>());
        list.setAdapter(adapter);
        list.setOnItemClickListener(new NavItemListener());
    }
    private void addNavItems() {
        addNavItem(R.string.nearby, R.drawable.ic_place_grey, Feed.LOAD_ALL);
        addNavItem(R.string.my_activity, R.drawable.ic_message_grey600_24dp, Feed.LOAD_USER);
        createOtherPlacesChooser();
    }
    public void addNavItem(int name, int icon, String feed_value) {
        NavItem ni = new NavItem(name, icon, feed_value);
        adapter.items.add(ni);
    }

    private class NavItemListener implements ListView.OnItemClickListener {
        @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectNavItem(position);
        }
    }
    public void selectNavItem(int position) {
        NavItem navItem = (NavItem) adapter.getItem(position);
        list.setItemChecked(position, true);
        mFeed.feedContent = navItem.value;
        if (navItem.value == Feed.LOAD_USER)
            mFeed.feedContent += Utils.getUserId(getActivity());
        else if (navItem.value == Feed.LOAD_ALL && Utils.getNearbyAdmins(getActivity()) != "")
            mFeed.setFeedToNearby(getActivity());
        Log.e("Nav Drawer", "Feed Content: " + mFeed.feedContent);
        updateFeedView(navItem.name, -1);
    }

    private void updateFeedView(String title, long adminId) {
        mFeed.setLocation(title, adminId, getContext(), getRootView());
        closeDrawer(GravityCompat.START);
    }

    private void createOtherPlacesChooser() {
        ListView adminList = (ListView) this.findViewById(R.id.places_list);
        frag.placeAdapter = new SimpleCursorAdapter(getActivity(), R.layout.places_item,
                null, new String[] { Contract.Admin.COLUMN_NAME }, new int[] { R.id.place_name }, 0);
        adminList.setAdapter(frag.placeAdapter);
        adminList.setOnItemClickListener(new PlacesClickListener());
        frag.getLoaderManager().initLoader(frag.PLACES_LOADER, null, frag);
        this.findViewById(R.id.places_toggle).setOnClickListener(this);
    }

    private class PlacesClickListener implements ListView.OnItemClickListener {
        @Override public void onItemClick(AdapterView parent, View view, int pos, long id) {
            mFeed.feedContent = Feed.LOAD_ALL;
            if (id != -1)
                mFeed.feedContent += " AND " + Feed.LOAD_ADMIN + id;
            updateFeedView((String) ((TextView) view).getText(), id);
        }
    }

    @Override
    public void onClick(View view) {
        if (VISIBLE == this.findViewById(R.id.places_list).getVisibility())
            togglePlaces(GONE, R.drawable.ic_expand_more_grey, view);
        else
            togglePlaces(VISIBLE, R.drawable.ic_expand_less_grey, view);
    }
    private void togglePlaces(int visibility, int expandDrawable, View view) {
        this.findViewById(R.id.top_border).setVisibility(visibility);
        this.findViewById(R.id.places_list).setVisibility(visibility);
        ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(
                getResources().getDrawable(R.drawable.ic_map_grey), null, getResources().getDrawable(expandDrawable), null);
    }

    private FragmentActivity getActivity() { return (FragmentActivity) getContext(); }

    private class NavItem {
        public String name, value;
        public int icon;
        public NavItem(int name, int icon, String value) {
            this.name = getResources().getString(name);
            this.icon = icon;
            this.value = value;
        }
    }
    public class NavArrayAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public ArrayList<NavItem> items;

        public NavArrayAdapter(Context context, ArrayList<NavItem> values) {
            mInflater = LayoutInflater.from(context);
            this.items = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.nav_list_item, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            NavItem item = items.get(position);
            holder.tview.setText(item.name);
            holder.tview.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(item.icon), null, null, null);
            return view;
        }

        private class ViewHolder {
            public TextView tview; 
            public ViewHolder(View v) {
                super();
                tview = (TextView) v;
            }
        }

        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
    }

    private class SettingsListener implements View.OnClickListener {
        @Override public void onClick(View view) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), SettingsActivity.class);
            closeDrawer(GravityCompat.START);
            getActivity().startActivity(intent);
        }
    }
}
