package com.udl.lluis.onsou.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
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
import com.udl.lluis.onsou.Globals;
import com.udl.lluis.onsou.R;
import com.udl.lluis.onsou.entities.Device;
import com.udl.lluis.onsou.entities.MyDevice;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


/**
 * Created by Lluís on 17/03/2015.
 */
public class UserMapFragment extends Fragment implements Serializable {

    private FragmentsCommunicationInterface mCallback;

    private SharedPreferences myPreference;

    private GoogleMap mMap;
    private MapView mapView;

    private HashMap<Long,Device> devices;
    private ArrayList<Place> placesList;
    private ArrayList<Place> sharedPlacesList;

    private static UserMapFragment fragment;

    private ShowBarsTask showBarsTask;
    private ShowSharedLocationsTask showSharedLocationsTask;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static UserMapFragment newInstance(int sectionNumber) {
        Log.e(Globals.TAG, "FRAGMENT NEW INSTANCE");
        fragment = new UserMapFragment();
        Bundle args = new Bundle();
        args.putInt(Globals.ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public UserMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(Globals.TAG, "FRAGMENT ON CREATE");
        setRetainInstance(true);

        myPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        devices = new HashMap<Long,Device>();
        placesList = new ArrayList<Place>();
        sharedPlacesList = new ArrayList<Place>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(Globals.TAG, "FRAGMENT ON CREATE VIEW");
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
        Log.e(Globals.TAG, "FRAGMENT ONACTIVITYCREATED");
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
        Log.e(Globals.TAG, "FRAGMENT ONRESUME");
        initializeMap();
        if(devices!=null)showDevicesInMap(devices);
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

            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    for(Place place : sharedPlacesList) {
                        if(place.getOwnerId() != MyDevice.getInstance().getId()) continue;
                        if(Math.abs(place.getLatitude() - latLng.latitude) < 0.0001 && Math.abs(place.getLongitude() - latLng.longitude) < 0.0001) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("markerName",place.getName());
                            bundle.putSerializable("markerId",place.getId());
                            //Log.e(Globals.TAG, "markerId"+Long.toString(place.getId()));
                            mCallback.showDialogFragment(7,bundle);
                            return;
                        }
                    }
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("latitude",latLng.latitude);
                    bundle.putSerializable("longitude",latLng.longitude);
                    mCallback.showDialogFragment(6,bundle);
                }
            });

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {

                }
            });

            // Try center map on my device
            Location myLoc = mMap.getMyLocation();
            LatLng myLatlon;
            if(myLoc != null){
                myLatlon = new LatLng(myLoc.getLatitude(),myLoc.getLongitude());
                centerMapOnPosition(myLatlon);
            }else{
                myLatlon = mCallback.getDeviceLocation();
                if(myLatlon != null){
                    centerMapOnPosition(myLatlon);
                }
            }

            // Show shared locations
            boolean locations = myPreference.getBoolean("show_shared_locations",false);
            sharedPlacesList.clear();
            if(locations){
                String url = "http://192.168.1.24:3000/locations";
                showSharedLocationsTask = new ShowSharedLocationsTask();
                showSharedLocationsTask.execute(url);
            }

            // Show restaurants
            boolean bars = myPreference.getBoolean("show_bars",false);
            placesList.clear();
            if(bars && myLatlon != null){
                String location = Double.toString(myLatlon.latitude) + "," + Double.toString(myLatlon.longitude);
                String radius = myPreference.getString("places_dist_list","1");
                String types = "bar|cafe|food|restaurant";
                String key = getString(R.string.server_key);
                String url = String.format(Globals.URL_BARS,location,radius,types,key);

                showBarsTask = new ShowBarsTask();
                showBarsTask.execute(url );
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
        Log.e(Globals.TAG, "SHOW DEVICES IN MAP");
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
            showPlaces();
            showSharedPlaces();
        }
    }

    private void showPlaces(){
        for(Place place : placesList){
            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(place.getLatitude(),place.getLongitude()))
                    .snippet(place.getName());
            if(place.getTypes().contains("restaurant")){
                marker.title(getString(R.string.restaurant))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cafe));
            }else{
                marker.title(getString(R.string.cafe))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant));
            }
            mMap.addMarker(marker);
        }
    }

    private void showSharedPlaces(){
        for(Place place : sharedPlacesList){
            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(place.getLatitude(),place.getLongitude()))
                    .snippet(place.getName())
                    .title(getString(R.string.sharedL));
            if(place.getOwnerId() == MyDevice.getInstance().getId()){
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_star));
            }else{
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.star));
            }
            mMap.addMarker(marker);
        }
    }

    private void showToast(CharSequence text){
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }


    private class ShowBarsTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {

            try {
                return makeCall((String) params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            showBarsTask = null;

            if(result == null){

            }else{
                placesList = parseJSON(result);
                showPlaces();
            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            showBarsTask = null;
        }
    }

    private class ShowSharedLocationsTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {

            try {
                return makeCall((String) params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            showSharedLocationsTask = null;

            if(result == null){

            }else{
                Log.e(Globals.TAG, result);
                sharedPlacesList = parseJSON(result);
                showSharedPlaces();
            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            showSharedLocationsTask = null;
        }
    }

    private String makeCall(String resourceUrl) throws IOException {
        InputStream is = null;

        try{
            URL url = new URL(resourceUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setDoInput(true);

            conn.connect();
            int response = conn.getResponseCode();

            is = conn.getInputStream();

            String res = readIt(is);
            return res;
        }finally {
            if(is != null){
                is.close();
            }
        }
    }

    private String readIt(InputStream is) throws IOException {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private ArrayList <Place> parseJSON(final String response){ //TODO next_page_token
        ArrayList temp = new ArrayList();
        try{
            JSONObject jsonObject = new JSONObject(response);

            if(jsonObject.has("results")){
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for(int i = 0; i < jsonArray.length(); i++){
                    Place place = new Place();
                    if(jsonArray.getJSONObject(i).has("name")){
                        place.setName(jsonArray.getJSONObject(i).optString("name"));
                        if(jsonArray.getJSONObject(i).has("geometry")){
                            if(jsonArray.getJSONObject(i).getJSONObject("geometry").has("location")){
                                if(jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").has("lat")){
                                    place.setLatitude(jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").optDouble("lat"));
                                }
                                if(jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").has("lng")){
                                    place.setLongitude(jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").optDouble("lng"));
                                }
                            }
                            if(jsonArray.getJSONObject(i).has("types")){
                                JSONArray typesArray = jsonArray.getJSONObject(i).getJSONArray("types");
                                for(int j = 0; j < typesArray.length(); j++){
                                    place.addTypes(typesArray.getString(j));
                                }
                            }
                        }
                    }
                    if(place.isComplete()){
                        temp.add(place);
                    }
                }
            }else if(jsonObject.has("locations")){

                JSONArray jsonArray = jsonObject.getJSONArray("locations");
                for(int i = 0; i < jsonArray.length(); i++){

                    Place place = new Place();
                    if(jsonArray.getJSONObject(i).has("name")){
                        place.setName(jsonArray.getJSONObject(i).optString("name"));
                    }
                    if(jsonArray.getJSONObject(i).has("latitude")){
                        place.setLatitude(jsonArray.getJSONObject(i).optDouble("latitude"));
                    }
                    if(jsonArray.getJSONObject(i).has("longitude")){
                        place.setLongitude(jsonArray.getJSONObject(i).optDouble("longitude"));
                    }
                    if(jsonArray.getJSONObject(i).has("_id")){
                        place.setId(jsonArray.getJSONObject(i).optString("_id"));
                        Log.e(Globals.TAG, Long.toString(jsonArray.getJSONObject(i).optLong("_id")));
                    }
                    if(jsonArray.getJSONObject(i).has("ownerId")){
                        place.setOwnerId(jsonArray.getJSONObject(i).optLong("ownerId"));
                    }
                    if(place.isComplete_()){
                        temp.add(place);
                    }
                }
            }
        }catch (Exception e){
            return new ArrayList<>();
        }
        return temp;
    }

    private class Place{
        String name;
        Double latitude;
        Double longitude;

        String id;
        Long ownerId;

        ArrayList <String> types = new ArrayList<>();


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public ArrayList <String> getTypes() {
            return types;
        }

        public void setTypes(ArrayList <String> types) {
            this.types = types;
        }

        public void addTypes(String s){
            types.add(s);
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Long getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(Long ownerId) {
            this.ownerId = ownerId;
        }

        public boolean isComplete(){
            return this.name != null && !this.name.isEmpty() && !this.types.isEmpty() && this.latitude != null && this.longitude != null;
        }

        public boolean isComplete_(){
            return this.name != null && !this.name.isEmpty() && this.id != null && this.ownerId != null && this.latitude != null && this.longitude != null;
        }
    }
}
