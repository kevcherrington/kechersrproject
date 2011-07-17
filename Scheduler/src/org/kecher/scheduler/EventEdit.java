package org.kecher.scheduler;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

public class EventEdit extends Activity {
	private static final String TAG = "EventEdit";
	
	private Long mRowId;
	private EditText mTitleText;
	private TimePicker mRunTime;
	private CheckBox mSun;
	private CheckBox mMon;
	private CheckBox mTues;
	private CheckBox mWed;
	private CheckBox mThur;
	private CheckBox mFri;
	private CheckBox mSat;
	private Spinner mMode;
	private String mModeString;
	private TextView mVolText;
	private SeekBar mVolBar;
	private ToggleButton mVibrate;
	private Button mConfirm;
	private EventsDbAdapter mDbHelper;
	
	private SchedulerService mBoundService;
	private boolean mIsBound;
	
	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        mBoundService = ((SchedulerService.LocalBinder)service).getService();

	        // Tell the user about this for our demo.
	        Toast.makeText(EventEdit.this, R.string.service_connected,
	                Toast.LENGTH_SHORT).show();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        mBoundService = null;
	        Toast.makeText(EventEdit.this, R.string.service_disconnected,
	                Toast.LENGTH_SHORT).show();
	    }
	};
	
	/*
	 * public inner class that defines the item selected listener for
	 * the mMode spinner. When nothing is selected it should default
	 * to the first item in the ring_modes array.
	 */
	public class MyItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			mModeString = mMode.getItemAtPosition(pos).toString();
		}

		public void onNothingSelected(AdapterView<?> parent) { /* Do nothing*/ }
	}

	/*
	 * Public inner class that defines the seek bar change listener for 
	 * mVolBar. We do nothing with start and stop tracking touch because
	 * we will make a direct call to mVolBar when we want to get the value.
	 * This class is mainly used to update the percentage shown by the 
	 * volume title.
	 */
	public class MySeekBarChangeListener implements OnSeekBarChangeListener {
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			mVolText.setText("Volume: " + (seekBar.getProgress()+1) + "%");
			Log.d("test", "Volume changed to " + (progress+1));
		}
		
		public void onStartTrackingTouch (SeekBar seekBar) { /* do nothing */}
		
		public void onStopTrackingTouch (SeekBar seekBar) { /* do nothing */}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new EventsDbAdapter(this);
		mDbHelper.open();

		setContentView(R.layout.event_edit);
		setTitle(R.string.edit_event);

		mTitleText = (EditText) findViewById(R.id.title);
		mRunTime = (TimePicker) findViewById(R.id.run_time);
		mSun = (CheckBox) findViewById(R.id.sun);
		mMon = (CheckBox) findViewById(R.id.mon);
		mTues = (CheckBox) findViewById(R.id.tues);
		mWed = (CheckBox) findViewById(R.id.wed);
		mThur = (CheckBox) findViewById(R.id.thur);
		mFri = (CheckBox) findViewById(R.id.fri);
		mSat = (CheckBox) findViewById(R.id.sat);
		mMode = (Spinner) findViewById(R.id.mode);
		mVolText = (TextView) findViewById(R.id.voltext);
		mVolBar = (SeekBar) findViewById(R.id.volbar);
		mVolBar.setMax(100);
		mVolBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());
		mVibrate = (ToggleButton) findViewById(R.id.vibe);
		mConfirm = (Button) findViewById(R.id.confirm);
		
		mRowId = (savedInstanceState == null) ? null
				: (Long) savedInstanceState
						.getSerializable(EventsDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(EventsDbAdapter.KEY_ROWID)
					: null;
		}

		populateFields();

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.ring_modes, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
		mMode.setAdapter(adapter);

		mMode.setOnItemSelectedListener(new MyItemSelectedListener());

		doBindService();
		mBoundService.dbConnect(this);

		mConfirm.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				setResult(RESULT_OK);
				finish();
			}

		});
	}

	private void populateFields() {
		if (mRowId != null) {
			Cursor event = mDbHelper.fetchEvent(mRowId);
			startManagingCursor(event);
			mTitleText.setText(event.getString(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_TITLE)));
			mRunTime.setCurrentHour(event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_RUN_TIME_HOUR)));
			mRunTime.setCurrentMinute(event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_RUN_TIME_MINUTE)));
			mSun.setChecked((event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_SUN)) == 1) ? true
					: false);
			mMon.setChecked((event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_MON)) == 1) ? true
					: false);
			mTues.setChecked((event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_TUES)) == 1) ? true
					: false);
			mWed.setChecked((event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_WED)) == 1) ? true
					: false);
			mThur.setChecked((event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_THUR)) == 1) ? true
					: false);
			mFri.setChecked((event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_FRI)) == 1) ? true
					: false);
			mSat.setChecked((event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_SAT)) == 1) ? true
					: false);
			mModeString = event.getString(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_MODE));
			mVolBar.setProgress(event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_VOL)));
			mVolText.setText("Volume: " + (mVolBar.getProgress()+1) + "%");
			mVibrate.setChecked((event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_VIBRATE)) == 1) ? true
					: false);
			
			// initialize the mode spinner.
			String[] modes = getResources().getStringArray(R.array.ring_modes);
			if (mModeString.equals(modes[0])) {
				mMode.setSelection(0);
			} else if (mModeString.equals(modes[1])) {
				mMode.setSelection(1);
			} else if (mModeString.equals(modes[2])) {
				mMode.setSelection(2);
			} else {
				mMode.setSelection(0);
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
		outState.putSerializable(EventsDbAdapter.KEY_ROWID, mRowId);
	}

	/*
	 * On pause is always called when an activity ends. So now would be the time
	 * to schedule the event if we are going to schedule it.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		saveState();
		Log.d(TAG, "Saved event in DB");
		Log.d(TAG, "mBoundService is " + ((mBoundService == null) ? "Null" : "not Null"));
		Log.d(TAG, "mRowId is " + ((mRowId == null) ? "Null" : "not Null"));
		mBoundService.scheduleEvent(mRowId);
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	private void saveState() {
		String title = mTitleText.getText().toString();
		int runHour = mRunTime.getCurrentHour();
		int runMin = mRunTime.getCurrentMinute();
		int sun = mSun.isChecked() ? 1 : 0;
		int mon = mMon.isChecked() ? 1 : 0;
		int tues = mTues.isChecked() ? 1 : 0;
		int wed = mWed.isChecked() ? 1 : 0;
		int thur = mThur.isChecked() ? 1 : 0;
		int fri = mFri.isChecked() ? 1 : 0;
		int sat = mSat.isChecked() ? 1 : 0;
		String mode = mModeString;
		int vol = mVolBar.getProgress();
		int vibe = mVibrate.isChecked() ? 1 : 0;

		if (mRowId == null) {
			long id = mDbHelper.createEvent(title, runHour, runMin, sun, mon,
					tues, wed, thur, fri, sat, mode, vol, vibe, 0L, 0L);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updateEvent(mRowId, title, runHour, runMin, sun, mon,
					tues, wed, thur, fri, sat, mode, vol, vibe, 0L, 0L);
		}
	}

	void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
	    bindService(new Intent(EventEdit.this, 
	            SchedulerService.class), mConnection, Context.BIND_AUTO_CREATE);
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
	protected void onDestroy() {
	    super.onDestroy();
	    doUnbindService();
	}

}
