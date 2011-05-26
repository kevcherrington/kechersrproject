package org.kecher.scheduler;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class Scheduler extends Activity {
	private Handler mHandler = new Handler();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mHandler.removeCallbacks(showToast);
        mHandler.postDelayed(showToast, 10000);
    }

    private Runnable showToast = new Runnable() {
    	public void run() {
    		Context context = getApplicationContext();
    		Toast toast = Toast.makeText(context, "Hello World", Toast.LENGTH_LONG);
    		toast.show();
    	}
    };
}