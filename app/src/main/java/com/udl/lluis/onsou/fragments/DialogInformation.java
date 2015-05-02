package com.udl.lluis.onsou.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.lluis.onsou.backend.registration.Registration;
import com.lluis.onsou.backend.registration.model.Result;
import com.udl.lluis.onsou.FragmentsCommunicationInterface;
import com.udl.lluis.onsou.R;
import com.udl.lluis.onsou.entities.MyDevice;

import java.io.IOException;

/**
 * Created by Lluís on 21/03/2015.
 */
public class DialogInformation extends DialogFragment {

    private String message;
    private String type;

    private String senderNotification;

    private ResponseAddDeviceTask responseAddDeviceTask;

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

        message = (String) getArguments().getSerializable("message");
        type = (String) getArguments().getSerializable("type");


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.manage_friends_dialog, null))
                .setTitle("OnSou - Information")
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(DialogInformation.this);
                        if(type.equals("addFriend")){

                        }
                    }
                });
        if(type.equals("addFriend")){
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onDialogNegativeClick(DialogInformation.this);
                }
            });
        }
        // Create the AlertDialog object and return it
        return builder.create();
    }


    private class ResponseAddDeviceTask extends AsyncTask<String,  Void, Result> {
        private Registration regService = null;
        @Override
        protected Result doInBackground(String... params) {
            Result res;
            if (regService == null) {
                Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                        .setRootUrl("https://tranquil-well-88613.appspot.com/_ah/api/");
                regService = builder.build();
            }
            try {
                res = regService.acceptFriend(MyDevice.getInstance().getId(),(String)params[0]).execute();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return res;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            responseAddDeviceTask = null;
            Bundle bundle = new Bundle();
            if(result == null || !result.getStatus()){
                bundle.putSerializable("message",getString(R.string.internal_error));
                bundle.putSerializable("type","information");
                mListener.showDialogFragment(5,bundle);
            }
        }

        @Override
        protected void onCancelled() {
            responseAddDeviceTask = null;
            super.onCancelled();
        }
    }
}
