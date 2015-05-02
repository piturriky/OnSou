package com.udl.lluis.onsou.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.udl.lluis.onsou.FragmentsCommunicationInterface;
import com.udl.lluis.onsou.R;
import com.udl.lluis.onsou.entities.Device;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Lluís on 17/03/2015.
 */
public class UserMapFragment extends Fragment implements Serializable {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "SECTION_TAG";

    //private FragmentActivity context;
    private FragmentsCommunicationInterface mCallback;

    private SharedPreferences myPreference;

    private GoogleMap mMap;
    private MapView mapView;

    private HashMap<Long,Device> devices;

    private static UserMapFragment fragment;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static UserMapFragment newInstance(int sectionNumber) {
        Log.e("------------>", "FRAGMENT NEW INSTANCE");
        fragment = new UserMapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public UserMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("------------>", "FRAGMENT ON CREATE");
        setRetainInstance(true);

        myPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        devices = new HashMap<Long,Device>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("------------>", "FRAGMENT ON CREATE VIEW");
        View inflatedView = inflater.inflate(R.layout.map_view_fragment, container, false);
        mapView = (MapView) inflatedView.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mMap = mapView.getMap();

        MapsInitializer.initialize(this.getActivity());

        return inflatedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("------------>", "FRAGMENT ONACTIVITYCREATED");
        //showDevicesInMap(mCallback.getDevices());
        if(mCallback.getFragment(0)==null)mCallback.setFragment(fragment,0);
        mCallback.startProcessGetDevices();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        Log.e("------------>", "FRAGMENT ONRESUME");
        initializeMap();
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //context = (FragmentActivity)activity;
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (FragmentsCommunicationInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentsCommunicationInterface");
        }
    }

    private void initializeMap() {
        // Check if we were successful in obtaining the map.
        if (mMap != null) {

            // Enable MyLocation Layer of Google Map
            mMap.setMyLocationEnabled(true);

            // set map type
            String myListPreference = myPreference.getString("map_type_list", "1");
            mMap.setMapType(Integer.parseInt(myListPreference));

            // Set 3D buildings
            boolean buildings = myPreference.getBoolean("buildings_map_checkbox",true);
            mMap.setBuildingsEnabled(buildings);

            // Try center map on my device
            Location myLoc = mMap.getMyLocation();
            if(myLoc != null){
                centerMapOnPosition(new LatLng(myLoc.getLatitude(),myLoc.getLongitude()));
            }else{
                LatLng myLatlon = mCallback.getDeviceLocation();
                if(myLatlon != null){
                    centerMapOnPosition(myLatlon);
                }
            }
        }
    }

    public void centerMapOnDevice(Device device){
        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(device.getPosition()));
        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
    }
    public void centerMapOnPosition(LatLng pos){
        if(pos == null){
            showToast("Null Position");
            return;
        }
        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        //mMap.addMarker(new MarkerOptions().position(pos).title("You are here!").snippet("Consider yourself located"));
    }

    public void centerMapOn(Long id){
        if(devices.containsKey(id)){
            Marker marker = devices.get(id).getMarker();
            LatLng pos = marker.getPosition();

            // Show the current location in Google Map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            // Zoom in the Google Map
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
            // Show info
            marker.showInfoWindow();
        } else{
            showToast("Error Id");
        }
    }

    public void showDevicesInMap(HashMap devices){
        Log.e("------------>", "SHOW DEVICES IN MAP");
        /*Drawable dd = getResources().getDrawable(R.drawable.ic_action_person);
        // Get the color of the icon depending on system state
        int iconColor = android.graphics.Color.BLACK;
        iconColor = android.graphics.Color.RED;
        // Set the correct new color
        dd.setColorFilter( iconColor, PorterDuff.Mode.MULTIPLY );
        Bitmap mDotMarkerBitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mDotMarkerBitmap);
        dd.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
        dd.draw(canvas);
        mMap.addMarker(new MarkerOptions()
                .position(getDeviceLocation())
                .title("Test")
                .snippet("Test")
                .icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)));
        */

        mMap.clear();
        this.devices = devices;

        for(Device d : new ArrayList<Device>(devices.values())){
            if(d.isOnline()){
                MarkerOptions marker = new MarkerOptions()
                        .position(d.getPosition())
                        .title(d.getName())
                        .snippet(d.getPosition().latitude + " :: " + d.getPosition().longitude);
                if(d.isFriend())marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_person_blue));
                else marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_person_grey));

                /*if(deviceMarkers.containsKey(d.getId())){
                    deviceMarkers.get(d.getId()).setPosition(d.getPosition());
                }else{
                    deviceMarkers.put(d.getId(),mMap.addMarker(marker));
                }*/
                d.setMarker(mMap.addMarker(marker));
                //mMap.addMarker(marker);
            }else{
                /*if(deviceMarkers.containsKey(d.getId())){
                    deviceMarkers.get(d.getId()).remove();
                }*/
            }
        }
    }

    private void showToast(CharSequence text){
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    public String getTest(){
        return "TEESTE";
    }



}
