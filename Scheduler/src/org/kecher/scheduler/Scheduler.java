package org.kecher.scheduler;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class Scheduler extends ListActivity{
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
	private ArrayList<Boolean> mWeekDays;

    private EventsDbAdapter mDbHelper;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_list);
        mDbHelper = new EventsDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }

    private void fillData() {
        Cursor eventsCursor = mDbHelper.fetchAllEvents();
        startManagingCursor(eventsCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{EventsDbAdapter.KEY_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter events = 
            new SimpleCursorAdapter(this, R.layout.events_row, eventsCursor, from, to);
        setListAdapter(events);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                createEvent();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteEvent(info.id);
                removeIntent();
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void removeIntent() {
		// TODO Complete this function remove the intent from the alarm manager.
	}

	private void createEvent() {
        Intent i = new Intent(this, EventEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, EventEdit.class);
        i.putExtra(EventsDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        scheduleEvent();
        fillData();
    }
    
	private void scheduleEvent() {
		Calendar cal = Calendar.getInstance();
		
		cal.set(Calendar.HOUR_OF_DAY, mRunTime.getCurrentHour());
		cal.set(Calendar.MINUTE, mRunTime.getCurrentMinute());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		mWeekDays.add(mSun.isChecked());
		mWeekDays.add(mMon.isChecked());
		mWeekDays.add(mTues.isChecked());
		mWeekDays.add(mWed.isChecked());
		mWeekDays.add(mThur.isChecked());
		mWeekDays.add(mFri.isChecked());
		mWeekDays.add(mSat.isChecked());
		
		int curDay = cal.get(Calendar.DAY_OF_WEEK);
		curDay--;
		for (int i = 0; i < 7; i++) {
			if (mWeekDays.get((curDay + i) % 7)) {
				cal.add(Calendar.DAY_OF_MONTH, i);
				break;
			}
		}
		
		Intent intent = new Intent(getApplicationContext(), SchedulerReciever.class);
		intent.putExtra(EventsDbAdapter.KEY_ROWID, mRowId);
		
		PendingIntent sender = PendingIntent.getBroadcast(
				this, 123456, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

		mDbHelper.updateEventRunTimes(mRowId, 0L, cal.getTimeInMillis());
				
	}

}
