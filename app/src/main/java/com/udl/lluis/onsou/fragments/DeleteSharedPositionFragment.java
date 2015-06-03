package com.udl.lluis.onsou.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;

import com.udl.lluis.onsou.FragmentsCommunicationInterface;
import com.udl.lluis.onsou.Globals;
import com.udl.lluis.onsou.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Lluís on 27/05/2015.
 */
public class DeleteSharedPositionFragment extends DialogFragment {

    private String message;
    private String idLocation;

    private String senderNotification;

    private CommunicationServerTask communicationServerTask;

    /* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
//    public interface ManageFriendsDialogListener {
//        public void onDialogPositiveClick(DialogFragment dialog);
//        public void onDialogNegativeClick(DialogFragment dialog);
//    }

    // Use this instance of the interface to deliver action events
    FragmentsCommunicationInterface mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (FragmentsCommunicationInterface) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentsCommunicationInterface");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        message = String.format(getString(R.string.deletePositionMessage),(String) getArguments().getSerializable("markerName"));
        idLocation = (String) getArguments().getSerializable("markerId");


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.manage_friends_dialog, null))
            .setTitle("OnSou - Delete Shared Position")
            .setMessage(message)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onDialogPositiveClick(DeleteSharedPositionFragment.this);
                    communicationServerTask = new CommunicationServerTask();
                    communicationServerTask.execute(idLocation);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onDialogNegativeClick(DeleteSharedPositionFragment.this);
                }
            });

        // Create the AlertDialog object and return it
        return builder.create();
    }


    private class CommunicationServerTask extends AsyncTask<String,  Void, Boolean> {
        //http://hmkcode.com/android-send-json-data-to-server/
        String s_url = "http://192.168.1.24:3000/locations/";

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String url_ = s_url+(String)params[0]+"/delete";
                Log.e(Globals.TAG, url_);
                URL url = new URL(url_);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type","application/json");
                conn.setDoInput(true);

                conn.connect();
                int response = conn.getResponseCode();
                Log.e(Globals.TAG,"response" + Integer.toString(response));

            }catch (IOException e){
                return false;
            }

                return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(!result){
                Log.e(Globals.TAG, "Error delete ");
            }

        }

        @Override
        protected void onCancelled() {
            communicationServerTask = null;
            super.onCancelled();
        }
    }
}
