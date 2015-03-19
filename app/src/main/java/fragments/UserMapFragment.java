package fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.udl.lluis.onsou.R;


/**
 * Created by Lluís on 17/03/2015.
 */
public class UserMapFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "SECTION_TAG";

    private FragmentActivity context;
    private GoogleMap mMap;
    private MapView mapView;

    private LocationManager locManager;
    private LocationListener locListener;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static UserMapFragment newInstance(int sectionNumber) {
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

        //Obtenemos una referencia al LocationManager
        locManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Send to server new location
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Toast.makeText(context, "Provider Status: " + status, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(context, "Provider ON", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(context, "Provider OFF", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initializeMap() {
        // Check if we were successful in obtaining the map.
        if (mMap != null) {

            // Enable MyLocation Layer of Google Map
            mMap.setMyLocationEnabled(true);

            // set map type
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            // Set 3D buildings
            mMap.setBuildingsEnabled(true);

            //Nos registramos para recibir actualizaciones de la posición
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locListener);

            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title("Marker - LongClick"));
                }
            });

            centerMapOnDevice();
        }
    }

    private void centerMapOnDevice(){
        Location loc = getDeviceLocation();
        LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        //mMap.addMarker(new MarkerOptions().position(pos).title("You are here!").snippet("Consider yourself located"));
    }

    // Retorna la posició del usuari
    private Location getDeviceLocation(){
        if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            // Obtenemos la última posición conocida
            Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(loc != null){
                return loc;
            }
        }
        Toast.makeText(context, "Provider Disabled", Toast.LENGTH_SHORT).show();
        return null;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = (FragmentActivity)activity;
    }
}
