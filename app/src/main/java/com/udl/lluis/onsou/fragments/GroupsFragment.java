package com.udl.lluis.onsou.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.udl.lluis.onsou.R;

/**
 * Created by Lluís on 19/03/2015.
 */
/**
 * A placeholder fragment containing a simple view.
 */
public class GroupsFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private FragmentActivity context;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static GroupsFragment newInstance(int sectionNumber) {
        GroupsFragment fragment = new GroupsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public GroupsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.groups_fragment, container, false);


        ExpandableListAdapter mAdapter;
        ExpandableListView epView = (ExpandableListView) rootView.findViewById(R.id.expandableListViewGroups);
        mAdapter = new MyExpandableListAdapter();
        epView.setAdapter(mAdapter);

        epView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView arg0, View arg1,
                                        int groupPosition, long arg3) {
                if (groupPosition == 5) {

                }

                // Aqui podriamos cambiar si quisieramos el comportamiento de apertura y cierre de las listas explandibles mediante los metodos collapseGroup(int groupPos) y expandGroup(int groupPos)

                return false;
            }
        });

        epView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent,
                                        View v, int groupPosition, int childPosition,
                                        long id) {
                if (groupPosition == 0 && childPosition == 0) {

                }

                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = (FragmentActivity)activity;
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
        private String[] groups = {"Family", "Town Friends"};
        private String[][] children = { { "Mom" },{ "Jonny" }, { "Child3" },{ "Child4" }, { "Child5" } };

        public Object getChild(int groupPosition, int childPosition) {
            return children[groupPosition][childPosition];
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            int i = 0;
            try {
                i = children[groupPosition].length;

            } catch (Exception e) {
            }

            return i;
        }

        public TextView getGenericView(int groupPosition, int childPosition) {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 64);

            TextView textView = new TextView(context);
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
            textView.setText(getChild(groupPosition, childPosition).toString()+" group: "+groupPosition+" child: " + childPosition);
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
            textView.setText(getGroup(groupPosition).toString()+" group: "+groupPosition);
            return textView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

    }
}
