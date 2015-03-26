package com.udl.lluis.onsou;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import java.util.Map;

/**
 * Created by Lluís on 20/03/2015.
 */
public interface FragmentsCommunicationInterface {
    public Map getDevices();
    public void startProcessGetDevices();
    public void changeToFragment(int position);
    public Fragment getFragment(int position);
    public void showDialogFragment(int type, Bundle bundle);
    public void onDialogPositiveClick(DialogFragment dialog);
    public void onDialogNegativeClick(DialogFragment dialog);
}
