/**

 Copyright (C) 2015, Roman P., dev.roman [at] gmail

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see http://www.gnu.org/licenses/

 */

package com.rp.podemu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.ArrayList;
import java.util.Vector;



public class LogoDownloadBehaviourDialogFragment extends DialogFragment // implements DialogInterface.OnClickListener
{

    private ArrayList<Integer> logoDownloadBehaviourList;
    // Use this instance of the interface to deliver action events

    public LogoDownloadBehaviourDialogFragment()
    {

    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface LogoDownloadBehaviourDialogListener
    {
        void onLogoDownloadBehaviourSelected(DialogInterface dialog, int which);
    }

    // Use this instance of the interface to deliver action events
    LogoDownloadBehaviourDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (LogoDownloadBehaviourDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement LogoDownloadBehaviourDialogListener");
        }
    }


    public void setLogoDownloadBehaviourList(ArrayList<Integer> b)
    {
        logoDownloadBehaviourList=b;
    }

    public ArrayList<Integer> getLogoDownloadBehaviourList()
    {
        return logoDownloadBehaviourList;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        //PackageManager pm = super.getPackageManager();
        Vector<String> appNames = new Vector<String>();
        for (Integer i : logoDownloadBehaviourList)
        {
            appNames.add(SettingsActivity.getLogoBehaviourOptionString(i, getContext()));
        }

        // converting Vector appNames to String[] appNamesStr
        String [] appNamesStr=appNames.toArray(new String[appNames.size()]);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select logo download behaviour")
                .setItems(appNamesStr, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        mListener.onLogoDownloadBehaviourSelected(dialog, which);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

/*

    @Override
    public void onClick(DialogInterface dialogInterface, int i)
    {
        //dismiss();
    }
*/
}
