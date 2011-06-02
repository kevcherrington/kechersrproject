package org.kecher.alarmexample;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/*
 * Retrieved from http://justcallmebrian.com/?p=129 with minor modifications.
 */
public class AlarmExample extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		scheduleAlarm();
		Toast.makeText(getApplicationContext(), "AlarmScheduled",
				Toast.LENGTH_SHORT).show();
	}

	private void scheduleAlarm() {
		// get a Calendar object with current time
		Calendar cal = Calendar.getInstance();
		// add 5 minutes to the calendar object
		cal.add(Calendar.MINUTE, 1);
		Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
		intent.putExtra("alarm_message", "Hello World! The alarm receiver works");
		// In reality, you would want to have a static variable for the request code instead of 192837
		PendingIntent sender = PendingIntent.getBroadcast(this, 192837, intent,	PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	}
}