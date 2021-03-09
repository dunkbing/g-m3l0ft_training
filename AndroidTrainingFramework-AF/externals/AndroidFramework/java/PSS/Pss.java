#if USE_PSS
/**
 * PSS - Promotional Splash Screen
 * Version 1.0.1
 */

package APP_PACKAGE.pss;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;

import APP_PACKAGE.R;

import android.os.Build;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import java.util.HashMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.SharedPreferences;
import android.os.Looper;
import java.net.URLDecoder;
import android.telephony.TelephonyManager;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;

#if ADS_USE_FLURRY
import APP_PACKAGE.Flurry.GLFlurry;
#endif

public class Pss extends Activity implements Runnable {
	WebView mWebView;
	//Tmobile and Bell direct billing dialog
	AlertDialog.Builder abConfirmBuy;
	AlertDialog alertConfirmBuy;
	AlertDialog.Builder redownload_wifi_dialog;
	
	boolean confirmedBuy = false;
	
	CheckBox checkBoxIsEnabled;
	
	//XPlayer http connection
	Device mDevice;
	XPlayer mXPlayer;

	final int LAUCHES_TO_CONNECT_AGAIN = 5;//launch game 5 times to clear cache and connect online again
	public static boolean gameLaunched = false;
	boolean bIsPaused = true;
	boolean pssStarted = false;
	
	//shared store
	public static final String PREFERENCES_NAME = "PSS";
	//keys
	public static final String PREFERENCE_KEY_PSS_LAUNCHES = "PREFERENCE_KEY_PSS_LAUNCHES";
	public static final String PREFERENCE_KEY_PAGE_CACHED = "PREFERENCE_KEY_PAGE_CACHED";
	public static final String PREFERENCE_KEY_IS_ENABLED = "PREFERENCE_KEY_IS_ENABLED";
	public static final String PREFERENCE_KEY_GAME_LAUNCHES = "PREFERENCE_KEY_GAME_LAUNCHES";//Counter, increase every game launch with or without PSS
	
#if PSS_VZW
	public static final int LAUNCH_COUNT_TO_SHOW_PSS = 2;
#else
	public static final int LAUNCH_COUNT_TO_SHOW_PSS = 4;
#endif
	public static final int GAME_LAUNCHES_TO_ENABLE_PSS = 7;//when PSS is disabled, after 7 launches it will be displayed again
	public static String START_UP_URL;

	int launch_counter = 0;
	
	public final String ERROR_PAGE="file:///android_asset/pss_offline_error.html";
	
	//PSS States
	int pState = 0;
	public static final int STATE_INIT = 0;
	public static final int STATE_SEND_REQUEST = 1;
	public static final int STATE_WAIT_RESPONSE = 2;
	public static final int STATE_SHOW_PSS = 3;
	public static final int STATE_FINALIZE = 4;

	//get-set
	public int getLaunch_counter() {
		return launch_counter;
	}

	public void setLaunch_counter(int launchCounter) {
		launch_counter = launchCounter;
	}

	String cachedUrlStored;

	public String getCachedUrlStored() {
		return cachedUrlStored;
	}

	public void setCachedUrlStored(String cachedUrlStored) {
		this.cachedUrlStored = cachedUrlStored;
	}
	public static void setGameLaunched(boolean isLaunched){
		gameLaunched = isLaunched;
	}
	
	public static boolean canLaunchPSS()
	{
		int launch_counter = SUtils.getPreferenceInt(Pss.PREFERENCE_KEY_GAME_LAUNCHES, 0, Pss.PREFERENCES_NAME);
		boolean isPssEnabled = SUtils.getPreferenceBoolean(Pss.PREFERENCE_KEY_IS_ENABLED, true, Pss.PREFERENCES_NAME);
		DBG("PSS", "isPssEnabled="+isPssEnabled+"\tlaunch_counter="+launch_counter);
		if (isPssEnabled){
			if (launch_counter < LAUNCH_COUNT_TO_SHOW_PSS)
				return false;
		}else{
			if (launch_counter < GAME_LAUNCHES_TO_ENABLE_PSS)
				return false;
		}
		return true;
	}

