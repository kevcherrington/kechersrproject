package org.kecher.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SchedulerReciever extends BroadcastReceiver {
	private static final String TAG = "SchedulerReciever";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Recieved an intent.");

		// This is a check to see if the intent is for a system boot completed.
		// if it is we will start the service.
		if ((intent.getAction() != null)
				&& (intent.getAction()
						.equals("android.intent.action.BOOT_COMPLETED"))) {
			Intent newIntent = new Intent(context, SchedulerService.class);
			newIntent.putExtra(SchedulerService.PHONE_BOOT, "Schedule all events");
			Log.d(TAG, "Starting Scheduler Service.");
			context.startService(newIntent);
		
		// Any intent that is not a system boot completed notification will be handled
		// by the SchedulerService as an intent to change the ring volume.
		} else {
			Bundle bundle = intent.getExtras();
			Log.d(TAG, "Intent to change Ringtone.");
			Log.d(TAG, context.getPackageName() + " " + bundle.size() + " " + bundle.keySet());
			Log.d(TAG, "KEY_ROWID " + bundle.getLong(EventsDbAdapter.KEY_ROWID));
			Log.d(TAG, "_id " + bundle.getLong("_id"));
            context.startService(new Intent(context, SchedulerService.class)
            		.putExtra(EventsDbAdapter.KEY_ROWID, bundle.getLong(EventsDbAdapter.KEY_ROWID))
    				.putExtra(SchedulerService.ADJUST_SOUND, true));
            
		}
	}
}
