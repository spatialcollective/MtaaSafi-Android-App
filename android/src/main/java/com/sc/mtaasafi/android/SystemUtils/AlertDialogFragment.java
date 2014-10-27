package com.sc.mtaasafi.android.SystemUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.sc.mtaasafi.android.feed.MainActivity;

/**
 * Created by Agree on 9/26/2014.
 */
public class AlertDialogFragment extends android.support.v4.app.DialogFragment {
    public interface AlertDialogListener{
        void onAlertButtonPressed(int eventKey);
    }
    String alertMessage, positiveText, negativeText;
    int alertType;
    public static final int UPDATE_FAILED = 0,
                            LOCATION_FAILED = 1,
                            CONNECTION_FAILED = 2,
                            UPLOAD_FAILED = 3,
                            SAVED_REPORTS = 4,
                            GPLAY_UPDATE = 5,
                            GPLAY_MISSING = 6,
                            GPLAY_DISABLED = 7,
                            GPLAY_INVALID = 8,

                            RE_FETCH_FEED = 100,
                            SEND_SAVED_REPORTS = 200,
                            RE_UPLOAD_POST = 300,
                            UPDATE_GPLAY = 400,
                            INSTALL_GPLAY = 500,
                            ENABLE_GPLAY = 600;

    public static final String ALERT_KEY = "alert";

    private AlertDialogListener listener;
    public AlertDialogFragment(){
    }
    public void setAlertDialogListener(AlertDialogListener adl){
        this.listener = adl;
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
                                listener.onAlertButtonPressed(RE_FETCH_FEED);
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
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            }
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
                                listener.onAlertButtonPressed(RE_UPLOAD_POST);
                            }
                        });
                break;
            case SAVED_REPORTS:
                builder.setMessage("You have saved reports left over! Send them now?")
                        .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // accept failure, and therefore defeat.
                            }
                        })
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                listener.onAlertButtonPressed(SEND_SAVED_REPORTS);
                            }
                        });
                break;
            case GPLAY_UPDATE:
                builder.setMessage("You need to update Google Play Services to use Mtaa Safi!")
                        .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // accept failure, and therefore defeat.
                            }
                        })
                        .setNegativeButton("Update", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                listener.onAlertButtonPressed(UPDATE_GPLAY);
                            }
                        });
                break;
            case GPLAY_DISABLED:
                builder.setMessage("Enable Google Play Services to use Mtaa Safi!")
                        .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // accept failure, and therefore defeat.
                            }
                        })
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                listener.onAlertButtonPressed(ENABLE_GPLAY);
                            }
                        });
                break;
            case GPLAY_MISSING:
                builder.setMessage("Install Google Play Services to use Mtaa Safi!")
                        .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // accept failure, and therefore defeat.
                            }
                        })
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                listener.onAlertButtonPressed(INSTALL_GPLAY);
                            }
                        });
                break;
            case GPLAY_INVALID:
                builder.setMessage("Mtaa Safi detected invalid Google Play Services on this phone")
                        .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // accept failure, and therefore defeat.
                            }
                        })
                        .setNegativeButton("Reinstall", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                listener.onAlertButtonPressed(INSTALL_GPLAY);
                            }
                        });
                break;


        }
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