	//manage all buttons
	private ArrayList<Button>       buttonList;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Turn off the title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		SUtils.setContext(this);
        
        mWebView = new WebView(getApplicationContext());
	handledURL = null;
        //setContentView(R.layout.pss_main_layout);
        
        //checkBoxIsEnabled = (CheckBox) findViewById(R.id.checkBox1);
		//if (PssConfig.USE_DISABLE_OPTION)
		//	checkBoxIsEnabled.setChecked( SUtils.getPreferenceBoolean(Pss.PREFERENCE_KEY_IS_ENABLED, true, PREFERENCES_NAME) );

        //restore persistent values
        cachedUrlStored = SUtils.getPreferenceString(PREFERENCE_KEY_PAGE_CACHED, PREFERENCES_NAME);
        DBG("PSS", "[onCreate]\tCached page is "+cachedUrlStored);
        
        launch_counter = SUtils.getPreferenceInt(PREFERENCE_KEY_PSS_LAUNCHES, 0, PREFERENCES_NAME);
        if (cachedUrlStored!=null && cachedUrlStored.length() > 0 && launch_counter < LAUCHES_TO_CONNECT_AGAIN-1){
        	SUtils.setPreference(PREFERENCE_KEY_PSS_LAUNCHES, ++launch_counter, PREFERENCES_NAME);
        	DBG("PSS", "[onCreate]\tCache was stored, you are browsing offline "+launch_counter+" of " + LAUCHES_TO_CONNECT_AGAIN +" times");
        }else if (cachedUrlStored!=null && cachedUrlStored.length() > 0 && isNetworkAvailable()){
        	//clear cache
        	//its time to connect again
        	DBG("PSS", "[onCreate]\tCache was set and network available, connectig online");
		clearCache();
        }else{
        	DBG("PSS", "[onCreate]\tNo cache or no network found");
        }
		
