package com.udl.lluis.onsou.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import java.util.Map;


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

    private LocationManager locManager;
    private LocationListener locListener;

    private HashMap<Long,Marker> deviceMarkers;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static UserMapFragment newInstance(int sectionNumber) {
        Log.e("------------>", "FRAGMENT NEW INSTANCE");
        UserMapFragment fragment = new UserMapFragment();
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
        deviceMarkers = new HashMap<>();

        //Obtenemos una referencia al LocationManager
        locManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Send to server new location
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Toast.makeText(getActivity(), "Provider Status: " + status, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(getActivity(), "Provider ON", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(getActivity(), "Provider OFF", Toast.LENGTH_SHORT).show();
            }
        };

        if ( !locManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            mCallback.showDialogFragment(4,null);
        }


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
        initializeMap();

        return inflatedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("------------>", "FRAGMENT ONACTIVITYCREATED");
        //showDevicesInMap(mCallback.getDevices());
        mCallback.startProcessGetDevices();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        Log.e("------------>", "FRAGMENT ONRESUME");
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

            //Nos registramos para recibir actualizaciones de la posición
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locListener);

            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title("Marker - LongClick"));
                }
            });

            // Try center map on my device
            Location myLoc = mMap.getMyLocation();
            if(myLoc != null){
                centerMapOnPosition(new LatLng(myLoc.getLatitude(),myLoc.getLongitude()));
            }else{
                LatLng myLatlon = getDeviceLocation();
                if(myLatlon != null){
                    centerMapOnPosition(myLatlon);
                }
            }

        }
    }

    public void centerMapOnDevice(Device device){
        //LatLng pos = getDeviceLocation();// new LatLng(loc.getLatitude(), loc.getLongitude());
        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(device.getPosition()));
        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        //mMap.addMarker(new MarkerOptions().position(pos).title("You are here!").snippet("Consider yourself located"));
    }
    private void centerMapOnPosition(LatLng pos){
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
        if(deviceMarkers.containsKey(id)){
            Marker marker = deviceMarkers.get(id);
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

    // Retorna la posició del usuari
    private LatLng getDeviceLocation(){
        if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            // Obtenemos la última posición conocida
            Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(loc != null){
                return new LatLng(loc.getLatitude(), loc.getLongitude());
            }
        }
        Toast.makeText(getActivity(), "Provider Disabled", Toast.LENGTH_SHORT).show();
        return null;
    }

    public void showDevicesInMap(Map devices){
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

        for(Device d : new ArrayList<Device>(devices.values())){
            if(d.isOnline()){
                MarkerOptions marker = new MarkerOptions()
                        .position(d.getPosition())
                        .title(d.getName())
                        .snippet(d.getPosition().latitude + " :: " + d.getPosition().longitude);
                if(d.isFriend())marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_person_blue));
                else marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_person_grey));
                if(deviceMarkers.containsKey(d.getId())){
                    deviceMarkers.get(d.getId()).setPosition(d.getPosition());
                }else{
                    deviceMarkers.put(d.getId(),mMap.addMarker(marker));
                }
            }else{
                if(deviceMarkers.containsKey(d.getId())){
                    deviceMarkers.get(d.getId()).remove();
                }
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
