package APP_PACKAGE;

import APP_PACKAGE.PackageUtils.*;
import APP_PACKAGE.PackageUtils.Dispatchers.*;

#if SIMPLIFIED_PN
import APP_PACKAGE.PushNotification.*;
#endif

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;
import android.content.Intent;
import android.view.KeyEvent;
import android.app.AlarmManager;//for restarting the activity
import android.app.PendingIntent;//for restarting the activity
import android.content.Context;
import android.util.DisplayMetrics;//for orientation purposes
import android.content.pm.ActivityInfo;//for orientation purposes
import android.os.Build;//for orientation purposes

#if USE_INSTALLER
	import APP_PACKAGE.installer.GameInstaller;
	import APP_PACKAGE.GLUtils.GLConstants; 
#endif //USE_INSTALLER

import APP_PACKAGE.GLUtils.LowProfileListener;


public class MainActivity extends Activity implements SurfaceHolder.Callback
{
    private static String 			TAG = "ACP_LOGGER";
	
	private boolean 				m_bRestartApplicationOnExit = false;

    private RelativeLayout          mFrameLayout = null;
    //private SurfaceView             m_SurfaceView = null; //johny comment
    private GLSurfaceView           m_SurfaceView = null;
    private InputDispatcher         m_InputDispatcher = null;
    private SignalDispatcher        m_SignalDispatcher = null;
	
	private boolean					m_bApplicationDidFinishLaunching;
	
	boolean 						m_bLandscapeApplication = false;
	
	// Required by many-many modules
	public static MainActivity m_sInstance = null;//added for compatibility

	private void InitializeJavaModules()
	{
		// Handle the input
        m_InputDispatcher = new InputDispatcher();     
		
		// Initialize the plugin system
		m_SignalDispatcher = new SignalDispatcher();
        m_SignalDispatcher.StartDispatcher(this, mFrameLayout);  
		
		
		//here you can add calls to modules that only need an init called (like DataSharing), and don't require a plugin.
	}
	
	private void InitializeNativeModules()
	{
		JNIBridge.NativeInit();
	}
	
	private void InitializeAll()
	{
		//no java module should call directly the native code. the java code should will only run on demand from native.
		InitializeJavaModules();
		InitializeNativeModules();
		
		LowProfileListener.ActivateImmersiveMode(this);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
		
		//hack for launching the application from different places then the launcher.
		if (!isTaskRoot()) 
    	{
    		Intent intent = getIntent();
    	    String intentAction = intent.getAction();
			if( intentAction != null && intentAction.equals(Intent.ACTION_MAIN) )
            {
    	    	finish();
				return;
    	    }
    	}
		
		#if AMAZON_STORE
		//very big hack: 
		//Amazon Social (GameCircle) requires the AmazonGames so to be loaded before the game so
		//This hack considers that AmazonGamesJni is present. And although not needed by ACP, it needs to be part of the entry point.
		//If, at any time, the Social implementation doesn't require the Amazon lib to be loaded in the entry point, it should be moved to the SocialLib plugin!
				System.loadLibrary("AmazonGamesJni");
		//If anyone finds any issues because of this code, please contact Android-Tools Support!
		#endif
		
		// Bind own reference
		m_sInstance = this;
		
		//for Orientation purposes:
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		
		m_bLandscapeApplication = (width > height);
		
		SetOrientationLock(true);
		
		//m_bApplicationDidFinishLaunching will be set true when installer has finished downloading
		//if a non-installer build, do it on onCreate
		m_bApplicationDidFinishLaunching = false;

		// Load native code (Andu: I really think it's the sooner the better...)
		System.loadLibrary(SO_LIB_FILE);
		
		// Create surface and layout
        mFrameLayout = new RelativeLayout(this);
        //m_SurfaceView = new SurfaceView(this); //johny comment
        m_SurfaceView = new ESUtilGLSurfaceView(this);

        // Set view
		m_SurfaceView.setEnabled(true);
		m_SurfaceView.setFocusable(true);
		m_SurfaceView.setFocusableInTouchMode(true);
		
        m_SurfaceView.getHolder().addCallback(this);
        mFrameLayout.addView(m_SurfaceView);
        setContentView(mFrameLayout);
		
		
		
		#if !USE_INSTALLER
		InitializeAll();
		m_bApplicationDidFinishLaunching = true;
		#endif
    }
	
	private void KillApplication() 
	{
		if(m_bRestartApplicationOnExit) 
		{
			AlarmManager mgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
			
			PendingIntent RESTART_INTENT = PendingIntent.getActivity( this, 0, new Intent(this.getIntent()),  Intent.FLAG_ACTIVITY_CLEAR_TASK);
			
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 900, RESTART_INTENT);
		}
		
