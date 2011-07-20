package org.kecher.scheduler;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class Scheduler extends ListActivity {
	private static final String TAG = "Scheduler";

	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;

	private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;

	private EventsDbAdapter mDbAdapter;

	private SchedulerService mBoundService;
	private boolean mIsBound;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established
			mBoundService = ((SchedulerService.LocalBinder) service)
					.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			mBoundService = null;
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.events_list);
		mDbAdapter = new EventsDbAdapter(this);
		mDbAdapter.open();
		fillData();
		registerForContextMenu(getListView());
		Intent intent = new Intent(this, SchedulerService.class);
		intent.putExtra("message", "The Service is Running");
		Log.d(TAG, "Starting Scheduler Service.");
		this.startService(intent);
		doBindService();
	}

	private void fillData() {
		Cursor eventsCursor = mDbAdapter.fetchAllEvents();
		startManagingCursor(eventsCursor);

		// Create an array to specify the fields we want to display in the list
		// (only TITLE)
		String[] from = new String[] { EventsDbAdapter.KEY_TITLE };

		// and an array of the fields we want to bind those fields to (in this
		// case just text1)
		int[] to = new int[] { R.id.text1 };

		// Now create a simple cursor adapter and set it to display
		// the titles.
		SimpleCursorAdapter events = new SimpleCursorAdapter(this,
				R.layout.events_row, eventsCursor, from, to);
		setListAdapter(events);
	}

	void doBindService() {
		// Establish a connection with the service.
		mIsBound = bindService(new Intent(Scheduler.this,
				SchedulerService.class), mConnection, Context.BIND_AUTO_CREATE);
	}

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_ID, 0, R.string.menu_insert);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
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
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			mBoundService.removeIntent(info.id);
			mDbAdapter.deleteEvent(info.id);
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
		i.putExtra(EventsDbAdapter.KEY_ROWID, id);
		startActivityForResult(i, ACTIVITY_EDIT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillData();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDbAdapter.close();
		doUnbindService();
	}
}
