package org.kecher.scheduler;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;

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
    private Button mConfirm;
    private EventsDbAdapter mDbHelper;
    

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

        mConfirm = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(EventsDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(EventsDbAdapter.KEY_ROWID)
									: null;
		}

		populateFields();

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
            mTitleText.setText(event.getString(
                    event.getColumnIndexOrThrow(EventsDbAdapter.KEY_TITLE)));
            mRunTime.setCurrentHour(event.getInt(
            		event.getColumnIndexOrThrow(EventsDbAdapter.KEY_RUN_TIME_HOUR)));
            mRunTime.setCurrentMinute(event.getInt(
            		event.getColumnIndexOrThrow(EventsDbAdapter.KEY_RUN_TIME_MINUTE)));
            mSun.setChecked(event.getInt(
            		event.getColumnIndexOrThrow(EventsDbAdapter.KEY_SUNDAY)));
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

        if (mRowId == null) {
            long id = mDbHelper.createEvent(title, runHour, runMin);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateEvent(mRowId, title, runHour, runMin);
        }
    }

}
