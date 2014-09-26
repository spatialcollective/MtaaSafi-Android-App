package com.sc.mtaasafi.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Agree on 9/26/2014.
 */
public class AlertDialogFragment extends android.support.v4.app.DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_update_failed)
                .setPositiveButton(R.string.dialog_update_failed_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // accept failure, and therefore defeat.
                    }
                })
                .setNegativeButton(R.string.dialog_update_failed_retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // try to fetch posts again
                        MainActivity mainActivity = (MainActivity) getActivity();
                        mainActivity.updateFeed();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
