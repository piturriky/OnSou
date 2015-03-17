package com.udl.lluis.onsou;

import android.app.DialogFragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivityMain extends FragmentActivity {

    private LocationManager locManager;
    private LocationListener locListener;
    private MapsActivityMain mActivity = this;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_main);

        //Obtenemos una referencia al LocationManager
        locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locListener = buildLocationListener();

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
                //Nos registramos para recibir actualizaciones de la posición
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locListener);

//              mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                @Override
//                public void onMapClick(LatLng latLng) {
//                    mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title("Marker - SimpleClick"));
//                }
//              });
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title("Marker - LongClick"));
                    }
                });

                // Set 3D buildings
                mMap.setBuildingsEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps_activity, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.MnuOpc1:
                Toast.makeText(mActivity, "Opcion 1 pulsada!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.MnuOpc2:
                Toast.makeText(mActivity, "Opcion 2 pulsada!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.MnuOpc3:
                Toast.makeText(mActivity, "Opcion 3 pulsada!", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpMap() {
        //Mostramos la última posición conocida
        //mMap.addMarker(new MarkerOptions().position(new LatLng(loc.getLatitude(), loc.getLongitude())).title("You"));

        // Enable MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);

        // Obtenemos la última posición conocida
        Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
        // set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));

        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        //mMap.addMarker(new MarkerOptions().position(pos).title("You are here!").snippet("Consider yourself located"));
    }

    private LocationListener buildLocationListener(){
        LocationListener lcl = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setUpMap();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Toast.makeText(mActivity, "Provider Status: " + status, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(mActivity, "Provider ON", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(mActivity, "Provider OFF", Toast.LENGTH_SHORT).show();
            }
        };
        return lcl;
    }
}
