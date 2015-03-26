package com.udl.lluis.onsou;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.udl.lluis.onsou.entities.Device;
import com.udl.lluis.onsou.entities.Group;
import com.udl.lluis.onsou.fragments.AddDeviceDialogFragment;
import com.udl.lluis.onsou.fragments.AddGroupDialogFragment;
import com.udl.lluis.onsou.fragments.FriendsFragment;
import com.udl.lluis.onsou.fragments.GroupsFragment;
import com.udl.lluis.onsou.fragments.ManageFriendsDialogFragment;
import com.udl.lluis.onsou.fragments.UserMapFragment;

import java.util.ArrayList;
import java.util.HashMap;
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

    private ActionBar actionBar;
    private MainActivity act = this;
    private Menu optionsMenu;

    // Devices
    private HashMap<Long,Device> devicesMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("------------>", "ACTIVITY ONCREATE");
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null){
            devicesMap = (HashMap) savedInstanceState.getSerializable("devicesMap");
            //fragmentsMap = (HashMap)savedInstanceState.getSerializable("fragmentMap");
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


        devicesMap = new HashMap<Long,Device>();
        getDevicesFromServer();
        //showDevicesInMap();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e("------------>", "ACTIVITY ONSAVEINSTANCESTATE");
        outState.putSerializable("devicesMap",devicesMap);
        outState.putSerializable("fragmentsMap", fragmentsMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("------------>", "ACTIVITY ONRESUME");
    }

    // Send petition to server getDevices()
    //      this return available devices depending params configured
    private void getDevicesFromServer(){

        devicesMap.clear();

        // FILL devicesMap from download data
        for(Device d : getSimulatedDevices()){
            devicesMap.put(d.getId(),d);
        }
    }

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
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// METHODS OF ActionBar and menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        return true;
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
                // Execute some code after 2 seconds have passed
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        setRefreshActionButtonState(false);
                    }
                }, 2000);
                // Complete with your code
                return true;
            case  R.id.add_group: // ADD GROUP
                showDialogFragment(3,null);
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
        return devicesMap;
    }

    public void startProcessGetDevices(){
        // every X seconds getDevicesFromServer
        getDevicesFromServer();

        ((UserMapFragment) getFragment(0)).showDevicesInMap(devicesMap);
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

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }






    private void showToast(CharSequence text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}



