package com.udl.lluis.onsou.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.lluis.onsou.backend.registration.Registration;
import com.lluis.onsou.backend.registration.model.Result;
import com.udl.lluis.onsou.FragmentsCommunicationInterface;
import com.udl.lluis.onsou.R;
import com.udl.lluis.onsou.entities.MyDevice;

/**
 * Created by Lluís on 18/03/2015.
 */
public  class AddDeviceDialogFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */

    // Use this instance of the interface to deliver action events
    private FragmentsCommunicationInterface mListener;

    private AddDeviceTask addDeviceTask = null;

    private EditText usernameAddDevice;

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
        View dialogLayout = inflater.inflate(R.layout.add_device_dialog, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogLayout)
                .setTitle(R.string.addDevice)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(AddDeviceDialogFragment.this);

                        String username = usernameAddDevice.getText().toString();
                        if (TextUtils.isEmpty(username)) {
                            usernameAddDevice.setError(getString(R.string.error_field_required));
                            usernameAddDevice.requestFocus();
                        }else{
                            addDeviceTask = new AddDeviceTask();
                            addDeviceTask.execute(username);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(AddDeviceDialogFragment.this);
                    }
                });
        usernameAddDevice = (EditText) dialogLayout.findViewById(R.id.usernameAddDevice);
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private class AddDeviceTask extends AsyncTask<String,  Void, Result>{
        private Registration regService = null;
        @Override
        protected Result doInBackground(String... params) {
            if (regService == null) {
                Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                        .setRootUrl("https://tranquil-well-88613.appspot.com/_ah/api/");
                regService = builder.build();
            }
            Result res = regService.addDevice(MyDevice.getInstance().getId(),(String)params[0]).execute();
            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            addDeviceTask = null;
        }

        @Override
        protected void onCancelled() {
            addDeviceTask = null;
            super.onCancelled();
        }
    }
}
