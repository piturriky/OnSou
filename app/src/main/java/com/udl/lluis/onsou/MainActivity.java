package com.udl.lluis.onsou;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.lluis.onsou.backend.registration.Registration;
import com.lluis.onsou.backend.registration.model.Result;
import com.udl.lluis.onsou.entities.Device;
import com.udl.lluis.onsou.entities.Group;
import com.udl.lluis.onsou.entities.MyDevice;
import com.udl.lluis.onsou.fragments.AddDeviceDialogFragment;
import com.udl.lluis.onsou.fragments.AddGroupDialogFragment;
import com.udl.lluis.onsou.fragments.DialogInformation;
import com.udl.lluis.onsou.fragments.FriendsFragment;
import com.udl.lluis.onsou.fragments.GroupsFragment;
import com.udl.lluis.onsou.fragments.ManageFriendsDialogFragment;
import com.udl.lluis.onsou.fragments.UserMapFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener, FragmentsCommunicationInterface {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    private HashMap<Integer,Fragment> fragmentsMap = new HashMap<Integer, Fragment>();

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private MainActivity act = this;
    private static Context context;

    private ActionBar actionBar;
    private Menu optionsMenu;

    private LocationManager locManager;
    private LocationListener locListener;

    private Handler handler;
    private GetDevicesThread thread;
    private SendMyPositionTask sendMyPositionTask = null;
    private GetDevicesTask getDevicesTask = null;
    private LogoutTask logoutTask = null;


    // Devices
    private static HashMap<Long,Device> devices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        Log.e("------------>", "ACTIVITY ONCREATE");
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null){
            devices = (HashMap) savedInstanceState.getSerializable("devicesMap");
            //fragmentsMap = (HashMap)savedInstanceState.getSerializable("fragmentMap");
        }

        if(getIntent().getExtras() != null){
            if(getIntent().getExtras().getBoolean("fromNotification")){
                Bundle bundle = new Bundle();
                bundle.putSerializable("message",String.format(getString(R.string.addFriend),getIntent().getExtras().getString("senderNotification")));
                bundle.putSerializable("senderNotification",getIntent().getExtras().getString("senderNotification"));
                bundle.putSerializable("type","addFriend");
                showDialogFragment(5,bundle);
            }
        }

        // Set up the action bar.
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager); // Agafa el fragment principal
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        this.thread = new GetDevicesThread();
        this.handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 0:
                        devices.clear();
                        devices = (HashMap<Long, Device>) msg.obj;
                        ((UserMapFragment) getFragment(0)).showDevicesInMap(devices);
                        ((FriendsFragment) getFragment(1)).showDevices(devices);
                        break;
                }
                super.handleMessage(msg);
            }
        };


        //Obtenemos una referencia al LocationManager
        locManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Send to server new location
                sendMyPositionTask = new SendMyPositionTask();
                sendMyPositionTask.execute(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                showToast("Provider " + provider + " Status: " + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                showToast( "Provider "+ provider +" ON");
            }

            @Override
            public void onProviderDisabled(String provider) {
                showToast("Provider "+ provider +" OFF");
            }
        };

        if ( !locManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            showDialogFragment(4, null);
        }

        devices = new HashMap<Long,Device>();
        //getDevicesFromServer();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e("------------>", "ACTIVITY ONSAVEINSTANCESTATE");
        outState.putSerializable("devicesMap",devices);
//        outState.putSerializable("fragmentsMap", fragmentsMap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO thread.run();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //TODO thread.goStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Nos registramos para recibir actualizaciones de la posición
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locListener);
        Log.e("------------>", "ACTIVITY ONRESUME");
    }

    @Override
    protected void onDestroy() {
        locManager.removeUpdates(locListener);
        super.onDestroy();
    }

    // Send petition to server getDevices()
    //      this return available devices depending params configured
