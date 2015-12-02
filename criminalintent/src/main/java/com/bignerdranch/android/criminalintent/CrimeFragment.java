package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String TAG = "CrimeFragment";
    public static final String EXTRA_CRIME_ID = "com.bignerdranch.android.criminalintent.crime_id";
    private static final String EXTRA_PHOTO_NAME = "com.bignerdranch.android.criminalintent.photo_name";
    private static final String DIALOG_IMAGE = "image";
    private static final String DIALOG_DATE = "date";
    private static final String DIALOG_TIME = "time";
    private static final String DIALOG_CHOICE = "choice";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CHOICE = 2;
    private static final int REQUEST_PHOTO = 3;
//    private static final int REQUEST_INTENT_PHOTO = 4;
    private static final int REQUEST_CONTACT = 5;
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    //    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mSuspectButton;
    private Button mDialButton;
    private Callbacks mCallbacks;
//    private Uri mPhotoUri;
//    private String fileName="a";

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        UUID c = (UUID) getActivity().getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        setHasOptionsMenu(true);
        UUID crimeId = (UUID) getArguments().getSerializable(EXTRA_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }

    public void updateDate() {
        mDateButton.setText(DateFormat.format("yyyy年MM月dd日 EEEE kk:mm", mCrime.getDate()));
    }

//    public void updateTime() {
//        mTimeButton.setText(DateFormat.format("kk:mm", mCrime.getDate()));
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        if (NavUtils.getParentActivityName(getActivity()) != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                mCallbacks.onCrimeUpdated(mCrime);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                ChoiceDialogFragment dialogFragment = new ChoiceDialogFragment();
                dialogFragment.setTargetFragment(CrimeFragment.this, REQUEST_CHOICE);
                dialogFragment.show(fragmentManager, DIALOG_CHOICE);
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                mCallbacks.onCrimeUpdated(mCrime);
            }
        });

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_imageButton);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CrimeCameraActivity.class);
                startActivityForResult(i, REQUEST_PHOTO);

//                使用隐式intent调用相机
//                并不好使... 全局变量 fileName、photoUri 在onActivityResult中会变成空值??????
//                所以没办法在onActivityResult中新建Photo对象
//                fileName = UUID.randomUUID().toString() + ".jpg";
//                File file = new File(Environment.getExternalStoragePublicDirectory(Environment
//                        .DIRECTORY_PICTURES), fileName);
//                mPhotoUri = Uri.fromFile(file);
//                Log.i(TAG, "photo filename: " + mPhotoUri.toString());
//                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                i.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
//                startActivityForResult(i, REQUEST_INTENT_PHOTO);
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.crime_imageView);
        registerForContextMenu(mPhotoView);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Photo photo = mCrime.getPhoto();
                if (photo == null) {
                    return;
                }
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                String path = getActivity().getFileStreamPath(photo.getFilename()).getAbsolutePath();
                ImageFragment.newInstance(path, mCrime.getPhoto().getOrientation()).show(fragmentManager, DIALOG_IMAGE);
            }
        });

        final PackageManager packageManager = getActivity().getPackageManager();
        boolean hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD ||
                Camera.getNumberOfCameras() > 0;

        if (!hasCamera) {
            mPhotoButton.setEnabled(false);
        }

        Button reportButton = (Button) v.findViewById(R.id.crime_reportButton);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        mSuspectButton = (Button) v.findViewById(R.id.crime_suspectButton);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);

                List<ResolveInfo> activities = packageManager.queryIntentActivities(i, 0);
                boolean isIntentSafe = activities.size() > 0;
                if (isIntentSafe) {
                    startActivityForResult(i, REQUEST_CONTACT);
                }
            }
        });
        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        mDialButton = (Button) v.findViewById(R.id.crime_dialButton);

        mDialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCrime.getSuspectPhoneNumber() == null) {
                    return;
                }
                Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mCrime.getSuspectPhoneNumber()));
                startActivity(i);
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CHOICE) {
            int choice = data.getIntExtra(ChoiceDialogFragment.EXTRA_CHOICE, 0);
            if (choice == 0) {
                return;
            }
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            if (choice == ChoiceDialogFragment.CHOICE_DATE) {
                DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(mCrime.getDate());
                datePickerFragment.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                datePickerFragment.show(fragmentManager, DIALOG_DATE);
            } else if (choice == ChoiceDialogFragment.CHOICE_TIME) {
                TimePickerFragment timePickerFragment = TimePickerFragment.newInstance(mCrime.getDate());
                timePickerFragment.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                timePickerFragment.show(fragmentManager, DIALOG_TIME);
            }
        } else if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            mCallbacks.onCrimeUpdated(mCrime);
            updateDate();
        } else if (requestCode == REQUEST_TIME) {
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(date);
            mCallbacks.onCrimeUpdated(mCrime);
            updateDate();
        } else if (requestCode == REQUEST_PHOTO) {

            if (mCrime.getPhoto() != null) {
                mCrime.deletePhoto(getActivity().getFileStreamPath(mCrime.getPhoto().getFilename()).getAbsolutePath());
            }

            String filename = data.getStringExtra(CrimeCameraFragment.EXTRA_PHOTO_FILENAME);
            int orientation = data.getIntExtra(CrimeCameraFragment.EXTRA_PHOTO_ORIENTATION, 0);
            if (filename != null) {
                Photo photo = new Photo(filename, orientation);
                mCrime.setPhoto(photo);
//                mCallbacks.onCrimeUpdated(mCrime);
                showPhoto();
            }
//        } else if (requestCode == REQUEST_INTENT_PHOTO) {
//
//            if (mCrime.getPhoto() != null) {
//                mCrime.deletePhoto(getActivity().getFileStreamPath(mCrime.getPhoto().getFilename()).getAbsolutePath());
//            }

//            String fileName = data.getStringExtra(EXTRA_PHOTO_NAME);
//            Log.i(TAG, "photo filename: " + fileName);
//            if (fileName != null) {
//                Photo photo = new Photo(fileName);
//                mCrime.setPhoto(photo);
//                showPhoto();
//            }

        } else if (requestCode == REQUEST_CONTACT) {
            Uri contactUri = data.getData();

            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);
            if (c.getCount() == 0) {
                c.close();
                return;
            }

            c.moveToFirst();
            String suspect = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String phoneNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            mCrime.setSuspect(suspect);
            mCrime.setSuspectPhoneNumber(phoneNumber);
