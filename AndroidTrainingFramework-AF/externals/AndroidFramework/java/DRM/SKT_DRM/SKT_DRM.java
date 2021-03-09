
#if USE_SKT_DRM
package APP_PACKAGE;

#undef arm
#define arm arm

import APP_PACKAGE.R;
import com.skt.arm.aidl.IArmService;
import com.skt.arm.ArmListener;
import com.skt.arm.ArmManager;
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
import java.lang.String;
import android.app.ProgressDialog;

// #if USE_INSTALLER
// import APP_PACKAGE.installer.GameInstaller;
// #endif

public class SKT_DRM extends Activity implements ArmListener
{
	private String 	resMsg;	
	private String AID=SKT_DRM_AID;
	
	private ArmManager		arm;
	private ProgressDialog pDlg;
	
	private static int res = -1;
    /** Called when the activity is first created. */
		
		@Override
		public void onCreate(Bundle savedInstanceState) 
		{		
			super.onCreate(savedInstanceState);			
			
			if (!isTaskRoot()) // [A] Prevent App restart when open or 
			{
				DBG("SKT", "!isTaskRoot");
				finish();
				return;
			}
			
			runArmService();
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
		protected void onDestroy()
		{
			super.onDestroy();
		}

	private boolean runArmService()
	{
		try
		{
			pDlg = ProgressDialog.show(SKT_DRM.this, getString(R.string.DRM_LOADING_TITLE), getString(R.string.DRM_LOADING_MESSAGE));
			
			arm = null;
    		arm = new ArmManager(SKT_DRM.this);
    		arm.setArmListener(SKT_DRM.this);
    		arm.ARM_Plugin_ExecuteARM(AID);
		}
		catch(Exception e)
		{
			DBG_EXCEPTION(e);
		}
		return false;		
	}
	
	public void onArmResult()
	{
		pDlg.dismiss();
		
		if (arm.nNetState == SERVICE_CONNECT)
		{
			// #if !USE_INSTALLER
				Intent myIntent = new Intent(SKT_DRM.this, CLASS_NAME.class);
			// #else
				// Intent myIntent = new Intent(SKT_DRM.this, GameInstaller.class);
			// #endif
				startActivity(myIntent);
				finish();
		}
		else
		{
			//SERVICE_FAIL:
			//SERVICE_NOT_EXIST:
			resMsg = getString(R.string.DRM_CHECK_FAIL);
			showDialog(0);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) 
	{
		return new AlertDialog.Builder(this)
				.setIcon(null)
				.setTitle(getString(R.string.DRM_TITLE_1))				
				.setMessage(resMsg)
				.setPositiveButton(getString(R.string.DRM_TITLE_2), new DialogInterface.OnClickListener() { 
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
#endif