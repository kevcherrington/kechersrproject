package org.kecher.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SchedulerReciever extends BroadcastReceiver {

	SchedulerService service = new SchedulerService();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if ((intent.getAction() != null) && 
				(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))) {
			Intent newIntent = new Intent(context, SchedulerService.class);
			newIntent.putExtra("message", "The Service is Running");
			service.startService(newIntent);
		}
	}
}
