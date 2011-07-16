package org.kecher.scheduler;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class SchedulerService extends Service{
	
	private ArrayList<Boolean> mWeekDays;
	private EventsDbAdapter mDbAdapter;

    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.service_started;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        SchedulerService getService() {
            return SchedulerService.this;
        }
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.notification_icon, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Scheduler.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.service_label),
                       text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }
    
	private void rescheduleEvent(Long rowId) {
		Cursor event = mDbAdapter.fetchEvent(rowId);

		Calendar cal = Calendar.getInstance();

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

		Intent intent = new Intent(getApplicationContext(), SchedulerReciever.class);
		intent.putExtra(EventsDbAdapter.KEY_ROWID, rowId);

		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 123456,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

		mDbAdapter.updateEventRunTimes(rowId, (event.getLong(event
				.getColumnIndexOrThrow(EventsDbAdapter.KEY_NEXT_RUN))), cal
				.getTimeInMillis());
		
		event.close();
	}

}