package org.kecher.scheduler;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.ToggleButton;

public class EventEdit extends Activity {

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

	public class MyItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			mModeString = mMode.getItemAtPosition(pos).toString();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing
		}
	}

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

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
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
					tues, wed, thur, fri, sat, mode, vol, vibe);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updateEvent(mRowId, title, runHour, runMin, sun, mon,
					tues, wed, thur, fri, sat, mode, vol, vibe);
		}
	}

}