		if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ECLAIR_MR1)
		{
			int pid = android.os.Process.myPid();
			android.os.Process.killProcess(pid); 
		}
		else
		{
			System.exit(0);
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus)
		{
			LowProfileListener.ActivateImmersiveMode(this);
		}
	}
	
    @Override
    protected void onResume() 
    {
        super.onResume();

        if(m_bApplicationDidFinishLaunching)
		{
			m_SignalDispatcher.OnResume();
		}
		else
		{
		#if USE_INSTALLER
			try
			{
				Intent i = new Intent();
				i.setClassName(STR_APP_PACKAGE, STR_APP_PACKAGE + ".installer.GameInstaller");
				i.putExtras(getIntent());
				startActivityForResult(i, GLConstants.INSTALLER_ACTIVITY_NUMBER);
			} 
			catch(Exception e)
			{
				Log.e("ACP_LOGGER", "Starting Installer caused an exception:");
				e.printStackTrace();
			}
		#endif //USE_INSTALLER
		}
    }
    
    @Override
    protected void onPause() 
    {
    	super.onPause();
		
        if(m_bApplicationDidFinishLaunching)
		{
			m_SignalDispatcher.OnPause();
		}
		
		if(isFinishing() == true)
    	{
			m_bApplicationDidFinishLaunching = false;
			//finish() has been called. 
			//Killing the application here, so that the OS handles the static variables
    		KillApplication();
    	}
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{	
		if(m_bApplicationDidFinishLaunching)
		{
			m_SignalDispatcher.OnActivityResult(requestCode, resultCode, data);
		}
		else
		{
		#if USE_INSTALLER
			if (requestCode == GLConstants.INSTALLER_ACTIVITY_NUMBER)
			{
				if (resultCode == GLConstants.ACTIVITY_RESULT_OK)
				{
					InitializeAll();
					m_bApplicationDidFinishLaunching = true;
				}
				else
				{
					finish();
					KillApplication();
				}
			}
		#endif //USE_INSTALLER
		}
	
	}
	
    @Override
    public boolean onTouchEvent(final MotionEvent ev)
    {
        if(m_bApplicationDidFinishLaunching && m_InputDispatcher.OnTouchEvent(ev))
        {
            return true;
        }

        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent ev)
    {
        if(m_bApplicationDidFinishLaunching && m_InputDispatcher.OnGenericMotionEvent(ev))
        {
            return true;
        }

        return super.onGenericMotionEvent(ev);
    }

    @Override
    public boolean onKeyDown(final int keyCode, KeyEvent event)
    {
		//hack to fix the volume key for Immersive Mode
		LowProfileListener.onKeyDown(this, keyCode);
		
        if(m_bApplicationDidFinishLaunching && m_InputDispatcher.OnKeyDown(keyCode,event))
        {
            return true;
        }

        return super.onKeyDown(keyCode,event);
    }

    @Override
    public boolean onKeyUp(final int keyCode, KeyEvent event)
    {		
        if(m_bApplicationDidFinishLaunching && m_InputDispatcher.OnKeyUp(keyCode,event))
        {
            return true;
        }
		
        return super.onKeyUp(keyCode,event);
    }
   
    
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) 
    {
		if(m_bApplicationDidFinishLaunching)
		{
			JNIBridge.NativeSurfaceChanged(holder.getSurface(), w, h);
		}
    }
	

    public void surfaceCreated(SurfaceHolder holder) 
    {
    	//JNIBridge.NativeSurfaceCreated(holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) 
    {
		if(m_bApplicationDidFinishLaunching)
		{
			JNIBridge.NativeSurfaceChanged(null, 0, 0);
		}
    }
	
	// Required by many-many modules :(
	public static Activity getActivityContext() 
	{
		return m_sInstance;
	}
	
	public void MinimizeApplication()
	{
		runOnUiThread
		(
			new Runnable() 
			{
				public void run() 
				{
					moveTaskToBack(true);
				}
			}
		);
	}
	
	public void ExitApplication(boolean restart)
	{
		m_bRestartApplicationOnExit = restart;
		
		 runOnUiThread(
			new Runnable() 
			{
				public void run() 
				{
					finish();	
				}
			}
		);
	}
	
	private void SetScreenOn(boolean screenOn)
	{
		m_SurfaceView.setKeepScreenOn(screenOn);
	}
	
	private int GetCurrentScreenOrientation() 
	{
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		
		
		int orientation;
		// if the device's natural orientation is portrait:
		
		if ((rotation == Surface.ROTATION_0
				|| rotation == Surface.ROTATION_180) && height > width ||
			(rotation == Surface.ROTATION_90
				|| rotation == Surface.ROTATION_270) && width > height) 
		{
			switch(rotation) 
			{
				case Surface.ROTATION_0:
					orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					break;
				case Surface.ROTATION_90:
					orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
					break;
				case Surface.ROTATION_180:
					orientation =
						ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
					break;
				case Surface.ROTATION_270:
					orientation =
						ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
					break;
				default:
					orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					break;              
			}
		}
		// if the device's natural orientation is landscape or if the device
		// is square:
		else 
		{
			switch(rotation) 
			{
				case Surface.ROTATION_0:
					orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
					break;
				case Surface.ROTATION_90:
					orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					break;
				case Surface.ROTATION_180:
					orientation =
						ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
					break;
				case Surface.ROTATION_270:
					orientation =
						ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
					break;
				default:
					orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
					break;              
			}
		}

		return orientation;
	}
		
	public void SetOrientationLock(boolean lock)
	{
		if(lock == false) //locking current orientation
		{
			setRequestedOrientation(GetCurrentScreenOrientation());
		}
		else if(m_bLandscapeApplication)
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
			{
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE); //Put it simply when use API >= 18 :)
			}
			else
			{		
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			}
		}
		else		
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
			{
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT); //Put it simply when use API >= 18 :)
			}
			else
			{		
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
			}
		}
	}
	
	public void SetKeepScreenOn(final boolean keepScreenOn)
	{
		runOnUiThread(
			new Runnable() 
			{
				public void run() 
				{
					SetScreenOn(keepScreenOn);
				}
			}
		);
	}
}
