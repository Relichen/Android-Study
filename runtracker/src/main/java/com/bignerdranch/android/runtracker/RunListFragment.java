package com.bignerdranch.android.runtracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author Zhuo
 *         2015/12/15
 */
public class RunListFragment extends ListFragment {

    private static final int REQUEST_NEW_RUN = 0;
    private static final int NOTIFICATION_ID = 0;

    private RunDatabaseHelper.RunCursor mCursor;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCursor = RunManager.get(getActivity()).queryRuns();
        mNotificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        setHasOptionsMenu(true);

        RunCursorAdapter adapter = new RunCursorAdapter(getActivity(), mCursor);
        setListAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        mCursor.close();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.run_list_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_run:
                Intent intent = new Intent(getActivity(), RunActivity.class);
                startActivityForResult(intent, REQUEST_NEW_RUN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW_RUN) {
            mCursor.requery();
            ((RunCursorAdapter)getListAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent i = new Intent(getActivity(), RunActivity.class);
        i.putExtra(RunActivity.EXTRA_RUN_ID, id);
        startActivity(i);
    }

    private static class RunCursorAdapter extends CursorAdapter {

        private RunDatabaseHelper.RunCursor mRunCursor;

        public RunCursorAdapter(Context context, RunDatabaseHelper.RunCursor c) {
            super(context, c, 0);
            mRunCursor = c;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Run run = mRunCursor.getRun();

            TextView startDateTextView = (TextView) view;
            String cellText = context.getString(R.string.cell_text,
                    DateFormat.format("yyyy年MM月dd日 EEEE kk:mm", run.getStartDate()));
            startDateTextView.setText(cellText);
            if (RunManager.get(context).isTrackingRun(run)) {
                startDateTextView.setTextColor(Color.GREEN);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        long mCurrentRunId = getActivity().getSharedPreferences(RunManager.PREFS_FILE, Context.MODE_PRIVATE)
                .getLong(RunManager.PREF_CURRENT_RUN_ID, -1);

        if (mCurrentRunId == -1) {
            return;
        }

        Resources r = getActivity().getResources();

        Intent intent = new Intent(getActivity(), RunActivity.class);
        intent.putExtra(RunActivity.EXTRA_RUN_ID, mCurrentRunId);
        PendingIntent pi = PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent
                .FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(getActivity())
                .setTicker(r.getString(R.string.ticker_text))
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle(r.getString(R.string.content_title))
                .setContentText(r.getString(R.string.content_text) + mCurrentRunId)
                .setContentIntent(pi)
                .setAutoCancel(false)
                .build();

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public void onPause() {
        super.onPause();
        mNotificationManager.cancel(NOTIFICATION_ID);

    }
}
