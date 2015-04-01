// based on: https://gist.github.com/nidhi1608/104b31cb0ebc1f7b3f69
package com.sc.mtaa_safi.feed;

import android.content.Context;
import android.opengl.Visibility;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.Contract;

import java.util.ArrayList;

public class NavigationDrawer extends DrawerLayout implements View.OnClickListener{
    NewsFeedFragment frag;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavArrayAdapter adapter;
    private ArrayList<NavItem> navItems;
    private ListView list;

    public NavigationDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public NavigationDrawer(Context context, AttributeSet attrs) { super(context, attrs); }
    public NavigationDrawer(Context context) { super(context); }

    public void setupDrawer(Toolbar drawerToolbar, NewsFeedFragment nff) {
        frag = nff;
        navItems = new ArrayList<>();
        ((TextView) this.findViewById(R.id.user_name)).setText(Utils.getUserName(getActivity()));
        ((TextView) this.findViewById(R.id.user_email)).setText(Utils.getEmail(getActivity()));

        list = (ListView) this.findViewById(R.id.nav_list);
        adapter = new NavArrayAdapter(getActivity(), new ArrayList<NavItem>());
        list.setAdapter(adapter);
        list.setOnItemClickListener(new NavItemListener());

        toolbar = drawerToolbar;
        setupDrawerToggle();

        addNavItem(R.string.nearby, R.drawable.ic_place_grey, Contract.Entry.COLUMN_PENDINGFLAG  + " < " + 0);
        addNavItem(R.string.my_activity, R.drawable.ic_message_grey600_24dp, Contract.Entry.COLUMN_USERID  + " == " + Utils.getUserId(getActivity()));
        createLocationChooser(this);
    }

    public void addNavItem(int name, int icon, String feed_value) {
        NavItem ni = new NavItem(name, icon, feed_value);
        adapter.items.add(ni);
        navItems.add(ni);
    }

    public void selectNavItem(int position) {
        NavItem navItem = navItems.get(position);
        frag.FEED_CONTENT = navItem.value;
        frag.getLoaderManager().restartLoader(frag.FEED_LOADER, null, frag);
        list.setItemChecked(position, true);
        setTitle(navItem.name); // ;
        closeDrawer(GravityCompat.START);
    }
    private class NavItemListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectNavItem(position);
        }
    }

    private void createLocationChooser(View view) {
        ListView drawerList = (ListView) view.findViewById(R.id.places_list);
        frag.placeAdapter = new SimpleCursorAdapter(getActivity(), R.layout.places_item,
                null, new String[] { Contract.Admin.COLUMN_NAME }, new int[] { R.id.place_name }, 0);
        drawerList.setAdapter(frag.placeAdapter);
        drawerList.setOnItemClickListener(new LocationClickListener());
        frag.getLoaderManager().initLoader(frag.PLACES_LOADER, null, frag);
        view.findViewById(R.id.places_toggle).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        View list = this.findViewById(R.id.places_list);
        boolean listOpen = VISIBLE == list.getVisibility();
        if (listOpen) {
            this.findViewById(R.id.bottom_border).setVisibility(INVISIBLE);
            this.findViewById(R.id.top_border).setVisibility(INVISIBLE);
            list.setVisibility(INVISIBLE);
            ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(
                    getResources().getDrawable(R.drawable.ic_map_grey), null, getResources().getDrawable(R.drawable.ic_expand_more_grey), null);
        } else {
            this.findViewById(R.id.bottom_border).setVisibility(VISIBLE);
            this.findViewById(R.id.top_border).setVisibility(VISIBLE);
            list.setVisibility(VISIBLE);
            ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(
                    getResources().getDrawable(R.drawable.ic_map_grey), null, getResources().getDrawable(R.drawable.ic_expand_less_grey), null);
        }

    }
    private class LocationClickListener implements ListView.OnItemClickListener {
        @Override public void onItemClick(AdapterView parent, View view, int pos, long id) {
            frag.setFeedToLocation((String) ((TextView) view).getText(), id);
            closeDrawer(GravityCompat.START);
        }
    }

    private void setTitle(CharSequence title) {
        ((TextView) this.findViewById(R.id.title)).setText(title);
    }

    private void setupDrawerToggle() {
        toggle = new ActionBarDrawerToggle(getActivity(), this, toolbar, R.string.open, R.string.close);
        setDrawerListener(toggle);
    }

    private FragmentActivity getActivity() {
        return (FragmentActivity) getContext();
    }

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
                holder = new ViewHolder();
                holder.tview = (TextView) view;
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
        }

        @Override public int getCount() {
            return items.size();
        }
        @Override public Object getItem(int position) {
            return items.get(position);
        }
        @Override public long getItemId(int position) {
            return position;
        }
    }
}
