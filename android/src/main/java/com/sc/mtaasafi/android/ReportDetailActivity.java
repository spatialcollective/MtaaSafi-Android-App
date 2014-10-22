package com.sc.mtaasafi.android;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.sc.mtaasafi.android.R;

public class ReportDetailActivity extends ActionBarActivity {

    private ReportDetailFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment = new ReportDetailFragment();
        getSupportFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, mFragment)
            // .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
//        if (reportDetailReport != null)
//            reportDetailReport.saveState(REPORT_DETAIL_KEY, bundle);
//        bundle.putBoolean(HAS_REPORT_DETAIL_KEY, reportDetailReport != null);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        if (savedInstanceState.getBoolean(HAS_REPORT_DETAIL_KEY))
//            reportDetailReport = new Report(REPORT_DETAIL_KEY, savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.report_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
