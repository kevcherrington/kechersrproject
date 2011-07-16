package org.kecher.scheduler;

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
import android.widget.Toast;

public class SchedulerReciever extends BroadcastReceiver {

	private static final String TAG = "Reciever";
	// private SchedulerService service = new SchedulerService();
	private EventsDbAdapter mDbAdapter;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if ((intent.getAction() != null)
				&& (intent.getAction()
						.equals("android.intent.action.BOOT_COMPLETED"))) {
			Intent newIntent = new Intent(context, SchedulerService.class);
			newIntent.putExtra("message", "The Service is Running");
			Log.d(TAG, "If I wanted a service I should start it now.");
			// service.startService(newIntent);
		} else {
			try {
				mDbAdapter = new EventsDbAdapter(context);
				mDbAdapter.open();

				AudioManager adoMngr = (AudioManager) context
						.getSystemService(Context.AUDIO_SERVICE);

				Bundle bundle = intent.getExtras();

				Cursor event = mDbAdapter.fetchEvent(bundle
						.getLong(EventsDbAdapter.KEY_ROWID));
				String mode = event.getString(event
						.getColumnIndexOrThrow(EventsDbAdapter.KEY_MODE));
				int vol = event.getInt(event
						.getColumnIndexOrThrow(EventsDbAdapter.KEY_VOL));
				boolean vibe = (event.getInt(event
						.getColumnIndexOrThrow(EventsDbAdapter.KEY_VIBRATE)) == 1) ? true
						: false;
				String[] modes = context.getResources().getStringArray(
						R.array.ring_modes);

				if (mode.equals(modes[2])) { // Silent
					Log.d(TAG, "Changed to " + mode + " " + vol + " " + vibe);
					adoMngr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					adoMngr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
							AudioManager.VIBRATE_SETTING_OFF);
					adoMngr.setVibrateSetting(
							AudioManager.VIBRATE_TYPE_NOTIFICATION,
							AudioManager.VIBRATE_SETTING_OFF);
					// adoMngr.setStreamVolume(AudioManager.STREAM_RING, 0,
					// AudioManager.FLAG_SHOW_UI);
					// turn off Ringer and Vibrate
				} else if (mode.equals(modes[1])) { // Vibrate
					Log.d(TAG, "Changed to " + mode + " " + vol + " " + vibe);
					adoMngr.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
					
					// Set phone to vibrate and turn off ringer.
				} else if (mode.equals(modes[0])) { // Normal
					Log.d(TAG, "Changed to " + mode + " " + vol + " " + vibe);
					adoMngr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					
					int volLvl = adoMngr.getStreamMaxVolume(AudioManager.STREAM_RING);
					adoMngr.setStreamVolume(AudioManager.STREAM_RING, ((int)(volLvl * (vol * .01))), 
							AudioManager.FLAG_SHOW_UI);
					
					int noteLvl = adoMngr.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
					adoMngr.setStreamVolume(AudioManager.STREAM_NOTIFICATION, ((int)(noteLvl * (vol * .01))),
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
					throw new Exception("Ring Scheduler: invalid mode provided");
				}

				rescheduleEvent(bundle.getLong(EventsDbAdapter.KEY_ROWID));
				event.close();
			} catch (Exception e) {
				Toast.makeText(context, "Ring Scheduler: An error occured",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} finally { // Make sure to release resources
				Log.d(TAG, "closing DB");
				mDbAdapter.close();
				Log.d(TAG, "DB has been CLOSED.");
			}
		}
	}
}
