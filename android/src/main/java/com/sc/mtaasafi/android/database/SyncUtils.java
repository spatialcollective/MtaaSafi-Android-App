package com.sc.mtaasafi.android.database;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class SyncUtils {
    private static final long SYNC_FREQUENCY = 60 * 60; // 1 hour
    private static final String CONTENT_AUTHORITY = Contract.CONTENT_AUTHORITY;
    private static final String PREF_SETUP_COMPLETE = "setup_complete";

    // Create an entry for this application in the system account list, if it isn't already there.
    public static void CreateSyncAccount(Context context) {
        Log.i("SyncUtils", "creating account");
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = AuthenticatorService.GetAccount();
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
       if (accountManager.addAccountExplicitly(account, null, null)) {
        Log.i("SyncUtils", "setting up account sync...");
           // Inform the system that this account supports sync
           ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
           // Inform the system that this account is eligible for auto sync when the network is up
           ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
           // Recommend a schedule for automatic synchronization. The system may modify this based
           // on other scheduled syncs and network utilization.
           ContentResolver.addPeriodicSync(
                   account, CONTENT_AUTHORITY, new Bundle(),SYNC_FREQUENCY);
           newAccount = true;
       }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount || !setupComplete) {
            Log.i("SyncUtils", "Triggering Refresh");
            TriggerRefresh();
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
    }

    // Helper method to trigger an immediate sync ("refresh").
    // Typically, this means the user has pressed the "refresh" button.
    public static void TriggerRefresh() {
        Log.i("SyncUtils", "Requesting Sync");
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW! Omit for background syncs
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                AuthenticatorService.GetAccount(),      // Sync account
                Contract.CONTENT_AUTHORITY, // Content authority
                b);                                      // Extras
    }
}
