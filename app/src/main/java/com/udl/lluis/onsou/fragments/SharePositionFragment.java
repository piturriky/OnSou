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
import android.view.View;
import android.widget.EditText;

import com.udl.lluis.onsou.FragmentsCommunicationInterface;
import com.udl.lluis.onsou.Globals;
import com.udl.lluis.onsou.R;
import com.udl.lluis.onsou.entities.MyDevice;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Lluís on 25/05/2015.
 */
public class SharePositionFragment extends DialogFragment {

    EditText editText;

    private String positionName;
    private Double latitude;
    private Double longitude;

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

        latitude = (Double) getArguments().getSerializable("latitude");
        longitude = (Double) getArguments().getSerializable("longitude");

        View layout = inflater.inflate(R.layout.share_position_dialog, null);

        editText = (EditText) layout.findViewById(R.id.positionname);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(layout)
                .setTitle("OnSou - Share Position")
                //.setMessage(message)
                .setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(SharePositionFragment.this);
                        positionName = editText.getText().toString();
                        communicationServerTask = new CommunicationServerTask();
                        communicationServerTask.execute();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(SharePositionFragment.this);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }


    /*private class CommunicationServerTask extends AsyncTask<Void,  Void, Boolean> {

        String s_url = "http://192.168.1.24:3000/locations";
        DataOutputStream printout;
        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                URL url = new URL(s_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                //conn.setReadTimeout(10000);
                //conn.setConnectTimeout(15000);
                //conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json");
                conn.setRequestProperty("Accept","application/json");

                conn.connect();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name",positionName);
                jsonObject.put("latitude",latitude);
                jsonObject.put("longitude",longitude);

                Log.e(Globals.TAG, jsonObject.toString());

                printout = new DataOutputStream(conn.getOutputStream());
                printout.writeBytes(URLEncoder.encode(jsonObject.toString(),"UTF-8"));
                printout.flush();
                printout.close();
            }catch (IOException e){
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(!result){
                Log.e(Globals.TAG, "ERROR COMMUNICATION SERVER NODEJS");
            }
        }

        @Override
        protected void onCancelled() {
            communicationServerTask = null;
            super.onCancelled();
        }
    }*/

    private class CommunicationServerTask extends AsyncTask<Void,  Void, String> {
        //http://hmkcode.com/android-send-json-data-to-server/
        String s_url = "http://192.168.1.24:3000/locations";

        @Override
        protected String doInBackground(Void... params) {
            InputStream inputStream = null;
            String result = "";
            try {

                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(s_url);

                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("name", positionName);
                jsonObject.accumulate("latitude", latitude);
                jsonObject.accumulate("longitude", longitude);
                jsonObject.accumulate("ownerId", MyDevice.getInstance().getId());

                // 4. convert JSONObject to JSON to String
                json = jsonObject.toString();

                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);

                // 6. set httpPost Entity
                httpPost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                // 8. Execute POST request to the given URL
                HttpResponse httpResponse = httpclient.execute(httpPost);

                // 9. receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // 10. convert inputstream to string
                if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";

            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }

            // 11. return result
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.e(Globals.TAG, result);

        }

        @Override
        protected void onCancelled() {
            communicationServerTask = null;
            super.onCancelled();
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
