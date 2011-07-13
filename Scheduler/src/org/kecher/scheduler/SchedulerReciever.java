package org.kecher.scheduler;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SchedulerReciever extends BroadcastReceiver {

	private static final String TAG = "Reciever";
	private SchedulerService service = new SchedulerService();
	private EventsDbAdapter mDbAdapter;
	private ArrayList<Boolean> mWeekDays;
	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		if ((intent.getAction() != null) && 
				(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))) {
			Intent newIntent = new Intent(context, SchedulerService.class);
			newIntent.putExtra("message", "The Service is Running");
			service.startService(newIntent);
		} else {
			try {
				mDbAdapter = new EventsDbAdapter(context);
				mDbAdapter.open();
				
				Bundle bundle = intent.getExtras();
				
				Cursor event = mDbAdapter.fetchEvent(bundle.getLong(EventsDbAdapter.KEY_ROWID));
				String mode = event.getString(event.getColumnIndexOrThrow(EventsDbAdapter.KEY_MODE));
				int vol = event.getInt(event.getColumnIndexOrThrow(EventsDbAdapter.KEY_VOL));
				boolean vibe = (event.getInt(event.getColumnIndexOrThrow(EventsDbAdapter.KEY_SAT)) == 1)
					? true : false;
				String[] modes = context.getResources().getStringArray(R.array.ring_modes);
				
				if (mode.equals(modes[2])) { // Silent
					Log.d(TAG, "Changed to " + mode + " " + vol + " " + vibe);
					// turn off Ringer and Vibrate
				} else if (mode.equals(modes[1])) { // Vibrate
					Log.d(TAG, "Changed to " + mode + " " + vol + " " + vibe);
					// Set phone to vibrate and turn off ringer.
				} else if (mode.equals(modes[0])) { // Normal
					Log.d(TAG, "Changed to " + mode + " " + vol + " " + vibe);
					// Set the Volume and vibrate indicated.
				} else {
					throw new Exception("Ring Scheduler: invalid mode provided");
				}
				
				rescheduleEvent(bundle.getLong(EventsDbAdapter.KEY_ROWID));
				
			} catch (Exception e) {
				Toast.makeText(context,	"Ring Scheduler: An error occured",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	}
	
	private void rescheduleEvent(Long rowId) {
		Cursor event = mDbAdapter.fetchEvent(rowId);
			
		Calendar cal = Calendar.getInstance();
		
		cal.set(Calendar.HOUR_OF_DAY, event.getInt(event.getColumnIndexOrThrow(EventsDbAdapter.KEY_RUN_TIME_HOUR)));
		cal.set(Calendar.MINUTE, event.getInt(event.getColumnIndexOrThrow(EventsDbAdapter.KEY_RUN_TIME_MINUTE)));
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_SUN)) == 1) ? true : false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_MON)) == 1) ? true : false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_TUES)) == 1) ? true : false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_WED)) == 1) ? true : false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_THUR)) == 1) ? true : false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_FRI)) == 1) ? true : false);
		mWeekDays.add((event.getInt(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_SAT)) == 1) ? true : false);
		
		int curDay = cal.get(Calendar.DAY_OF_WEEK);
		curDay--;
		for (int i = 0; i < 7; i++) {
			if (mWeekDays.get((curDay + i) % 7)) {
				cal.add(Calendar.DAY_OF_MONTH, i);
				break;
			}
		}
		
		Intent intent = new Intent(mContext, SchedulerReciever.class);
		intent.putExtra(EventsDbAdapter.KEY_ROWID, rowId);
		
		PendingIntent sender = PendingIntent.getBroadcast(
				mContext, 123456, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

		mDbAdapter.updateEventRunTimes(rowId, (event.getLong(
				event.getColumnIndexOrThrow(EventsDbAdapter.KEY_NEXT_RUN))),
				cal.getTimeInMillis());
	}
}
