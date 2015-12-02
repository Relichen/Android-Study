package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.Date;

/**
 * @author  Zhuo
 *
 */
public class ChoiceDialogFragment extends DialogFragment {

    public static final int CHOICE_DATE = 1;
    public static final int CHOICE_TIME = 2;
    public static final String EXTRA_CHOICE = "com.bignerdranch.android.criminalintent.choice";
    private Date mDate;
    private DialogFragment mDialogFragment;
    private int mChoice;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.time_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChoice = CHOICE_TIME;
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .setNegativeButton(R.string.date_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChoice = CHOICE_DATE;
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .setMessage(R.string.picker_title)
                .create();
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent i = new Intent();
        i.putExtra(EXTRA_CHOICE, mChoice);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }


}
