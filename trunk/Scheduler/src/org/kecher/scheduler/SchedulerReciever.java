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
		if ((intent.getAction() != null)
				&& (intent.getAction()
						.equals("android.intent.action.BOOT_COMPLETED"))) {
			Intent newIntent = new Intent(context, SchedulerService.class);
			newIntent.putExtra("message", "The Service is Running");
			Log.d(TAG, "Starting Scheduler Service.");
			context.startService(newIntent);
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