//    private void getDevicesFromServer(){
//
//        devices.clear();
//
//        // FILL devicesMap from download data
//        for(Device d : getSimulatedDevices()){
//            devices.put(d.getId(),d);
//        }
//    }

    private List<Device> getSimulatedDevices(){
        List<Device> list = new ArrayList<Device>(){{
            add(new Device(new Long(1000),"Simulation Friend 1",new LatLng(41.69,0.64),true,true));
            add(new Device(new Long(2000),"Simulation No Friend 2",new LatLng(41.96,0.65),false,true));
            add(new Device(new Long(3000),"Simulation Friend 3",new LatLng(41.716,0.66),true,true));
            add(new Device(new Long(4000),"Simulation No Online Friend 4",new LatLng(41.616,0.68),true,false));
        }};
        return list;
    }

    private List <Group> getSimulatedGroups(){
        final List devices = getSimulatedDevices();

        Group g1 = new Group("Simulated Group 1", new Color());

        List<Group> list = new ArrayList<Group>(){{
            add(new Group("Simulated Group 1", new Color()));
        }};
        return list;
    }

    public void showDialogFragment(int type, Bundle bundle){
        switch (type){
            case 1:
                AddDeviceDialogFragment d = new AddDeviceDialogFragment();
                d.show(getSupportFragmentManager(), "AddDeviceDialogFragment");
                break;
            case 2:
                ManageFriendsDialogFragment f = new ManageFriendsDialogFragment();
                f.setArguments(bundle);
                f.show(getSupportFragmentManager(), "ManageFriendsDialogFragment");
                break;
            case 3:
                AddGroupDialogFragment a = new AddGroupDialogFragment();
                a.setArguments(bundle);
                a.show(getSupportFragmentManager(),"AddGroupDialogFragment");
                break;
            case 4: // To enable GPS
                buildAlertMessageNoGps();
                break;
            case 5: //Dialog Information
                DialogInformation e = new DialogInformation();
                e.setArguments(bundle);
                e.show(getSupportFragmentManager(),"DialogInformation");
                break;
        }
    }

    // Retorna la posició del usuari
    public LatLng getDeviceLocation(){
        if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            // Obtenemos la última posición conocida
            Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(loc != null){
                return new LatLng(loc.getLatitude(), loc.getLongitude());
            }
        }
        showToast("Provider" + LocationManager.GPS_PROVIDER + " Disabled");
        return null;
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// METHODS OF ActionBar and menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSuggestionsAdapter(new SearchSuggestionsAdapter(this));
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener()
        {
            @Override
            public boolean onSuggestionClick(int position)
            {
                Toast.makeText(MainActivity.this, "Position: " + position, Toast.LENGTH_SHORT).show();
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position)
            {
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                Toast.makeText(MainActivity.this, query, Toast.LENGTH_SHORT).show();
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                return false;
            }
        });
        /*searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                new CallGeocoderTask().execute(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });*/
        return true;
    }
     private static ArrayList<String> test(String s){
         Geocoder geocoder = new Geocoder(context, Locale.getDefault());
         ArrayList<String> list = new ArrayList<>();
         try {
            for(Address a : geocoder.getFromLocationName(s,10)){
                list.add(a.getAddressLine(1));
            }
         } catch (IOException e) {
             e.printStackTrace();
         }
         return list;
     }

    public static class SearchSuggestionsAdapter extends SimpleCursorAdapter{
        private static final String[] mFields  = { "_id", "result" };
        private static final String[] mVisible = { "result" };
        private static final int[]    mViewIds = { android.R.id.text1 };
        public SearchSuggestionsAdapter(Context context)
        {
            super(context, android.R.layout.simple_list_item_1, null, mVisible, mViewIds, 0);
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint)
        {
            return new SuggestionsCursor(constraint);
        }

        private static class SuggestionsCursor extends AbstractCursor {
            private ArrayList<String> mResults;

            public SuggestionsCursor(CharSequence constraint) {

                Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                try {
                    List<Address> addressList = geocoder.getFromLocationName(constraint.toString(),10);
                    mResults = new ArrayList<String>(addressList.size());
                    for(Address a : addressList){

                        mResults.add(a.toString());
                        Log.e("---->",a.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


                final int count = 100;
//                mResults = test(constraint.toString());//new ArrayList<String>(count);
//                for (int i = 0; i < count; i++) {
//                    mResults.add(constraint.toString());
//                }
                if (!TextUtils.isEmpty(constraint)) {
                    String constraintString = constraint.toString().toLowerCase(Locale.ROOT);
                    Iterator<String> iter = mResults.iterator();
                    while (iter.hasNext()) {
                        if (!iter.next().toLowerCase(Locale.ROOT).startsWith(constraintString)) {
                            iter.remove();
                        }
                    }
                }
            }

            @Override
            public int getCount() {
                return mResults.size();
            }

            @Override
            public String[] getColumnNames() {
                return mFields;
            }

            @Override
            public long getLong(int column) {
                if (column == 0) {
                    return mPos;
                }
                throw new UnsupportedOperationException("unimplemented");
            }

            @Override
            public String getString(int column) {
                if (column == 1) {
                    return mResults.get(mPos);
                }
                throw new UnsupportedOperationException("unimplemented");
            }

            @Override
            public short getShort(int column) {
                throw new UnsupportedOperationException("unimplemented");
            }

            @Override
            public int getInt(int column) {
                throw new UnsupportedOperationException("unimplemented");
            }

            @Override
            public float getFloat(int column) {
                throw new UnsupportedOperationException("unimplemented");
            }

            @Override
            public double getDouble(int column) {
                throw new UnsupportedOperationException("unimplemented");
            }

            @Override
            public boolean isNull(int column) {
                return false;
            }
        }
    }


    @Override // Botó Menú
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement

        switch(item.getItemId()){
            case R.id.action_settings: // SETTINGS OPTION
                //Toast.makeText(this, "Toast settings", Toast.LENGTH_SHORT).show();
                Intent intent = null;
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                //finish();
                return true;
            case  R.id.add_device: // ADD DEVICE BUTTON
                showDialogFragment(1,null);
                return true;
            case R.id.airport_menuRefresh:
                setRefreshActionButtonState(true);
                startProcessGetDevices();
                // Execute some code after 2 seconds have passed
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    public void run() {
//                        setRefreshActionButtonState(false);
//                    }
//                }, 2000);
                // Complete with your code
                return true;
            case  R.id.add_group: // ADD GROUP
                showDialogFragment(3,null);
                return true;
            case  R.id.action_logout: // LOG OUT
                logoutTask = new LogoutTask();
                logoutTask.execute();
                return true;
            /*case R.id.action_fullscreen:
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setDisplayShowTitleEnabled(false);
                return true;*/
        }

        return super.onOptionsItemSelected(item);
    }

    public void setRefreshActionButtonState(final boolean refreshing) {
        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu
                    .findItem(R.id.airport_menuRefresh);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// METHODS OF TABS
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        //private Map<Integer,Fragment> fragmentsMap = new HashMap<Integer, Fragment>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                //UserMapFragment userMapFragment = (UserMapFragment)((SectionsPagerAdapter)mViewPager.getAdapter()).getFragment(0);
                //showToast(userMapFragment.getTest());
                case 0:
                    UserMapFragment userMapFragment = UserMapFragment.newInstance(position + 1);
                    fragmentsMap.put(position,userMapFragment);
                    return userMapFragment;
                case 1:
                    FriendsFragment friendsFragment = FriendsFragment.newInstance(position + 1);
                    fragmentsMap.put(position,friendsFragment);
                    return friendsFragment;
                case 2:
                    GroupsFragment groupsFragment = GroupsFragment.newInstance(position + 1);
                    fragmentsMap.put(position,groupsFragment);
                    return groupsFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }

    }


    //Dialog to enable GPS
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// METHODS OF FragmentsCommunicationInterface
    @Override
    public Map getDevices() {
        return devices;
    }

    public void startProcessGetDevices(){
        // every X seconds getDevicesFromServer
        setRefreshActionButtonState(true);
        getDevicesTask = new GetDevicesTask();
        getDevicesTask.execute();

        //((UserMapFragment) getFragment(0)).showDevicesInMap(devices);
    }

    public void changeToFragment(int position){
        if(position < 0 && position > 2) return;
        mViewPager.setCurrentItem(position);
    }

/*    public Fragment getFragment(int position){
        return ((SectionsPagerAdapter)mViewPager.getAdapter()).getFragment(position);
    }*/

    public Fragment getFragment(int key){
        return fragmentsMap.get(key);
    }
    public void setFragment(Fragment fragment, int position){
        fragmentsMap.put(position,fragment);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// ShowToast

    private void showToast(CharSequence text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// Geocoder

    public class CallGeocoderTask extends AsyncTask<String, Integer, List<Address>> {
        private Geocoder geocoder;
        @Override
        protected List<Address> doInBackground(String... params) {
            try{
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                return geocoder.getFromLocationName(params[0],10);
            }catch(IOException e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            ((UserMapFragment)getFragment(0)).centerMapOnPosition(new LatLng(addresses.get(0).getLatitude(),addresses.get(0).getLongitude()));
            super.onPostExecute(addresses);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// SendMyPositionTask

    private class SendMyPositionTask extends AsyncTask <Location ,Void ,com.lluis.onsou.backend.registration.model.Result>{

        private Registration regService = null;

        private Result result;

        @Override
        protected Result doInBackground(Location... params) {
            if (regService == null) {
                Registration.Builder builder;
                if(false){
                    builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                            new AndroidJsonFactory(), null)
                            // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
                            // otherwise they can be skipped
                            .setRootUrl("http://192.168.1.4:8080/_ah/api/")
                            .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                                @Override
                                public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                                        throws IOException {
                                    abstractGoogleClientRequest.setDisableGZipContent(true);
                                }
                            });
                    // end of optional local run code
                }else{
                    builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                            .setRootUrl("https://tranquil-well-88613.appspot.com/_ah/api/");
                }
                regService = builder.build();
            }
            try {
                result = regService.updatePosition(MyDevice.getInstance().getId(), ((Location)params[0]).getLatitude(),((Location)params[0]).getLongitude()).execute();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            sendMyPositionTask = null;
            //TODO if return error
        }
        @Override
        protected void onCancelled() {
            sendMyPositionTask = null;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// GetDevicesTask

    private class GetDevicesTask extends AsyncTask <Void ,Void ,com.lluis.onsou.backend.registration.model.Result>{

        private Registration regService = null;

        private Result result;

        @Override
        protected com.lluis.onsou.backend.registration.model.Result doInBackground(Void... params) {
            if (regService == null) {
                Registration.Builder builder;
                builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                        .setRootUrl("https://tranquil-well-88613.appspot.com/_ah/api/");
                regService = builder.build();
            }
            try {
                result = regService.getDevices(MyDevice.getInstance().getId()).execute();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            sendMyPositionTask = null;
            if(result.getStatus()){
                devices.clear();
                devices = Device.getFromServer((ArrayList)result.getDevices());
                ((UserMapFragment) getFragment(0)).showDevicesInMap(devices);
                ((FriendsFragment) getFragment(1)).showDevices(devices);
            }
            setRefreshActionButtonState(false);
        }
        @Override
        protected void onCancelled() {
            sendMyPositionTask = null;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// LogoutTask

    private class LogoutTask extends AsyncTask <Void,Void,Boolean>{

        private Registration regService = null;
        private Result result;

        @Override
        protected Boolean doInBackground(Void... params) {
            if (regService == null) {
                Registration.Builder builder;
                builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                        .setRootUrl("https://tranquil-well-88613.appspot.com/_ah/api/");
                regService = builder.build();
            }
            try {
                result = regService.unregister(MyDevice.getInstance().getId()).execute();
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
            return result.getStatus();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            logoutTask = null;
            if(aBoolean){
                MyDevice.getInstance().setGCMId("");
                MyDevice.getInstance().setOnline(false);
                finish();
            }else{
                showToast(getString(R.string.cantLogout));
            }
        }

        @Override
        protected void onCancelled() {
            logoutTask = null;
            super.onCancelled();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// Thread getDevices

    private class GetDevicesThread extends Thread{
        private Registration regService = null;
        private Result result;

        boolean stop = false;

        HashMap <com.lluis.onsou.backend.registration.model.Device,Boolean> devices;

        @Override
        public void run(){
            super.run();
            stop = false;
            while(!stop){
                if (regService == null) {
                    Registration.Builder builder;
                    builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                            .setRootUrl("https://tranquil-well-88613.appspot.com/_ah/api/");
                    regService = builder.build();
                }
                try {
                    result = regService.getDevices(MyDevice.getInstance().getId()).execute();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                if(result.getStatus()){
                    Message msg = new Message();
                    msg.obj = Device.getFromServer((ArrayList) result.getDevices());
                    msg.what = 0;
                    MainActivity.this.handler.sendMessage(msg);
                }

                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        public void goStop(){
            stop = true;
        }
    }
}



