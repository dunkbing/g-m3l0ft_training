#if USE_LGU_DRM
package APP_PACKAGE;

#undef arm
#define arm arm

import APP_PACKAGE.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import com.lgt.arm.*;

// #if USE_INSTALLER
// import APP_PACKAGE.installer.GameInstaller;
// #endif
public class LGU_DRM extends Activity 
{
	private ArmInterface armInterface;
	private ArmInterfaceConnection armCon;
	private String resMsg;
	
	//Modify AID when you get the one from LG U+
	private String AID=LGU_DRM_AID;	

	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.main);
		if (!isTaskRoot()) // [A] Prevent App restart when open or 
		{
			DBG("LGU", "!isTaskRoot");
			finish();
			return;
		}
		runService();
	}
	
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
	//Call bindService Android API to interlock LG U+ DRM
	private boolean runService()
	{
		try
		{
			if(armCon == null)
			{
				armCon = new ArmInterfaceConnection();
				Boolean conRes= bindService(new Intent(ArmInterface.class.getName()),armCon, Context.BIND_AUTO_CREATE);
				if(conRes) return true;
			}
		}
		catch(Exception e)
		{
			DBG_EXCEPTION(e);
			releaseService();
		}
		
		releaseService();
		
		resMsg = getString(R.string.LGU_DRM_CHECK_FAIL);
		showDialog(0); //The error message would be dislplayed when the problem is happened.
		return false;
	}
	//Call unbindService Android API to be end.
	private void releaseService()
	{
		if(armCon != null)
		{
			unbindService(armCon);
			armCon = null;
		}
	}
	
	//the part of connecting ARM service.
	class ArmInterfaceConnection implements ServiceConnection 
	{
		public void onServiceConnected(ComponentName name, IBinder boundService) 
		{
			if(armInterface == null)
				armInterface = ArmInterface.Stub.asInterface((IBinder) boundService);
			
			
			try
			{
				int res = armInterface.executeArm(AID);
				
				if(res != 1)
					onArmResult(res);
				else
				{
					// #if !USE_INSTALLER
						Intent myIntent = new Intent(LGU_DRM.this, CLASS_NAME.class);
					// #else
						// Intent myIntent = new Intent(LGU_DRM.this, GameInstaller.class);
					// #endif
						startActivity(myIntent);
						finish();
				}	
				
			}
			catch(Exception e)
			{
				DBG_EXCEPTION(e);
				releaseService();
				return;
			}
			
			releaseService();
		}
		
		public void onServiceDisconnected(ComponentName name) 
		{
			armInterface = null;
		}
	}

	public void onArmResult(int res)
	{
		switch(res)
		{
			case 1:
				resMsg = getString(R.string.LGU_DRM_F01_1);
			break;
			case 0xF0000004:
				resMsg = getString(R.string.LGU_DRM_F04_1);
			break;
			case 0xF0000008:
				resMsg = getString(R.string.LGU_DRM_F08_1);
			break;
			case 0xF0000009:
				resMsg = getString(R.string.LGU_DRM_F09_1);
			break;
			case 0xF000000A:
				resMsg = getString(R.string.LGU_DRM_F0A_1);
			break;
			case 0xF000000C:
				resMsg = getString(R.string.LGU_DRM_F0C_1);
			break;
			case 0xF000000D:
				resMsg = getString(R.string.LGU_DRM_F0D_1);
			break;
			case 0xF000000E:
				resMsg = getString(R.string.LGU_DRM_F0E_1);
			break;
			case 0xF0000011:
				resMsg = getString(R.string.LGU_DRM_F11_1);
			break;
			case 0xF0000012:
				resMsg = getString(R.string.LGU_DRM_F12_1);
			break;
			case 0xF0000013:
				resMsg = getString(R.string.LGU_DRM_F13_1);
			break;
			case 0xF0000014:
				resMsg = getString(R.string.LGU_DRM_F14_1);
			break;
			default:
				resMsg = getString(R.string.LGU_DRM_DEF_1);
			break;
		}
		showDialog(0);
	}
	
	protected Dialog onCreateDialog(int id) 
	{
		return new AlertDialog.Builder( this )
		//.setIcon(R.drawable.icon).setCancelable(false) or .setIcon(null) if you don't want show icon
		.setTitle(getString(R.string.LGU_DRM_TITLE_1))				
		.setMessage(resMsg)
		.setPositiveButton(getString(R.string.LGU_DRM_TITLE_2), new DialogInterface.OnClickListener() { 
					public void onClick(DialogInterface dialog, int whichButton) {					
						finish();		
					}
				})
				.setOnKeyListener(new DialogInterface.OnKeyListener(){
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent keyevent) {
					if (keyevent.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyevent.getKeyCode()) {
						case KeyEvent.KEYCODE_BACK:
							return true;
						case KeyEvent.KEYCODE_SEARCH:
							return true;
						default:
							break;
						}
					}
					return false;
				}

			}).create();
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
	protected void onStop() {
	      //TODO Auto-generated method stub
	         super.onStop();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
	         this.finish();
	}

	@Override
	protected void onResume() {
		//TODO Auto-generated method stub
		super.onResume();
	}
}
#endif