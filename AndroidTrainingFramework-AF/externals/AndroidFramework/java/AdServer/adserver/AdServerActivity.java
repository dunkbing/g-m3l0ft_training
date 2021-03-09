package APP_PACKAGE;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
//import android.view.WindowManager;

/**
 * 	This is a sample activity which exemplifies the use of the AdServer class.
 */
public class AdServerActivity extends Activity 
{
	public 	RelativeLayout	mView;
	public 	AdServer		adServer = new AdServer(this, AdServer.BOTTOM_CENTER, AdServer.BANNER_SMALL);
		
	@Override
	protected void onStart()
	{
		super.onStart();
		adServer.onStart();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
	#endif
	}

	@Override
	protected void onStop() 
	{
		super.onStop();
		adServer.onStop();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
	}
	
	@Override
    public void onDestroy()
    {
		adServer.onDestroy();
	 	super.onDestroy();
    }
    
    @Override
    public void onResume()
    {
		adServer.onResume();
		super.onResume();
    }
    
    @Override
    public void onPause()
    {
		adServer.onPause();
		super.onPause();
    }
	
	@Override
	public void onBackPressed()
	{
		if (adServer.onBackPressed())
			return;
		else 
			super.onBackPressed();
	}
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);  
		
		//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        mView = new RelativeLayout(this); // Main View.
		mView.setBackgroundColor(Color.LTGRAY);
		
		// Setup the interface and language
		android.content.Intent sender = getIntent();
		if (sender.getExtras() != null)
			adServer.currentLanguage = sender.getExtras().getInt("language");
		else
			adServer.currentLanguage = 0;
		
		// Main buttons
		Button showAd = new Button(this);
		showAd.setText("Show Ad");
		showAd.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				adServer.ShowBanner(mView);				
			}
		});		
		
		Button hideAd = new Button(this);
		hideAd.setText("Hide Ad");
		hideAd.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { 
				adServer.HideBanner(mView);
			}
		});
		
		final Button freeCash = new Button(this);
		freeCash.setText("Free Cash");
		freeCash.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) 
			{	
				adServer.OpenFreeCash();
			}
		});
		freeCash.setVisibility(View.GONE);
		
		Button showInterstitial = new Button(this);
		showInterstitial.setText("Interstitial");
		showInterstitial.setOnClickListener(new OnClickListener() 
		{			
			@Override
			public void onClick(View v) {
				adServer.ShowInterstitial();
			}
		});
		
		RelativeLayout.LayoutParams lpCenter = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lpCenter.addRule(RelativeLayout.CENTER_IN_PARENT);
		
		LinearLayout mCenter = new LinearLayout(this);
		
		mCenter.addView(showAd);
		mCenter.addView(hideAd);
		mCenter.addView(showInterstitial);
		mCenter.addView(freeCash);
		
		mView.addView(mCenter, lpCenter);
		
		setContentView(mView); //
		adServer.InitAds(mView);  
		adServer.GetFreeCash();

		//Toast.makeText(AdServerActivity.this, "Request sent. Waiting for reply...", Toast.LENGTH_SHORT).show();						
		new Thread(new Runnable() { public void run() 
		{	
			long callstarttime = System.currentTimeMillis();
			while (adServer.showFreeCash != true)
			{
				if (System.currentTimeMillis() - callstarttime > 10000)
					break;
			}
			
			runOnUiThread(new Runnable() { public void run()
			{
				if (adServer.showFreeCash)
				{
					Toast.makeText(AdServerActivity.this, "Free cash available.", Toast.LENGTH_SHORT).show();	
					freeCash.setVisibility(View.VISIBLE);
				}
				else 	
				{
					Toast.makeText(AdServerActivity.this, "No free cash available.", Toast.LENGTH_SHORT).show();	
					freeCash.setVisibility(View.GONE);
				}
			}});
		}}).start();
    }
}