package com.udl.lluis.onsou.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.udl.lluis.onsou.FragmentsCommunicationInterface;
import com.udl.lluis.onsou.R;
import com.udl.lluis.onsou.entities.Device;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Lluís on 19/03/2015.
 */
public class FriendsFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    //private FragmentActivity context;
    private FragmentsCommunicationInterface mCallback;

    private ExpandableListAdapter mAdapter;

    private List<Device> devicesOnLineList;
    private List<Device> devicesOffLineList;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FriendsFragment newInstance(int sectionNumber) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FriendsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        devicesOnLineList = new ArrayList<>();
        devicesOffLineList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.friends_fragment, container, false);

        ExpandableListView epView = (ExpandableListView) rootView.findViewById(R.id.expandableListViewFriends);
        mAdapter = new MyExpandableListAdapter();
        epView.setAdapter(mAdapter);

        epView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView arg0, View arg1,
                                        int groupPosition, long arg3) {
                // Aqui podriamos cambiar si quisieramos el comportamiento de apertura y cierre de
                // las listas explandibles mediante los metodos collapseGroup(int groupPos) y expandGroup(int groupPos)
                return false;
            }
        });
        epView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);

                    // You now have everything that you would as if this was an OnChildClickListener()
                    // Add your logic here.

                    Bundle bundle = new Bundle();
                    if(groupPosition == 0) bundle.putSerializable("device",devicesOnLineList.get(childPosition));
                    else bundle.putSerializable("device",devicesOffLineList.get(childPosition));
                    mCallback.showDialogFragment(2,bundle);
                    // Return true as we are handling the event.
                    return true;
                }

                return false;
            }
        });


        epView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent,
                                        View v, int groupPosition, int childPosition,
                                        long id) {
                if (groupPosition == 0) {
                    ((UserMapFragment) mCallback.getFragment(0)).centerMapOn(devicesOnLineList.get(childPosition).getId());
                    mCallback.changeToFragment(0);
                }

                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        for(Device d : new ArrayList<Device>(mCallback.getDevices().values())){
           if (d.isFriend()){
               if(d.isOnline()){
                   devicesOnLineList.add(d);
               }else{
                   devicesOffLineList.add(d);
               }
           }
        }

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


    private void showDevicesInList(Map devices){

    }


    /**
     * A simple adapter which maintains an ArrayList of photo resource Ids. Each
     * photo is displayed as an image. This adapter supports clearing the list
     * of photos and adding a new photo.
     *
     */
    public class MyExpandableListAdapter extends BaseExpandableListAdapter {
        // Sample data set. children[i] contains the children (String[]) for
        // groups[i].
        private String[] groups = {"On - Line", "Off - Line"};

        public Object getChild(int groupPosition, int childPosition) {
            if(groupPosition == 0){
                return devicesOnLineList.get(childPosition).getName();
            }else{
                return devicesOffLineList.get(childPosition).getName();
            }
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            int onLine = 0;
            int offLine = 0;
            int i = 0;
            try {
                if(groupPosition == 0){
                    i = devicesOnLineList.size();
                }else{
                    i = devicesOffLineList.size();
                }
            } catch (Exception e) {}
            return i;
        }

        public TextView getGenericView(int groupPosition, int childPosition) {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 64);

            TextView textView = new TextView(getActivity());
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

            switch (groupPosition){
                case 0:
                    if(childPosition == Integer.MIN_VALUE){
                        textView.setTextColor(getResources().getColor(R.color.darkblue));
                        textView.setTextSize(20);
                        //textView.setBackgroundColor();
                    }else{
                        textView.setTextColor(getResources().getColor(R.color.green));
                        textView.setTextSize(15);
                    }
                    break;
                case 1:
                    if(childPosition == Integer.MIN_VALUE){
                        textView.setTextColor(getResources().getColor(R.color.darkorange));
                        textView.setTextSize(20);
                    }else{
                        //textView.setTextColor(getResources().getColor(R.color.darkorange));
                        textView.setTextSize(15);
                    }
                    break;
            }

            // Set the text starting position
            textView.setPadding(50, 0, 0, 0);
            return textView;
        }

        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            TextView textView = getGenericView(groupPosition, childPosition);
            textView.setText(getChild(groupPosition, childPosition).toString());
            return textView;
        }

        public Object getGroup(int groupPosition) {
            return groups[groupPosition];
        }

        public int getGroupCount() {
            return groups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            TextView textView = getGenericView(groupPosition,Integer.MIN_VALUE);
            textView.setText(getGroup(groupPosition).toString());
            return textView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

    }

    private void showToast(CharSequence text){
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }
}
