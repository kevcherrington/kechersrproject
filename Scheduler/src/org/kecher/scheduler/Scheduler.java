package org.kecher.scheduler;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Scheduler extends Activity {
//	private Handler mHandler = new Handler();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		scheduleAlarm();
		silenceRinger();
		Toast.makeText(getApplicationContext(), "AlarmScheduled",
				Toast.LENGTH_SHORT).show();

//        mHandler.removeCallbacks(showToast);
//        mHandler.postDelayed(showToast, 10000);
    }

	private void silenceRinger() {
		AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
	
		am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
	}

	private void scheduleAlarm() {
		// get a Calendar object with current time
		Calendar cal = Calendar.getInstance();
		// add 5 minutes to the calendar object
		cal.add(Calendar.MINUTE, 1);
		Intent intent = new Intent(getApplicationContext(), AlarmRec.class);
		intent.putExtra("volume", new Integer(100));
		intent.putExtra("vibrate", new Integer(1));
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	}

//    private Runnable showToast = new Runnable() {
//    	public void run() {
//    		Context context = getApplicationContext();
//    		Toast toast = Toast.makeText(context, "Hello World", Toast.LENGTH_LONG);
//    		toast.show();
//    	}
//    };
}