package org.kecher.scheduler;


public class AlarmRec { //extends BroadcastReceiver {
//	private static final String TAG = "AlarmReceiver";
//
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		try {
//			AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//			Bundle bundle = intent.getExtras();
//		
//			int level = bundle.getInt("volume");
//			int vibrate = bundle.getInt("vibrate");
//			int curLevel = am.getStreamVolume(AudioManager.STREAM_RING);
//			int maxLevel = am.getStreamMaxVolume(AudioManager.STREAM_RING);
//		
//		
//			Log.d(TAG, "max volume level: " + maxLevel + " Current Level: " + curLevel);
//			Log.d(TAG, "percentage: " + (curLevel / maxLevel) * 100);
//		
//			am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//			am.setStreamVolume(AudioManager.STREAM_RING, maxLevel, vibrate);
//		} catch (Exception e) {
//			Log.d(TAG, "didn't work...");
//		}
//	}

}