		//Direct purchase dialog
		abConfirmBuy = new AlertDialog.Builder(Pss.this);
		abConfirmBuy.setTitle(getString(R.string.pss_BuyConfirmTitle));
		abConfirmBuy.setMessage(getString(R.string.pss_BuyConfirmText))
		.setCancelable(false)
		.setPositiveButton(getString(R.string.pss_BuyConfirmOK), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//extra header
				HashMap<String, String> newURL = parsePurchaseURL(handledURL);
				mWebView.loadUrl(newURL.get("query"), map);
				handledURL = null;
				clearCache();
			}
		})
		.setNegativeButton(getString(R.string.pss_BuyConfirmNO), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				handledURL = null;
			}
		});
    }
    
	public static void increaseGameLaunch()
	{
		int launch_counter = SUtils.getPreferenceInt(PREFERENCE_KEY_GAME_LAUNCHES, 0, PREFERENCES_NAME);
		if (launch_counter <= GAME_LAUNCHES_TO_ENABLE_PSS)
		{
			launch_counter ++;
			SUtils.setPreference(Pss.PREFERENCE_KEY_GAME_LAUNCHES, launch_counter, Pss.PREFERENCES_NAME);
		}
	}
    
	@Override
	protected void onStart(){
		super.onStart();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
	#endif
	#if ADS_USE_FLURRY
		GLFlurry.onStartSession(this);
	#endif
		DBG("PSS", "[onStart]");
		if (!pssStarted)
		{
			new Thread(this).start();
			pssStarted = true;
		}
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
	#if ADS_USE_FLURRY
		GLFlurry.onEndSession(this);
	#endif
	}
	
	
	public void onDestroy(){
    	super.onDestroy();
		DBG("PSS", "[onDestroy]");
		if (!gameLaunched)
			Exit();
    }
	
	public void onPause(){
		super.onPause();
		bIsPaused = true;
		DBG("PSS", "[onPause]");
	}
	
	public void onResume(){
		super.onResume();
		bIsPaused = false;
		DBG("PSS", "[onResume]");
	}
    
	public void setButtonList(ArrayList<Button> buttonList)
    {
        this.buttonList = buttonList;
    }
    
    public ArrayList<Button> getButtonList()
    {
        return buttonList;
    }
    public void gotoLayout(final int layoutId)
	{
		this.runOnUiThread(new Runnable ()
		{
			public void run ()
			{
				try{
					setContentView(layoutId);
					switch(layoutId)
					{
						case R.layout.pss_main_layout:
							//	Game title
							String GAME_TITLE = getString(R.string.pss_title).replace("{GAME_TITLE}", getString(R.string.app_name));
							TextView text_title = (TextView) findViewById(R.id.TextView01);
							if (text_title != null)
								text_title.setText(GAME_TITLE);
							
							checkBoxIsEnabled = (CheckBox) findViewById(R.id.checkBox1);
							if (PssConfig.USE_DISABLE_OPTION){
								checkBoxIsEnabled.setChecked( SUtils.getPreferenceBoolean(Pss.PREFERENCE_KEY_IS_ENABLED, true, PREFERENCES_NAME) );
								checkBoxIsEnabled.setOnCheckedChangeListener(chkOnCheckListener);
							}
							
							setDeviceTextView();
							addWebViewToLayout();
						break;
					}
				} catch(Exception e){
					DBG_EXCEPTION(e);
					//go to finalize
				}
			}
		});
	}
	//extra headers
	public String gl_subno;
	public String imei;
	#if HDIDFV_UPDATE
	public String hdidfv;
	#endif
	HashMap<String,String> map = new HashMap<String,String>();
    
	public void addWebViewToLayout(){
    	//clear buttons for changes
    	setButtonList(new ArrayList<Button>());
        getButtonList().clear();
    	
    	mWebView.getSettings().setJavaScriptEnabled(true);
    	mWebView.setVerticalFadingEdgeEnabled(true);
		map.put("x-up-gl-subno",gl_subno);
		map.put("x-up-gl-imei",imei);
		#if HDIDFV_UPDATE
		map.put("x-up-gl-hdidfv",hdidfv);
		#endif

    	if ((cachedUrlStored == null || cachedUrlStored.length() == 0) && isNetworkAvailable()){
    		//create cache
    		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    		mWebView.getSettings().setAppCacheEnabled(false);
    		mWebView.loadUrl(START_UP_URL, map);
    		DBG("PSS", "[addWebViewToLayout]\tWebView connecting LOAD_NO_CACHE");
    	}else if (cachedUrlStored != null && cachedUrlStored.length() > 0){
    		//browsing cache
    		mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
    		mWebView.getSettings().setAppCacheEnabled(true);
    		mWebView.loadUrl(cachedUrlStored, map);
    		DBG("PSS", "[addWebViewToLayout]\tWebView offline LOAD_CACHE_ONLY");
    	}else{
    		//no cache and no network
    		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    		mWebView.getSettings().setAppCacheEnabled(false);
    		mWebView.loadUrl(ERROR_PAGE);
    		DBG("PSS", "[addWebViewToLayout]\tWebView no cache or network found");
    	}
    	
    	//WebView container
		RelativeLayout wbLayout = (RelativeLayout) findViewById(R.id.webview_layout);
    	if (PssConfig.USE_DISABLE_OPTION){
    		int checkBoxHeight = 50;//default size 
    		//checkBoxHeight = ((LinearLayout) findViewById(R.id.linearLayout1)).getLayoutParams().height;
    		wbLayout.setPadding(wbLayout.getPaddingLeft(), wbLayout.getPaddingTop(), wbLayout.getPaddingRight(), checkBoxHeight);
        }else{
        	wbLayout.setPadding(wbLayout.getPaddingLeft(), wbLayout.getPaddingTop(), wbLayout.getPaddingRight(), wbLayout.getPaddingBottom());
			checkBoxIsEnabled.setVisibility(View.GONE);
        }
    	
    	mWebView.setWebViewClient(new PSSWebViewClient());
    	wbLayout.addView(mWebView);
    	//add buttons
    	getButtonList().add((Button) findViewById(R.id.bt_play_now));
    	//add listener
    	for (Button but : getButtonList())
        {
            but.setOnClickListener(btnOnClickListener);
        }
    }
    
    public void setDeviceTextView(){
    	String deviceModel = android.os.Build.MODEL;
		String deviceManufacturer = android.os.Build.MANUFACTURER;
		//setup textView
		TextView MoreGames = (TextView) findViewById(R.id.TextView03);
		MoreGames.setText(getString(R.string.pss_MoreGamesText)+ "\n" + deviceManufacturer.toUpperCase() + " " + deviceModel);
    }
    
    public void setPurchaseDialogText(String gameName, String price){
    	String title = getString(R.string.pss_BuyConfirmTitle).replace("{GAME_NAME}", gameName);
    	String itemPrice = getString(R.string.pss_BuyConfirmText).replace("{PRICE}", price);
    	
    	abConfirmBuy.setTitle(title);
    	abConfirmBuy.setMessage(itemPrice);
    }
	
    public void removeWebViewFromLayout(){
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.webview_layout);
		rl.removeView(mWebView);
		
		mWebView.destroy();
		mWebView = null;
		System.gc();
    }
	
	#if PSS_VZW
	CompoundButton.OnCheckedChangeListener checkBoxListener = new CompoundButton.OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (mVzwBilling == null)
					mVzwBilling = new VzwBilling(Pss.this);
				mVzwBilling.ChangeConnection(isChecked);
			}
	};
	
	public void createDownloadConfirmDialog() {
		LayoutInflater inflater = (LayoutInflater) Pss.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.pss_start_download_dialog, 
						(ViewGroup) findViewById(R.id.DownloadOptionsLinearLayout));
						
		CheckBox wifiCheckBox = (CheckBox) layout.findViewById(R.id.checkBox1);
		wifiCheckBox.setOnCheckedChangeListener(checkBoxListener);
						
		redownload_wifi_dialog = new AlertDialog.Builder(Pss.this);
		redownload_wifi_dialog.setView(layout);
		redownload_wifi_dialog.setCancelable(false)
				.setPositiveButton(getString(R.string.pss_vzw_downlaod), new DialogInterface.OnClickListener() {
				   public void onClick(DialogInterface dialog, int id) {
						//this event will be override with View.OnClickListener to keep both dialogs on screen.
				   }
			   });
		
		final AlertDialog alert_wifi = redownload_wifi_dialog.create();
		alert_wifi.show();

		Button okButton = alert_wifi.getButton(DialogInterface.BUTTON_POSITIVE);
		okButton.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v) {
						if (canInstallFromOtherSources(Pss.this))
						{
							alert_wifi.dismiss();
							mWebView.loadUrl(handledURL, map);
							handledURL = null;
						} else {
							AlertDialog customD = createUnknownSourcesDialog(Pss.this, "");
							customD.show();
						}
						
					}
				});
	}
	
	public static AlertDialog createUnknownSourcesDialog(final Activity mActivity, String gameName){
		LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mlayout = inflater.inflate(R.layout.pss_unknown_sources_dialog, 
						(ViewGroup) mActivity.findViewById(R.id.BaseLayout));
						
		String message = mActivity.getString(R.string.pss_vzw_unknown_sources_message).replace("{GAME}", gameName);
		TextView txt_title = (TextView) mlayout.findViewById(R.id.titleTextView);
		txt_title.setText(message);
		
		
		AlertDialog.Builder mInstallUnknownDialog = new AlertDialog.Builder(mActivity);
		mInstallUnknownDialog.setView(mlayout);
		mInstallUnknownDialog.setCancelable(false);
		//mInstallUnknownDialog.setPositiveButton(mActivity.getString(R.string.pss_vzw_settings), onClickListener);
		mInstallUnknownDialog.setPositiveButton(mActivity.getString(R.string.pss_vzw_settings), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				try{
					if (Build.VERSION.SDK_INT > 13)//ICE_CREAM_SANDWICH
						mActivity.startActivityForResult(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS) ,0);//
					else
						mActivity.startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS) ,0);
				}catch(Exception e){
					mActivity.startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS) ,0 );
				}
				handledURL = null;
				dialog.cancel();
			}
		});
		
		AlertDialog alert = mInstallUnknownDialog.create();
		return alert;
	}
	
	public static boolean canInstallFromOtherSources(Activity mActivity)
	{
		boolean canInstall;
		try {
		   canInstall = Settings.Secure.getInt(mActivity.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1;
		} catch (SettingNotFoundException e) {
		// TODO Auto-generated catch block
		   e.printStackTrace();
		   canInstall = true;
		}
		return canInstall;
	}
	#endif	//PSS_VZW
	
	public void clearCache()
	{
        	clearCacheFolder(getCacheDir(), 0);
        	launch_counter = 0;
        	SUtils.setPreference(PREFERENCE_KEY_PSS_LAUNCHES, launch_counter, PREFERENCES_NAME);
        	cachedUrlStored = "";
        	SUtils.setPreference(PREFERENCE_KEY_PAGE_CACHED, cachedUrlStored, PREFERENCES_NAME);
	}
    
    static int clearCacheFolder(final File dir, final int numDays) {

        int deletedFiles = 0;
        if (dir!= null && dir.isDirectory()) {
            try {
                for (File child:dir.listFiles()) {

                    //first delete subdirectories recursively
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }

                    //then delete the files and subdirectories in this dir
                    //only empty directories can be deleted, so subdirs have been done first
                    if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            }
            catch(Exception e) {
                //ERR(TAG, String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }
        return deletedFiles;
    }
    
    private long dirSize(File dir) {
        long result = 0;

        Stack<File> dirlist= new Stack<File>();
        dirlist.clear();

        dirlist.push(dir);

        while(!dirlist.isEmpty())
        {
            File dirCurrent = dirlist.pop();

            File[] fileList = dirCurrent.listFiles();
            for (int i = 0; i < fileList.length; i++) {

                if(fileList[i].isDirectory())
                    dirlist.push(fileList[i]);
                else
                    result += fileList[i].length();
            }
        }
        DBG("PSS", "[dirSize] cache dir ="+(result/1024));
        return result;
    }
    public boolean isCacheValid(){
    	long cache_size = dirSize(getCacheDir());
    	if (cache_size<=0){	
    		DBG("PSS", "[isCacheValid] false: cache_size <= 0");
    		return false;
    	}
    	int Kb = (int) (cache_size/1024);
    	if (Kb >= 100){
    		DBG("PSS", "[isCacheValid] = true");
    		return true;
    	}
    	DBG("PSS", "[isCacheValid] = false");
    	return false;
    	
    }

    public void changeWebViewURL(String url, int cache_mode)
	{
		
    	if (cache_mode == WebSettings.LOAD_NO_CACHE){
    		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    		mWebView.getSettings().setAppCacheEnabled(false);
    		mWebView.loadUrl(url, map);
    	}else if (cache_mode == WebSettings.LOAD_CACHE_ONLY){
    		mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
    		mWebView.getSettings().setAppCacheEnabled(true);
    		mWebView.loadUrl(url, map);
    	}
    }

#if PSS_VZW
	VzwBilling mVzwBilling = new VzwBilling(this);
	protected void RequestGame(final String url)
	{
		INFO("PSS", "[RequestGame] ");
		clearCache();		
		mVzwBilling.RequestBuy(url);
	}
#endif

    public OnCheckedChangeListener chkOnCheckListener = new OnCheckedChangeListener(){

		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (bIsPaused){
				return;
			}
			DBG("PSS", "[onCheckedChanged]");
			
			if (!isChecked){
				SUtils.setPreference(PREFERENCE_KEY_IS_ENABLED, false, PREFERENCES_NAME);
				SUtils.setPreference(PREFERENCE_KEY_GAME_LAUNCHES, 0, PREFERENCES_NAME);
			}else{
				//make pss visible for the next time
				SUtils.setPreference(PREFERENCE_KEY_GAME_LAUNCHES, GAME_LAUNCHES_TO_ENABLE_PSS, PREFERENCES_NAME);
				SUtils.setPreference(PREFERENCE_KEY_IS_ENABLED, true, PREFERENCES_NAME);
			}
		}
    };
	public DialogInterface.OnCancelListener pdOnCancelListener = new DialogInterface.OnCancelListener(){
		@Override
		public void onCancel(DialogInterface dialog) {
			DBG("PSS", "User stopped WebView loading");
			mWebView.stopLoading();
			//clear cache
			clearCache();
			//load webview again with error
			mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    		mWebView.getSettings().setAppCacheEnabled(false);
    		mWebView.loadUrl(ERROR_PAGE);
			
		}

	};
    
    public OnClickListener btnOnClickListener = new OnClickListener(){

		public void onClick(View v) {
			// TODO Auto-generated method stub
			int buttonId;
			try{
				Button buttonPressed = (Button) v;
                buttonId = buttonPressed.getId();
			}catch(Exception e){
				buttonId = -1;
			}
			switch(buttonId){
			case R.id.bt_play_now:
				removeWebViewFromLayout();
				launchGame();
				break;
			}
		}
    	
    };
	
	public void run(){
		Looper.prepare();
		pState = STATE_INIT;
		while (pState != STATE_SHOW_PSS)
		{
			// Reduce the CPU usage while in the background
			if(bIsPaused)
			{
				try{Thread.sleep(50);}catch(Exception e){}
				continue;
			}
			update();
		}
		if (pState == STATE_SHOW_PSS)
		{
			gotoLayout(R.layout.pss_main_layout);
		}
	}
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) 
		{
	        	finish();
	    	    return true;
	    }else if( keyCode == KeyEvent.KEYCODE_CAMERA 
		|| keyCode == KeyEvent.KEYCODE_VOLUME_DOWN 
		|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
		|| keyCode == KeyEvent.KEYCODE_SEARCH
		)
			return false;
			
		
	    return true;
	}
    
    public void launchGame(){
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClassName(PssConfig.LAUNCH_ACTIVITY_PACKAGE, PssConfig.LAUNCH_ACTIVITY_CLASS);
		
		startActivity(i);
		setGameLaunched(true);
		finish();
	}
    void Exit()
	{
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1)
		{
			int pid = android.os.Process.myPid();
			android.os.Process.killProcess(pid); 
		}
		else
		{
			System.exit(0);
		}
	}
    
    public boolean isNetworkAvailable(){
    	ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    	return activeNetworkInfo != null;
    }
	
	public void update(){
		switch(pState)
		{
			case STATE_INIT:
				DBG("PSS", "PSS_STATE_INIT");
				gotoLayout(R.layout.pss_connecting);
				
				mDevice = new Device();
				mXPlayer = new XPlayer(mDevice);
				pState = STATE_SEND_REQUEST;
			break;
			case STATE_SEND_REQUEST:
				DBG("PSS", "STATE_SEND_REQUEST");
				String key = "igpcode=" + Device.getDemoCode();
				String URL = "http://wapshop.gameloft.com/PSS/";
				mXPlayer.sendRequestPSS_games_available(URL,key);
				gl_subno = XPlayer.getDevice().getLineNumber();
				imei = XPlayer.getDevice().getIMEI();
				#if HDIDFV_UPDATE
				hdidfv = XPlayer.getDevice().getHDIDFV();
				#endif
				
				if (gl_subno == null || gl_subno.equals("00"))
					gl_subno = "0000000000";
					
				// trunk mobile number to 10 digits. avoid international roaming digits.
				if(gl_subno.length() > 10)
					gl_subno = gl_subno.substring(gl_subno.length() - 10, gl_subno.length());

				pState= STATE_WAIT_RESPONSE;
			break;
			case STATE_WAIT_RESPONSE:
				DBG("PSS", "STATE_WAIT_RESPONSE");
				DBG("PSS", "Waiting for response...");
				while (!mXPlayer.handleRequestPSS_games_available())
				{
					try {
						Thread.sleep(200);
					} catch (Exception exc) {}
				}
				
				//  Get SIM state to skip if is invalid
				TelephonyManager telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
				boolean isValidSim = (telMgr.getSimState()!= TelephonyManager.SIM_STATE_ABSENT)?true:false;
				try{
				  long line_number = Long.parseLong(gl_subno);
				  isValidSim = (line_number > 0)?true:false;
				}catch(NumberFormatException ne){
				  isValidSim = false;
				} 
        
				if (mXPlayer.str_response != null)
				{
					DBG("PSS", "Server response=" + mXPlayer.str_response);
					if (mXPlayer.str_response.length() > 0 && mXPlayer.str_response.startsWith("http:") && isValidSim)
					{	
						pState = STATE_SHOW_PSS;
						START_UP_URL = parseWebViewURL(mXPlayer.str_response);
						DBG("PSS","WebView URL="+START_UP_URL);
					}else{
						launchGame();
					}
					//reset game launched times to 0 when always show is not checked.
					boolean pss_always_show = SUtils.getPreferenceBoolean(Pss.PREFERENCE_KEY_IS_ENABLED, true, Pss.PREFERENCES_NAME);
					if (!pss_always_show)
					{
						SUtils.setPreference(Pss.PREFERENCE_KEY_GAME_LAUNCHES, 0, Pss.PREFERENCES_NAME);//restet counter
					}
				}else{
					//finalize and launch game
					launchGame();
				}
			break;
		}
	}
	public String parseWebViewURL(String serverResponse)
	{
		if (!serverResponse.startsWith("http"))
			serverResponse = "http://"+serverResponse;
		return serverResponse;
	}
	
	/*
	 * Get gamename and price from buy url
	 */
	final String[][] specialChars = {
			{"%99","™"},//trademark
			{"%A9","©"},//copyright
	};
	public HashMap<String,String> parsePurchaseURL(String url){
		HashMap<String,String> parameters = new HashMap<String,String>();
		String name=null;
		String price=null;
		String newQuerty="";
		try {
			//String querty = Uri.parse(url).getQuery();
			String querty = url.substring(url.indexOf("?"),url.length());
			String[] params = querty.split("&");
			for (String param : params){
				if (param.contains("gameName="))
					name = URLDecoder.decode(param.split("=")[1],"UTF-8");
				else if(param.contains("price="))
					price = URLDecoder.decode(param.split("=")[1],"UTF-8");
				else{
					if (newQuerty.length()>0)
						newQuerty+="&"+param;
					else
						newQuerty+= url.substring(0, url.indexOf('?')+1) + param;
				}
			}
			//replace special chars
			for (int x=0;x<specialChars[0].length;x++){
				if (name.contains(specialChars[x][0]))
					name = name.replace(specialChars[x][0], specialChars[x][1]);
			}
			parameters.put("gameName", URLDecoder.decode(name));
			parameters.put("price", URLDecoder.decode(price));
			parameters.put("query", newQuerty);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			DBG_EXCEPTION(e);
		}
		return parameters;
	}
	
	static String handledURL = null;
    
    /**
     * PSSWebViewClient. Manages all the events in the webView
     * 
     */
    public class PSSWebViewClient extends WebViewClient {
    	public ProgressDialog dialog = null;
    	
    	//PSSWebViewClient(){DBG( "PSS", "constructor PSSWebViewClient");}
    	@Override
		public boolean shouldOverrideUrlLoading(WebView view, String URL) {
		
			final String url = URL;
    		//need special override for opening full screen view of the url selected
			try{
					if (handledURL != null && handledURL.compareTo(url) == 0)
					{
						DBG("PSS","shouldOverrideUrlLoading URL already handled");
						return true;
					}else
						handledURL = url;
						
					DBG("PSS", "URL: "+url);
					
				#if PSS_VZW
					if( url.contains("vzw://") )
					{
						openBrowser(url);
						handledURL = null;
						return true;
					}else if( url.contains("pss://") )
					{
					
						if(dialog != null)
						{
							dialog.dismiss();
							dialog =null;
						
						}
			
						RequestGame(url);
					
						return true;
					}
				#endif
					//urls not handled by webView
					if (url.contains("hdplus/product.php?") || url.contains("hdplus/subscription.php?") || url.contains("hdplus/buy.php?") || url.contains("hdplus/prebuy.php?") || url.contains("hdplus/index.php?") 
					|| url.endsWith(".apk"))
					{
						DBG("PSS", "[shouldOverrideUrlLoading]");
						Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
						startActivity(browserIntent);
						handledURL = null;
						return true;
					}else if (url.contains("buy_pss.php?"))
					{
						//set up purchase dialog
						HashMap<String, String> m = parsePurchaseURL(url);
						setPurchaseDialogText(m.get("gameName"), m.get("price"));
						
						alertConfirmBuy = null;
						alertConfirmBuy = abConfirmBuy.create();
						
						if (!alertConfirmBuy.isShowing())
							alertConfirmBuy.show();
						handledURL = url;
						return true;
					}else if(url.contains("redownload_confirm.php?")){
						view.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
					#if PSS_VZW
						//	Turn wifi on only shows when wifi is off
						WifiManager wifiManager = (WifiManager) (Pss.this.getSystemService(Context.WIFI_SERVICE));
						if (!wifiManager.isWifiEnabled())
						{
							createDownloadConfirmDialog();
							handledURL = url;
							return true;
						} else {
							//check for uknowkn sources setting
							if (!canInstallFromOtherSources(Pss.this))
							{
								AlertDialog customD = createUnknownSourcesDialog(Pss.this, "");
								customD.show();
								handledURL = url;
								return true;
							}
						}
					#endif
					}
			}catch(StringIndexOutOfBoundsException e){
				//if the url return -1
			}
			view.loadUrl(url,map);
    		return true;
    	}
    	@Override
    	public void onPageStarted(WebView view, String url, Bitmap  favicon){
    		//DBG("PSS", "[onPageStarted]");
		#if PSS_VZW
			if( mVzwBilling.isInProgress() )
			{
				return;
			}
		#endif
			
			if (dialog == null){
    			dialog = new ProgressDialog(Pss.this);
    			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    			dialog.setCancelable(true);//rjsc added, let LOADING finish 
    			dialog.setMessage(getString(R.string.pss_LoadingText));
				dialog.setOnCancelListener(pdOnCancelListener);
    			dialog.show();
    			
    		}
    	}
		
		private void dismissDialog()
		{
			if(dialog != null)
       	 	{
   				dialog.dismiss();
       	 		dialog =null;
       	 	}
		}
		
		
    	@Override  
        public void onPageFinished(WebView view, String url){
    		//DBG("PSS", "[onPageFinished]");
			//JDUMP("PSS", url);
			dismissDialog();
			
    		//is network available cache is stored
    		if (isNetworkAvailable() && !url.startsWith("file:") && getLaunch_counter()==0 && isCacheValid())
    		{
    			if (getCachedUrlStored().length() > 0)	return;
				setCachedUrlStored(url);
    			SUtils.setPreference(PREFERENCE_KEY_PAGE_CACHED, getCachedUrlStored(), PREFERENCES_NAME);
    			DBG("PSS", "[onPageFinished]\tSet cache "+getCachedUrlStored());
    		}
			
    	}
    	@Override
    	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
    		//DBG("PSS", "[onReceivedError]");
    		dismissDialog();
			
    		if (getCachedUrlStored() != null && cachedUrlStored.length() >0 && isNetworkAvailable()){
    			//cache is corrupted and network is available
			clearCache();
    			changeWebViewURL(START_UP_URL, WebSettings.LOAD_NO_CACHE);
    			DBG("PSS", "\t[onReceivedError]\tClear cache and reconnect again");
    		}else if (getCachedUrlStored() != null && cachedUrlStored.length() >0 ){
    			//cache is corrupted and not network connection
			clearCache();
    			changeWebViewURL(ERROR_PAGE, WebSettings.LOAD_NO_CACHE);
    			DBG("PSS", "\t[onReceivedError]\tClear cache and load error page");
    		}else{
    			//neither cache or network found
    			changeWebViewURL(ERROR_PAGE, WebSettings.LOAD_NO_CACHE);
    			DBG("PSS", "\t[onReceivedError]\tNo cache adn no connection found, sent to error");
    		}
    	}
    	
    	public void openBrowser(String url)
	    {
	        if(url == null || url.length() <= 0)
	            return;

	        try
	        {
	            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	            startActivity(i);
	        }
	        catch (Exception e)
	        {
	            DBG_EXCEPTION(e);
	        }
	    }
    	

    }
}
#endif