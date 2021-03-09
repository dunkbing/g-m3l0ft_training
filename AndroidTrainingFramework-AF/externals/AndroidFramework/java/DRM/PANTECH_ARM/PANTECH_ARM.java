
#if USE_PANTECH_ARM
package APP_PACKAGE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.pantech.appcross.armagent.ArmListener;
import com.pantech.appcross.armagent.ArmManager;
import APP_PACKAGE.GLUtils.Device;

// #if USE_INSTALLER
// import APP_PACKAGE.installer.GameInstaller;
// #endif
import android.content.Intent;


public class PANTECH_ARM extends Activity implements ArmListener
{
	private ArmManager armManager;
	private String CID = PANTECH_STORE_CID;//"C111340340";
	private String DID = "" ; 
	SET_TAG("PANTECH_ARM");
	private static String Msg;

	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		if (!isTaskRoot()) // [A] Prevent App restart when open or 
		{
			DBG(TAG, "!isTaskRoot");
			finish();
			return;
		}
        runArmService();        
    }
    
    
	private void runArmService()
	{
		try
		{
		#if !RELEASE_VERSION			
			DBG(TAG, "_____________ runArmService " + DID + "   " + CID);
		#endif// !RELEASE_VERSION
		
			DID = Device.getDeviceId();
			armManager = new ArmManager(PANTECH_ARM.this);
			armManager.setArmListener(PANTECH_ARM.this);
			armManager.executeArm(DID, CID);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
    
    
    @Override
	public void onArmResult()
	{
	#if !RELEASE_VERSION
		DBG(TAG, "_____________ onArmResult [" + armManager.nStatus + "]");
	#endif// !RELEASE_VERSION
		
		
		
		switch (armManager.nStatus)
		{
		case SERVICE_CONNECT:
		#if !RELEASE_VERSION
			DBG(TAG, "_____________ SERVICE_CONNECT");
		#endif// !RELEASE_VERSION
			/*
			 * ARMService not spawn pop-up window when the connection is successful, run Application properly.
			 */
			//Main display logic (actually, depending on the nature of the application for information on the initial starting place must be running on).
			// setContentView(R.layout.arm_main);
			
		// #if !USE_INSTALLER
			Intent myIntent = new Intent(PANTECH_ARM.this, CLASS_NAME.class);
		// #else
			// Intent myIntent = new Intent(PANTECH_ARM.this, GameInstaller.class);
		// #endif
			startActivity(myIntent);
			finish();						
			
			
			break;

		case SERVICE_FAIL:
			/*
			 * ARMService the Application is not installed error message after you kill the Float pop-up window.
			 */
			// Msg = armManager.sResMsg;
			
		#if !RELEASE_VERSION
			DBG(TAG, "_____________ SERVICE_FAIL GLGame.Exit: " + Msg);
		#endif// !RELEASE_VERSION
			
			this.finish();
			break;
		}
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
	#endif
	}
	
	@Override
	protected void onStop() 
	{
		//TODO Auto-generated method stub
		super.onStop();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
		this.finish();
	}

	@Override
	protected void onResume() 
	{
		//TODO Auto-generated method stub
		super.onResume();
	}
}
#endif //USE_PANTECH_ARM