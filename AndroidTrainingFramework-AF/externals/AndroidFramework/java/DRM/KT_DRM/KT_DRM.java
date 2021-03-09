#if USE_KT_DRM
package APP_PACKAGE;

import APP_PACKAGE.R;
import com.kaf.GeneralException;
import com.kaf.KafManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.kt.olleh.protection.ProtectionService;

// #if USE_INSTALLER
// import APP_PACKAGE.installer.GameInstaller;
// #endif
public class KT_DRM extends Activity 
{
	int ret;
	boolean initialized = false;
	/** Called when the activity is first created. */
	private ProgressDialog d;

    	@Override
    	public void onCreate(Bundle savedInstanceState) {
			if (!isTaskRoot()) // [A] Prevent App restart when open or 
			{
				DBG("KT", "!isTaskRoot");
				finish();
				return;
			}
    		d = new ProgressDialog(this);
			d.setCancelable(false);
			d.setMessage("initializing");
			d.show();
    		String result;

    		super.onCreate(savedInstanceState);
			// setContentView(R.layout.main);

    		if (!initialized) {
				ret = verifyApp(this);
				result = getResultText(ret);
				//result = KafManagerTest.DRM_Initialize(d.getContext(),
				//		KafManager.INIT_COPYRIGHT);
				d.dismiss();
				//showDialog("Initialize", result);
				showDialog(getString(R.string.KT_DRM_TITLE), result);
			}
			initialized = true;
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
			super.onStop();
		#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
			APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
		#endif
		}

    	private void showDialog(String title, String result) {
    		new AlertDialog.Builder(KT_DRM.this).setTitle(title).setMessage(
    				result).setNeutralButton("OK",
    				new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {

    						if (ret != ProtectionService.APP_REGAL) {
    							finish();
    						}
    						else {
							
							// #if !USE_INSTALLER
								Intent myIntent = new Intent(KT_DRM.this, CLASS_NAME.class);
							// #else
								// Intent myIntent = new Intent(KT_DRM.this, GameInstaller.class);
							// #endif
								startActivity(myIntent);
    							DBG("Test Initialize", "go to game : ");
								finish();
    						}

    					}
    				}
    				)
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
    				.show();
    	}
		
		private int verifyApp(Context context) {
			int ret = ProtectionService.UNKNOWN_ERROR;

			ProtectionService protectionService = ProtectionService.getProtection();
			try {
				ret = protectionService.verifyApp(this);
			} catch (Exception e) {
				e.printStackTrace();
				ret = ProtectionService.UNKNOWN_ERROR;
			} catch (Throwable e) {
				e.printStackTrace();
				ret = ProtectionService.UNKNOWN_ERROR;
			}

			return ret;
		}
	
		private String getResultText(int ret) {
			String text = "";
			switch (ret) {
			case ProtectionService.APP_REGAL:
				text = getString(R.string.KT_DRM_APP_REGAL);
				break;
			case ProtectionService.APP_ILLEGAL_COPY:
				text = getString(R.string.KT_DRM_APP_ILLEGAL_COPY);
				break;
			case ProtectionService.INVALID_CONTEXT:
				text = getString(R.string.KT_DRM_INVALID_CONTEXT);
				break;
			case ProtectionService.OLLEHMARKET_NOT_INSTALLED:
				text = getString(R.string.KT_DRM_OLLEHMARKET_NOT_INSTALLED);
				break;
			case ProtectionService.FUNC_INITIALIZING:
				text = getString(R.string.KT_DRM_FUNC_INITIALIZING);
				break;
			case ProtectionService.UNKNOWN_ERROR:
				text = getString(R.string.KT_DRM_UNKNOWN_ERROR);
				break;
			}

			return text;
		}
    }
	
	/*
    class KafManagerTest {
    	private static int m_iResult;

    	public static String DRM_Initialize(Context ct, int copylight) {
    		String sResult = "";
    		KafManager kafManager;
    		kafManager = KafManager.getInstance();

    		try {
    			m_iResult = kafManager.Initialize(ct, copylight);
    			// result = -3; //???? ???? ???? ?? ??? ??

    			if (m_iResult == 0) {

    				DBG("Test Initialize", "Initialize Success : ");
    				sResult = ct.getString(R.string.KT_DRM_R0_1);
    			} else if (m_iResult == -1) {

    				DBG("Test Initialize", "Initialize Fail");
    				sResult = ct.getString(R.string.KT_DRM_R1_1);
    			} else if (m_iResult == -2) {

    				DBG("Test Initialize", "Initialize Fail");
    				sResult = ct.getString(R.string.KT_DRM_R2_1);
    			} else if (m_iResult == -3) {

    				DBG("Test Initialize", "Initialize Fail");
    				sResult = ct.getString(R.string.KT_DRM_R3_1);
    			}

    		} catch (IllegalAccessException e) {
    			DBG_EXCEPTION(e);

    			ERR("Test Initialize", "IllegalAccessException");
    			sResult = "IllegalAccessException";
    		} catch (GeneralException e) {
    			DBG_EXCEPTION(e);

    			sResult = "GeneralException";
    			ERR("Test Initialize", "GeneralException");
    		}

    		return sResult;
    	}

    	public static int GetResult() {
    		return m_iResult;
    	}
		
}*/
#endif