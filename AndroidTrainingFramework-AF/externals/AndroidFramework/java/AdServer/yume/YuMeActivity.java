package APP_PACKAGE;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

@SuppressWarnings("deprecation")
public class YuMeActivity extends Activity 
{
	/* YuMeSDKInterface handle  */
	YuMeInterface yumeSDKInterface;
	
	/* relative Layout that contains the frame layout and video view */
	private RelativeLayout rLayout = null;

	/* frame layout that holds the video view */
	FrameLayout fLayout = null;
	
	/* video view that displays video */
	VideoView vView = null;
	
	/* Delay timer */
	private Timer delayTimer;
	
	/* combined height of status bar and title bar */
	private int statusBarAndTitleBarHeight = 0;
	
    /* Display Screen Width */
    public static int DISPLAY_SCREEN_WIDTH = 0;
    
    /* Display Screen Height */
    public static int DISPLAY_SCREEN_HEIGHT = 0;
    
    /* flag to indicate if the device under test is an android tablet or not */
    public boolean bIsDeviceATablet = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	/* set the full screen mode */
    	setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
 
    	super.onCreate(savedInstanceState);
    	
        /* check if the device is a tablet or not */
    	checkIfDeviceIsATablet();
    	
    	/* create the YuMeSDKInterface object */
    	yumeSDKInterface = YuMeInterface.getYuMeSDKInterface();
    	
        /* Create the screen Layout that holds the Frame Layout and VideoView */
        rLayout = new RelativeLayout(this);
        if(rLayout != null) {
	        ViewGroup.LayoutParams rLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
	        if(rLayoutParams != null)
	        	rLayout.setLayoutParams(rLayoutParams);
        }
        
        /* Create the fLayout */
        fLayout = new FrameLayout(this);
        if(fLayout != null) {
	        ViewGroup.LayoutParams fLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
	        if(fLayoutParams != null)
	        	fLayout.setLayoutParams(fLayoutParams);
        }
        
        /* request for title bar icon */
    	requestWindowFeature(Window.FEATURE_LEFT_ICON);
    	setContentView(rLayout);
    	
    	/* create the display view */
    	createDisplayView();
    
    	/* set the adview handle in YuMeSDKInterface */
   		yumeSDKInterface.setAdView(this);
   		
