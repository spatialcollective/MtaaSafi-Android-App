package com.sc.mtaasafi.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

/**
 * Created by Agree on 9/26/2014.
 */
public class AlertDialogFragment extends android.support.v4.app.DialogFragment {
    String alertMessage, positiveText, negativeText;
    int alertType;
    public static final int UPDATE_FAILED = 0,
                            LOCATION_FAILED = 1,
                            CONNECTION_FAILED = 2,
                            UPLOAD_FAILED = 3;
    public static final String ALERT_KEY = "alert";
    public AlertDialogFragment(){
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final MainActivity mainActivity = (MainActivity) getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        Bundle arguments = getArguments();
        if(getArguments() != null)
            alertType = arguments.getInt(ALERT_KEY);
        switch (alertType) {
            case UPDATE_FAILED:
                builder.setMessage("Sorry! The feed failed to update")
                        .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // accept failure, and therefore defeat.
                            }
                        })
                        .setNegativeButton("Try again", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // TODO: method to tell mainActivity to try to update the feed.
                            }
                        });
                break;
            case LOCATION_FAILED:
                builder.setMessage("We need your location to use the app!")
                        .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // accept failure, and therefore defeat.
                            }
                        })
                        .setNegativeButton("Enable location", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        });
                break;
            case CONNECTION_FAILED:
                builder.setMessage("Uh-oh! Looks like there's a problem with your network connection")
                        .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // accept failure, and therefore defeat.
                            }
                        })
                        .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}
                        });
                break;
            case UPLOAD_FAILED:
                builder.setMessage("Sorry! The report upload was interrupted")
                        .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // accept failure, and therefore defeat.
                            }
                        })
                        .setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // TODO: method to tell mainactivity to upload report
                            }
                        });
                break;


        }
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