//            mCallbacks.onCrimeUpdated(mCrime);
            mSuspectButton.setText(suspect);
            c.close();
        }
    }

    public static CrimeFragment newInstance(UUID crimeId) {

        Bundle args = new Bundle();
        args.putSerializable(EXTRA_CRIME_ID, crimeId);
        CrimeFragment crimeFragment = new CrimeFragment();
        crimeFragment.setArguments(args);
        return crimeFragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).saveCrimes();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_crime:
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.delete_picker_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                                if (NavUtils.getParentActivityName(getActivity()) != null) {
                                    NavUtils.navigateUpFromSameTask(getActivity());
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                return true;
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (mCrime.getPhoto() != null)
            getActivity().getMenuInflater().inflate(R.menu.crime_photo_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mCrime.deletePhoto(getActivity().getFileStreamPath(mCrime.getPhoto().getFilename()).getAbsolutePath())) {
            showPhoto();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void showPhoto() {
        Photo p = mCrime.getPhoto();
        BitmapDrawable b = null;
        if (p != null) {
            String path = getActivity().getFileStreamPath(p.getFilename()).getAbsolutePath();
            b = PictureUtils.getScaledDrawable(getActivity(), path);
            if (b != null && p.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
                b = PictureUtils.getPortraitDrawable(mPhotoView, b);
            }
        }
        mPhotoView.setImageDrawable(b);
    }

    @Override
    public void onStart() {
        super.onStart();
        showPhoto();
    }

    @Override
    public void onStop() {
        super.onStop();
        PictureUtils.cleanImageView(mPhotoView);
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    private static File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = null;
        try {
            // This location works best if you want the created images to be
            // shared
            // between applications and persist after your app has been
            // uninstalled.
            mediaStorageDir = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "MyCameraApp");

            Log.d(TAG, "Successfully created mediaStorageDir: "
                    + mediaStorageDir);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error in Creating mediaStorageDir: "
                    + mediaStorageDir);
        }

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                // 在SD卡上创建文件夹需要权限：
                // <uses-permission
                // android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
                Log.d(TAG,
                        "failed to create directory, check if you have the WRITE_EXTERNAL_STORAGE permission");
                return null;
            }
        }

        // Create a media file name
        String fileName = UUID.randomUUID().toString() + ".jpg";
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath(),fileName);

        return mediaFile;
    }
}
