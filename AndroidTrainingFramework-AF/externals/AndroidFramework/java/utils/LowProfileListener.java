package APP_PACKAGE.GLUtils;

import android.os.Build;
import android.app.Activity;
import java.lang.Runnable;
import android.os.Handler;

public class LowProfileListener
{
	SET_TAG("LowProfileListener");

	private static android.view.View.OnSystemUiVisibilityChangeListener UIListener = null;
	private static String activityName = "";
	
	public static void onKeyDown (final Activity thiz, int keyCode) 
	{
		if (Build.VERSION.SDK_INT >= 14) //hasPermanentMenuKey was included on ICE_CREAM_SANDWICH
		{
			if(android.view.ViewConfiguration.get(thiz.getApplicationContext()).hasPermanentMenuKey())
				return;//avoid calling Immersive/LowProfile if the device doesn't have Software menu
				
			if(keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN
				|| keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP
				)
			{
				ActivateImmersiveMode(thiz);
			}
		}
	}
	
	private static void MakeImmersive(final Activity thiz)
	{
		#if TARGET_API_LEVEL_INT >= 19 //android 4.4
			thiz.getWindow().getDecorView().setSystemUiVisibility(
				android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
				android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
				android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
				android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
				android.view.View.SYSTEM_UI_FLAG_FULLSCREEN	|
				android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				);
		#endif
	}
	
	public static void ActivateImmersiveMode (final Activity thiz) 
	{
		if (Build.VERSION.SDK_INT >= 14)
		{
			if(android.view.ViewConfiguration.get(thiz.getApplicationContext()).hasPermanentMenuKey())
				return;//dont register listener if there's no software menu

			#if (USE_LOW_PROFILE_MENU == 0)
			if( Build.VERSION.SDK_INT < 19 )
			#endif
			{
				thiz.getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_VISIBLE);
				thiz.getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LOW_PROFILE);
				RegisterSystemUIListener(thiz);
			}
			#if (USE_LOW_PROFILE_MENU == 0)
			else //if (Build.VERSION.SDK_INT >= 19)//android 4.4
			{				
				MakeImmersive(thiz);
				
				Runnable rehideRunnable = new Runnable()
				{
					public void run() 
					{
						MakeImmersive(thiz);
					}
				};
				Handler rehideHandler = new Handler();
				rehideHandler.postDelayed(rehideRunnable, 3000);
			}
			#endif
		}
	}
	
	private static void RegisterSystemUIListener(final Activity activ)
	{
		//add listener for in every activity
		if(UIListener == null || !activityName.equals(activ.getClass().getSimpleName()))
		{
			activityName = activ.getClass().getSimpleName();
			DBG(TAG,"Register Low Profile Listener to " + activityName);
			UIListener = new android.view.View.OnSystemUiVisibilityChangeListener() 
			{
				@Override
				public void onSystemUiVisibilityChange(int visibility) 
				{
					if (visibility == 0)
					{
						DBG(TAG,"Visibility = 0, will set LowProfile");
						Runnable rehideRunnable = new Runnable()
						{
							public void run() 
							{
								activ.getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_VISIBLE);
								activ.getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LOW_PROFILE);
							}
						};
						Handler rehideHandler = new Handler();
						rehideHandler.postDelayed(rehideRunnable, 3000);
					}
				}
			};
		}
		activ.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(UIListener);	
	}
}
