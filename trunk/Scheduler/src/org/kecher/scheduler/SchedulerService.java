package org.kecher.scheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class SchedulerService extends Service {

	private static final String TAG = "SchedulerService";
	
	// "flag" used to check if the intent is meant to adjust the ringtone.
	protected static final String ADJUST_SOUND = "adjustSound";
	
	// "flag" used to check if the phone just booted and events need
	// to be scheduled.
	protected static final String PHONE_BOOT = "phoneBooted";

	// used to keep track of what days an event should be executed on.
	private ArrayList<Boolean> mWeekDays;
	
	private EventsDbAdapter mDbAdapter;
	private boolean mDbIsOpen;

	private NotificationManager mNM;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.service_started;

	/**
	 * Class for clients to access.
	 */
	public class LocalBinder extends Binder {
		SchedulerService getService() {
			return SchedulerService.this;
		}
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Display a notification about us starting and put an icon in the
		// status bar.
		showNotification();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Received start id " + startId + " intent: " + intent);
		Bundle bundle = intent.getExtras();
		Log.d(TAG, "Keys " + bundle.keySet());

		if (bundle.containsKey(ADJUST_SOUND)) {
			Log.d(TAG, "onStartCommand() KEY_ROWID " + bundle.getLong("_id"));
			adjustSoundSettings(bundle.getLong(EventsDbAdapter.KEY_ROWID));
		} else if (bundle.containsKey(PHONE_BOOT)) {
			Log.d(TAG, "Fresh boot schedule all events.");
			scheduleAllEvents();
		}

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		mNM.cancel(NOTIFICATION);
		mDbAdapter.close();
		mDbIsOpen = false;

		// Tell the user we stopped.
		Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients.
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// Use the same text for the ticker and the expanded notification
		CharSequence text = getText(R.string.service_started);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(
				R.drawable.notification_icon, text, System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, Scheduler.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.service_label),
				text, contentIntent);

		// Send the notification.
		mNM.notify(NOTIFICATION, notification);
	}

	private void scheduleAllEvents() {
		List<Long> rowIds = new ArrayList<Long>();
		
		if (!mDbIsOpen) {
			mDbAdapter = new EventsDbAdapter(getApplicationContext());
			mDbAdapter.open();
			mDbIsOpen = true;
		}
		
		// get all the row Ids. Then pass them to the event scheduler.
		Cursor events = mDbAdapter.fetchAllEvents();
		events.moveToFirst();
		
		// Use two for loops because scheduleEvent() will close the connection to the DB
		// every time. using two for loops allows us to collect all row Ids before
		// our connection to the DB is lost.
		for (int i = 1; i <= events.getCount(); i ++) {
			rowIds.add(events.getLong(events.getColumnIndexOrThrow(EventsDbAdapter.KEY_ROWID)));
			events.moveToNext();
		}
		
		// Cursor is no longer needed so Close it.
		events.close();
		
		for (Long rowId : rowIds) {
			scheduleEvent(rowId);
		}
	}

	/*
	 * Reschedule the event for another day. To avoid looping for the minute the
	 * event is executed set the reschedule for a minimum of tomorrow. We don't
	 * want the same event happening more than once a day.
	 */
	protected void rescheduleEvent(Long rowId) {
		// Open connection to the DB.
		if (!mDbIsOpen) {
			mDbAdapter = new EventsDbAdapter(getApplicationContext());
			mDbAdapter.open();
			mDbIsOpen = true;
		}
		
		Cursor event = mDbAdapter.fetchEvent(rowId);

		Calendar cal = Calendar.getInstance();

		cal.add(Calendar.DAY_OF_WEEK, 1); // Because the same event can only be
											// scheduled once per day.

		cal.set(Calendar.HOUR_OF_DAY, event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_RUN_TIME_HOUR)));
		cal.set(Calendar.MINUTE, event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_RUN_TIME_MINUTE)));
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		mWeekDays = new ArrayList<Boolean>();

		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_SUN)) == 1) ? true
				: false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_MON)) == 1) ? true
				: false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_TUES)) == 1) ? true
				: false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_WED)) == 1) ? true
				: false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_THUR)) == 1) ? true
				: false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_FRI)) == 1) ? true
				: false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_SAT)) == 1) ? true
				: false);

		int curDay = cal.get(Calendar.DAY_OF_WEEK);
		curDay--;
		for (int i = 0; i < 7; i++) {
			if (mWeekDays.get((curDay + i) % 7)) {
				cal.add(Calendar.DAY_OF_MONTH, i);
				break;
			}
		}

		Intent intent = new Intent(getApplicationContext(),
				SchedulerReciever.class);
		intent.putExtra(EventsDbAdapter.KEY_ROWID, rowId);

		PendingIntent sender = PendingIntent.getBroadcast(
				getApplicationContext(), 123456, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

		// move next run to last run and update next run with the calculated
		// calendar time.
		mDbAdapter.updateEventRunTimes(rowId, (event.getLong(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_NEXT_RUN))), cal
				.getTimeInMillis());

		// Log this for convenience so we know when it is scheduled to run next.
		Log.d(TAG, "Reschedule: Next Runtime - " + cal.toString());

		// Close connections to the DB to avoid leaks.
		event.close();
		mDbAdapter.close();
		mDbIsOpen = false;
	}

	/*
	 * This is used only to schedule an event for the first time. We don't have
	 * to worry about a scheduling loop here.
	 */
	protected void scheduleEvent(Long rowId) {
		// Open connection to the DB.
		if (!mDbIsOpen) {
			mDbAdapter = new EventsDbAdapter(getApplicationContext());
			mDbAdapter.open();
			mDbIsOpen = true;
		}

		Cursor event = mDbAdapter.fetchEvent(rowId);

		mWeekDays = new ArrayList<Boolean>();

		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_RUN_TIME_HOUR)));
		cal.set(Calendar.MINUTE, event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_RUN_TIME_MINUTE)));
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_SUN)) == 1) ? true
				: false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_MON)) == 1) ? true
				: false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_TUES)) == 1) ? true
				: false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_WED)) == 1) ? true
				: false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_THUR)) == 1) ? true
				: false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_FRI)) == 1) ? true
				: false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_SAT)) == 1) ? true
				: false);

		int curDay = cal.get(Calendar.DAY_OF_WEEK);
		curDay--;
		for (int i = 0; i < 7; i++) {
			if (mWeekDays.get((curDay + i) % 7)) {
				cal.add(Calendar.DAY_OF_MONTH, i);
				break;
			}
		}

		Intent intent = new Intent(getApplicationContext(),
				SchedulerReciever.class);
		intent.putExtra(EventsDbAdapter.KEY_ROWID, rowId);

		PendingIntent sender = PendingIntent.getBroadcast(
				getApplicationContext(), 123456, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
		Log.d(TAG, "Created new event.");

		mDbAdapter.updateEventRunTimes(rowId, 0L, cal.getTimeInMillis());

		// present for convenience.
		Log.d(TAG, "Schedule: Next Run Time - " + cal.toString());

		// Close connections to the DB to avoid leaks.
		event.close();
		mDbAdapter.close();
		mDbIsOpen = false;
	}

	/*
	 * To cancel the event we must recreate the Intent that was passed. When the
	 * matching Intent is found it will then be deleted.
	 */
	protected void removeIntent(Long id) {
		Intent intent = new Intent(getApplicationContext(),
				SchedulerReciever.class);
		intent.putExtra(EventsDbAdapter.KEY_ROWID, id);

		PendingIntent sender = PendingIntent.getBroadcast(
				getApplicationContext(), 123456, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		am.cancel(sender);
		Log.d(TAG, "Removed intent for item " + id);
	}

	protected void adjustSoundSettings(Long rowId) {
		// Open connection to Db.
		if (!mDbIsOpen) {
			mDbAdapter = new EventsDbAdapter(getApplicationContext());
			mDbAdapter.open();
			mDbIsOpen = true;
		}
		
		Cursor event;
		
		try {
			Log.d(TAG, "Event Row Id " + rowId);

			AudioManager adoMngr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

			event = mDbAdapter.fetchEvent(rowId);

			String mode = event.getString(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_MODE));

			int vol = event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_VOL));

			boolean vibe = (event.getInt(event
					.getColumnIndexOrThrow(EventsDbAdapter.KEY_VIBRATE)) == 1) ? true
					: false;

			String[] modes = getResources().getStringArray(R.array.ring_modes);

			if (mode.equals(modes[2])) { // Silent
				Log.d(TAG, "Changed to " + mode + " " + vol + " " + vibe);

				adoMngr.setRingerMode(AudioManager.RINGER_MODE_SILENT);

				adoMngr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
						AudioManager.VIBRATE_SETTING_OFF);

				adoMngr.setVibrateSetting(
						AudioManager.VIBRATE_TYPE_NOTIFICATION,
						AudioManager.VIBRATE_SETTING_OFF);

			} else if (mode.equals(modes[1])) { // Vibrate
				Log.d(TAG, "Changed to " + mode + " " + vol + " " + vibe);

				adoMngr.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

			} else if (mode.equals(modes[0])) { // Normal
				Log.d(TAG, "Changed to " + mode + " " + vol + " " + vibe);

				adoMngr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

				int volLvl = adoMngr
						.getStreamMaxVolume(AudioManager.STREAM_RING);

				adoMngr.setStreamVolume(AudioManager.STREAM_RING,
						((int) (volLvl * (vol * .01))),
						AudioManager.FLAG_SHOW_UI);

				int noteLvl = adoMngr
						.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

				adoMngr.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
						((int) (noteLvl * (vol * .01))),
						AudioManager.FLAG_SHOW_UI);

				if (vibe) {
					adoMngr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
							AudioManager.VIBRATE_SETTING_ON);

				} else {
					adoMngr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
							AudioManager.VIBRATE_SETTING_OFF);
				}

			} else {
				throw new Exception("Ring Scheduler: invalid mode provided");
			}

			rescheduleEvent(rowId);

			event.close();

		} catch (Exception e) {

			Log.e(TAG, "", e);

			Toast.makeText(getApplicationContext(),
					"Ring Scheduler: An error occured", Toast.LENGTH_SHORT)
					.show();

			e.printStackTrace();

		} finally { // Make sure to release resources
			Log.d(TAG, "closing DB");

			mDbAdapter.close();
			mDbIsOpen = false;

			Log.d(TAG, "DB has been CLOSED.");
		}
	}
}