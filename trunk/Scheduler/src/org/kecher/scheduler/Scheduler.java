package org.kecher.scheduler;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
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
import android.widget.Toast;

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
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a
			// explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mBoundService = ((SchedulerService.LocalBinder) service)
					.getService();

			// Tell the user about this for our demo.
			Toast.makeText(Scheduler.this, R.string.service_connected,
					Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			mBoundService = null;
			Toast.makeText(Scheduler.this, R.string.service_disconnected,
					Toast.LENGTH_SHORT).show();
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
		if (mIsBound) {
			Log.d(TAG, "bound to service connecting to Db");
			if (mBoundService == null) {
				Log.d(TAG, "mBoundService is null.");
			}
			mBoundService.dbConnect(getApplicationContext());
		} else {
			Log.d(TAG, "Not Bound to Service");
		}
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
		SimpleCursorAdapter events = new SimpleCursorAdapter(this,
				R.layout.events_row, eventsCursor, from, to);
		setListAdapter(events);
	}

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(Scheduler.this, SchedulerService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
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
	
	protected void adjustSoundSettings(Long rowId) {
		try {
			mDbAdapter = new EventsDbAdapter(this);
			mDbAdapter.open();

			AudioManager adoMngr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


			Cursor event = mDbAdapter.fetchEvent(rowId);
			String mode = event.getString(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_MODE));
			int vol = event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_VOL));
			boolean vibe = (event
					.getInt(event
							.getColumnIndexOrThrow(EventsDbAdapter.KEY_VIBRATE)) == 1) ? true
					: false;
			String[] modes = getResources().getStringArray(
					R.array.ring_modes);

			if (mode.equals(modes[2])) { // Silent
				Log.d(TAG, "Changed to " + mode + " " + vol + " "
						+ vibe);
				adoMngr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				adoMngr.setVibrateSetting(
						AudioManager.VIBRATE_TYPE_RINGER,
						AudioManager.VIBRATE_SETTING_OFF);
				adoMngr.setVibrateSetting(
						AudioManager.VIBRATE_TYPE_NOTIFICATION,
						AudioManager.VIBRATE_SETTING_OFF);
				// adoMngr.setStreamVolume(AudioManager.STREAM_RING, 0,
				// AudioManager.FLAG_SHOW_UI);
				// turn off Ringer and Vibrate
			} else if (mode.equals(modes[1])) { // Vibrate
				Log.d(TAG, "Changed to " + mode + " " + vol + " "
						+ vibe);
				adoMngr.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

				// Set phone to vibrate and turn off ringer.
			} else if (mode.equals(modes[0])) { // Normal
				Log.d(TAG, "Changed to " + mode + " " + vol + " "
						+ vibe);
				adoMngr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

				int volLvl = adoMngr
						.getStreamMaxVolume(AudioManager.STREAM_RING);
				adoMngr.setStreamVolume(AudioManager.STREAM_RING,
						((int) (volLvl * (vol * .01))),
						AudioManager.FLAG_SHOW_UI);

				int noteLvl = adoMngr
						.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
				adoMngr.setStreamVolume(
						AudioManager.STREAM_NOTIFICATION,
						((int) (noteLvl * (vol * .01))),
						AudioManager.FLAG_SHOW_UI);

				if (vibe) {
					adoMngr.setVibrateSetting(
							AudioManager.VIBRATE_TYPE_RINGER,
							AudioManager.VIBRATE_SETTING_ON);
				} else {
					adoMngr.setVibrateSetting(
							AudioManager.VIBRATE_TYPE_RINGER,
							AudioManager.VIBRATE_SETTING_OFF);
				}
				// Set the Volume and vibrate indicated.
			} else {
				throw new Exception(
						"Ring Scheduler: invalid mode provided");
			}

			mBoundService.rescheduleEvent(rowId);
			event.close();
		} catch (Exception e) {
			Toast.makeText(this, "Ring Scheduler: An error occured",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} finally { // Make sure to release resources
			Log.d(TAG, "closing DB");
			mDbAdapter.close();
			Log.d(TAG, "DB has been CLOSED.");
		}
	}

	public class SchedulerReciever extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if ((intent.getAction() != null)
					&& (intent.getAction()
							.equals("android.intent.action.BOOT_COMPLETED"))) {
				Intent newIntent = new Intent(context, SchedulerService.class);
				newIntent.putExtra("message", "The Service is Running");
				Log.d(TAG, "Starting Scheduler Service.");
				Scheduler.this.startService(newIntent);
			} else {
				Bundle bundle = intent.getExtras();
				adjustSoundSettings(bundle.getLong(EventsDbAdapter.KEY_ROWID));
			}
		}
	}
}
