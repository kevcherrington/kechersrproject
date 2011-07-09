package org.kecher.scheduler;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class SchedulerService extends IntentService{

	public SchedulerService () {
		super("SchedulerService");
	}
	
	@Override
	protected void onHandleIntent (Intent intent) {
		// TODO finish this method
	}
	
	@Override
	public ComponentName startService(Intent intent) {
		Context context = getApplicationContext();
		ComponentName name = new ComponentName(context.getPackageName(), "SchedulerService");
		Bundle bundle = intent.getExtras();
		String message = bundle.getString("message");
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		
		return name;
	}
}
