package APP_PACKAGE.PackageUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class UtilsBatteryStateReceiver extends BroadcastReceiver
{
	/**
	 * Called at initialization to set the C variables keeping
	 * the battery information.
	 */
	public static void SetInitialBatteryState(Context ctx)
	{		
        Intent intent = ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		int chargeStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		int plugStatus   = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		int level        = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		
		boolean isCharging = chargeStatus == BatteryManager.BATTERY_STATUS_CHARGING || 
							 chargeStatus == BatteryManager.BATTERY_STATUS_FULL;
		
		boolean usbCharge = plugStatus == BatteryManager.BATTERY_PLUGGED_USB;
		boolean acCharge  = plugStatus == BatteryManager.BATTERY_PLUGGED_AC;		
		
		JNIBridge.SetBatteryInfo(isCharging, usbCharge, acCharge, level);
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		int chargeStatus = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		int plugStatus   = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		int level        = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);	
		
		boolean isCharging = chargeStatus == BatteryManager.BATTERY_STATUS_CHARGING || 
							 chargeStatus == BatteryManager.BATTERY_STATUS_FULL;
		
		boolean usbCharge = plugStatus == BatteryManager.BATTERY_PLUGGED_USB;
		boolean acCharge  = plugStatus == BatteryManager.BATTERY_PLUGGED_AC;
		
		// If intent is low battery, launch a low battery event
		if(intent.getAction() == Intent.ACTION_BATTERY_LOW)
		{
			JNIBridge.NotifyLowBattery();
		}
		
		JNIBridge.SetBatteryInfo(isCharging, usbCharge, acCharge, level);
	}
	
	// Gets the intent filter used by this state receiver
	public static IntentFilter getIntentFilter()
	{
		IntentFilter bif = new IntentFilter();
		
		bif.addAction(Intent.ACTION_BATTERY_LOW);          
        bif.addAction(Intent.ACTION_BATTERY_OKAY); 
		bif.addAction(Intent.ACTION_POWER_CONNECTED);  		
		bif.addAction(Intent.ACTION_POWER_DISCONNECTED);
		
		return bif;
	}
}
		
									   
		
		