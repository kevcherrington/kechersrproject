package org.kecher.scheduler;

import android.app.ListActivity;
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

public class Scheduler extends ListActivity {
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;
	
	private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private EventDbAdapter mDbHelper;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_list);
        mDbHelper = new EventDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }

    private void fillData() {
        // Get all of the rows from the database and create the item list
        Cursor eventCursor = mDbHelper.fetchAllEvents();
        startManagingCursor(eventCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{DbFields.KEY_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter events = 
            new SimpleCursorAdapter(this, R.layout.events_row, eventCursor, from, to);
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
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createEvent() {
        Intent i = new Intent(this, EventEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, EventEdit.class);
        i.putExtra(DbFields.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}    
//		scheduleAlarm();
//		silenceRinger();
//		Toast.makeText(getApplicationContext(), "AlarmScheduled",
//				Toast.LENGTH_SHORT).show();

//        mHandler.removeCallbacks(showToast);
//        mHandler.postDelayed(showToast, 10000);
//    }
//
//    private void fillData() {
//    	// get the rows from the db and create event list
//    	Cursor eventCursor = mDbHelper.fetchAllEvents();
//    	startManagingCursor(eventCursor);
//    	
//    	// Create an array to specify the fields we want to display
//    	String[] from = new String[]{EventDbAdapter.KEY_TITLE};
//    	
//    	//
//    	int[] to = new int[]{R.id.text1};
//    	
//    	SimpleCursorAdapter events =
//    		new SimpleCursorAdapter(this, R.layout.event_row, )
//    }
//	private void silenceRinger() {
//		AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
//	
//		am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
//	}
//
//	private void scheduleAlarm() {
//		// get a Calendar object with current time
//		Calendar cal = Calendar.getInstance();
//		// add 5 minutes to the calendar object
//		cal.add(Calendar.MINUTE, 1);
//		Intent intent = new Intent(getApplicationContext(), AlarmRec.class);
//		intent.putExtra("volume", new Integer(100));
//		intent.putExtra("vibrate", new Integer(1));
//		PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//		// Get the AlarmManager service
//		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
//		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//	    MenuInflater inflater = getMenuInflater();
//	    inflater.inflate(R.menu.menu, menu);
//	    return true;
//	}
//	
//    private Runnable showToast = new Runnable() {
//    	public void run() {
//    		Context context = getApplicationContext();
//    		Toast toast = Toast.makeText(context, "Hello World", Toast.LENGTH_LONG);
//    		toast.show();
//    	}
//    };
//}