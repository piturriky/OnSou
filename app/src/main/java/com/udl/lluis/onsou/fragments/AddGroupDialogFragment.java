package com.udl.lluis.onsou.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.udl.lluis.onsou.FragmentsCommunicationInterface;
import com.udl.lluis.onsou.R;
import com.udl.lluis.onsou.entities.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lluís on 21/03/2015.
 */
public class AddGroupDialogFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
//    public interface ManageFriendsDialogListener {
//        public void onDialogPositiveClick(DialogFragment dialog);
//        public void onDialogNegativeClick(DialogFragment dialog);
//    }

    // Use this instance of the interface to deliver action events
    FragmentsCommunicationInterface mListener;
    AlertDialog alertDialog;
    Button buttonNo;
    EditText groupNameView;
    View view;

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

        final List mSelectedItems = new ArrayList();  // Where we track the selected items
        List<Device> friendsList = new ArrayList<Device>(mListener.getDevices().values());
        int sizeArray = 0;
        for(Device d : friendsList){
            sizeArray ++;
        }
        String[] friendsArray = new String[sizeArray];

//        for(Device d : friendsList){
//            if (d.isFriend()){
//                sizeArray
//            }
//        }
        for(int i = 0; i< sizeArray ; i++){
            friendsArray[i] = friendsList.get(i).getName();
        }

        MyListAdapter adapter = new MyListAdapter(getActivity(),R.layout.manage_friends_dialog,friendsList);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view = inflater.inflate(R.layout.add_group_dialog, null))
                .setTitle("New Group")
                .setMultiChoiceItems(friendsArray, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedItems.add(which);
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                }
                            }
                        })
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

//                        if(groupNameView.getText().length() == 0){
//                            return;
//                        }
                        mListener.onDialogPositiveClick(AddGroupDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(AddGroupDialogFragment.this);
                    }
                });

        // Create the AlertDialog object and return it
        alertDialog = builder.create();

//        buttonNo = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//        groupNameView = (EditText) view.findViewById(R.id.groupname);
//
//        buttonNo.setEnabled(false);
//        groupNameView.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if(groupNameView.getText().length() == 0) buttonNo.setEnabled(false);
//                else buttonNo.setEnabled(true);
//                return false;
//            }
//        });

        return alertDialog;
    }

    public class MyListAdapter extends ArrayAdapter<Device> {

        public MyListAdapter(Context context, int resource, List<Device> objects) {
            super(context, resource, objects);
        }

        @Override
        public Device getItem(int position) {
            return super.getItem(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(getActivity());
            textView.setText("TEST");
            return textView;
        }

    }
}