   		/* this delay timer is started in order to make sure that status bar and title bar
   		height is calculated correctly */
   		startDelayTimer();
    };
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		/* handle the orientation change event */
		handleOrientationChange(newConfig.orientation);
	}
    
    @Override
	protected void onStop() { 
		super.onStop();
		/* This will get called when Back Key / Home key gets pressed */
    	/* call the Back key Pressed API in SDK so that it can do the
    	necessary cleanup, if playing is in progress */
		yumeSDKInterface.backKeyPressed();
	}
	
	/**
	 * Gets the Application context.
	 * @param None. 
	 * @return the Application context.
	 */	
    public Context getAppContext() {
    	return this.getApplicationContext();
    }	
	
    /**
	 * Gets the Activity context.
	 * @param None. 
	 * @return this - the Activity context.
	 */	
	public Context getActivityContext() {
    	return this;
	}
    
	/**
	 * Creates the Display View.
	 * @param None.
	 * @return None.
	 */		
	private void createDisplayView() {
		/* remove existing views from layout, if any */
		removeViewsFromLayout();
		
		/* create the video view */
    	if(vView == null)
    		vView = new VideoView(this);
    	
		/* add the views to layout */
		addViewsToLayout();		
	}
	
	/**
	 * Adds views to layout.
	 * @param None. 
	 * @return None.
	 */
    private void addViewsToLayout() {
    	/* first add the vView to fLayout */
   		if(fLayout != null)
			fLayout.addView(vView);

    	/* add the fLayout to rLayout */
   		if(rLayout != null)
   			rLayout.addView(fLayout);
    }    	
	
	/**
	 * Removes the views from layout.
	 * @param None. 
	 * @return None.
	 */
    private void removeViewsFromLayout() {
    	/* first remove the vView from fLayout, if added already */
    	if(fLayout != null)
 			fLayout.removeView(vView);

    	/* then remove the fLayout from rLayout if added already */
    	if(rLayout != null)
    		rLayout.removeView(fLayout);
    }	

    /**
	 * Handles the orientation change event.
	 * @param newOrientation - the current orientation. 
	 * @return None.
	 */	 	
	private void handleOrientationChange(int newOrientation) {
		switch(newOrientation) {
			case Configuration.ORIENTATION_UNDEFINED:
				DBG("ADSERVER", "New Orientation: UNDEFINED");
				break;
			case Configuration.ORIENTATION_PORTRAIT:
				DBG("ADSERVER", "New Orientation: PORTRAIT");
				resizeAdLayout();
				break;
			case Configuration.ORIENTATION_LANDSCAPE:
				DBG("ADSERVER", "New Orientation: LANDSCAPE");
				resizeAdLayout();
				break;
			case Configuration.ORIENTATION_SQUARE:
				DBG("ADSERVER", "New Orientation: SQUARE");
				break;
		}
	}
    
	/**
	 * Gets the combined height of status bar and title bar.
	 * @param None. 
	 * @return the combined height of status bar and title bar.
	 */		
	public int getStatusBarAndTitleBarHeight() {
		return statusBarAndTitleBarHeight;
	}
    
	/**
	 * Calculates the combined height of status bar and title bar.
	 * @param None. 
	 * @return None.
	 */		
	public void calculateStatusBarAndTitleBarHeight() {
        Rect rect = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        
        int statusBarHeight = rect.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        //DBG("ADSERVER", "contentViewTop: " + contentViewTop);
        int titleBarHeight = 0;
        if(contentViewTop > 0)
        	titleBarHeight = contentViewTop - statusBarHeight;
        
        /* work-around to fix the issue wherein statusBarHeight is always >0 even, if not present */ 
        if(contentViewTop == 0)
        	statusBarHeight = 0;        

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        /* check if status bar resides at the bottom of the device screen like in Motorola Xoom */
        if(rect.bottom < display.getHeight()) {
        	statusBarHeight = display.getHeight() - rect.bottom;
        	statusBarAndTitleBarHeight = titleBarHeight;
        } else {
        	statusBarAndTitleBarHeight = statusBarHeight + titleBarHeight;
        }
    	INFO("ADSERVER", "Status Bar Height: " + statusBarHeight + ", Title Bar Height: " + titleBarHeight);        
        INFO("ADSERVER", "Status Bar & Title Bar Height: " + statusBarAndTitleBarHeight);
	}
	
	/**
	 * Creates and starts the Delay timer.
	 * @param None. 
	 * @return None.
	 */		
	private void startDelayTimer() {
		if(delayTimer == null) {
			/* create and start the Delay timer */
			int timeVal = 50; /* ms */
			delayTimer = new Timer();
			delayTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					onDelayTimerExpired();
				}
			}, timeVal);
		}
	}
	
	/**
	 * Stops the Delay timer.
	 * @param None. 
	 * @return None.
	 */		
	void stopDelayTimer() {
		if(delayTimer != null) {
			delayTimer.cancel();
			delayTimer = null;
		}
	}
	
	/**
	 * Listener for timer expiry event from Delay timer.
	 * @param None. 
	 * @return None.
	 */		
	void onDelayTimerExpired() {
		/* stop the Delay timer */
		stopDelayTimer();
		
		/* perform the ad display on UI thread */
		runOnUiThread(displayAdOnUIThread);
	}
	
	/**
	 * Displays the ad on UI thread.
	 * @param None.
	 * @return None.
	 */
	private Runnable displayAdOnUIThread = new Runnable() {
		public void run() {
			try {
				/* get the status bar and title bar height */
				calculateStatusBarAndTitleBarHeight();
				
				/* resize the ad layout */
				resizeAdLayout();
				
				/* set the parent view for ad display in YuMe SDK */
		   		yumeSDKInterface.setParentView(fLayout, vView, null);
		   		
		   		/* display the prefetched ad */
				boolean retVal = yumeSDKInterface.displayAd();
				if(!retVal)
					finish();
			} catch (Exception e) {
				ERR("ADSERVER", "Exception Displaying Ad.");
				DBG_EXCEPTION(e);
			}				
		}
	};
	
	/**
	 * Gets the display resolution of the device and identifies if it is an Android PHONE or TABLET.
	 * @param None. 
	 * @return None.
	 */		
	private void getDeviceDisplayResolution() {
        /* get the display resolution of the device */
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    	DISPLAY_SCREEN_WIDTH = display.getWidth();
    	DISPLAY_SCREEN_HEIGHT = display.getHeight();
	    int currRotation = display.getRotation();
		if ( (currRotation == Surface.ROTATION_0) || 
			 (currRotation == Surface.ROTATION_180) ) {
			INFO("ADSERVER", "Current Display Orientation: PORTRAIT.");
		} else if ( (currRotation == Surface.ROTATION_90) || 
			        (currRotation == Surface.ROTATION_270) ) {			
			INFO("ADSERVER", "Current Display Orientation: LANDSCAPE.");
	    	DISPLAY_SCREEN_WIDTH = display.getHeight();
	    	DISPLAY_SCREEN_HEIGHT = display.getWidth();
		}
        INFO("ADSERVER", "Device Resolution: Width: " + DISPLAY_SCREEN_WIDTH + ", Height: " + DISPLAY_SCREEN_HEIGHT);
	}
	
	/**
	 * Checks if the current device is an Android Tablet or not.
	 * @param None. 
	 * @return None.
	 */	    
	private void checkIfDeviceIsATablet() {
    	/* get the device display resolution */
    	getDeviceDisplayResolution();
    	
		bIsDeviceATablet = false;
		int scrLayout = getResources().getConfiguration().screenLayout;
		//DBG("ADSERVER", "Screen Layout: " + scrLayout + ", Val: " + (scrLayout & Configuration.SCREENLAYOUT_SIZE_MASK));
		if( (scrLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
	    	bIsDeviceATablet = true;
	    	INFO("ADSERVER", "Device is identified as an Android TABLET.");
		} else {
			INFO("ADSERVER", "Device is identified as an Android PHONE.");
		}
	}
	
	/**
	 * Resizes the ad layout.
	 * @param None.
	 * @return None.
	 */	
	void resizeAdLayout() {
        /* get the device display resolution */
        Display display = getWindowManager().getDefaultDisplay();
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();
        
        /* set / modify the rLayout padding to make the frame layout positioned properly, in case of non-full screen parentview for ad display */
        if(rLayout != null)
        	rLayout.setPadding(0, 0, 0, 0);

        /* set / modify the Frame Layout params */
        if(fLayout != null) {
	        ViewGroup.LayoutParams fLayoutParams1 = fLayout.getLayoutParams();
	        if(fLayoutParams1 == null) {
	            fLayoutParams1 = new FrameLayout.LayoutParams(displayWidth, displayHeight - statusBarAndTitleBarHeight);
	        } else {
	            fLayoutParams1.width = displayWidth;
	            fLayoutParams1.height = displayHeight - statusBarAndTitleBarHeight;
	        }
	        fLayout.setLayoutParams(fLayoutParams1);
	        DBG("ADSERVER", "Resizing FLayout: Width: " + fLayout.getLayoutParams().width + ", Height: " + fLayout.getLayoutParams().height);
        }

        /* set / modify the VideoView layout params */
        if(vView != null) {
        	 ViewGroup.LayoutParams layoutParams = vView.getLayoutParams();
	        if(layoutParams == null) {
	            layoutParams = new FrameLayout.LayoutParams(displayWidth, displayHeight - statusBarAndTitleBarHeight);
	        } else {
	            layoutParams.width = displayWidth;
	            layoutParams.height = displayHeight - statusBarAndTitleBarHeight;
	        }
	       	vView.setLayoutParams(layoutParams);
	        DBG("ADSERVER", "Resizing VideoView: Width: " + vView.getLayoutParams().width + ", Height: " + vView.getLayoutParams().height);
        }
    }	
}
