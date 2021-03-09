Changes needed to upgrade to Burstly ads server from previous version without Burstly:

In config.bat (see config.bat.template):

Set flag: ADS_USE_BURSTLY=1
Set flag: ADS_BURSTLY_PUB_ID to the Burstly Publisher ID
Set flag: ADS_BURSTLY_ZONE_ID_1 to the Burstly Zone ID for banners
Set flag: ADS_BURSTLY_ZONE_ID_2 to the Burstly Zone ID for offer wall


In Game.java:

// onDestroy, onResume, onPause config for burstly:
@Override
public void onDestroy()
{
#if ADS_USE_BURSTLY
	adServer.onDestroy();
#endif
	super.onDestroy();
}

@Override
public void onResume()
{
#if ADS_USE_BURSTLY
	adServer.onResume();
#endif
	super.onResume();
}

@Override
public void onPause()
{
#if ADS_USE_BURSTLY
	adServer.onPause();
#endif	
	super.onPause();
}


In AndroidManifest.xml (see AndroidManifest.xml.template):

Replace in the #if USE_ADS_SERVER section:
<activity android:name="com.inmobi.androidsdk.IMBrowserActivity" android:configChanges="keyboardHidden|orientation|keyboard" />   
<activity android:name="com.google.ads.AdActivity" android:configChanges="keyboard|keyboardHidden|orientation"/>

with:
#if ADS_USE_BURSTLY
	<activity android:name=".GameActivity" />
	<activity android:name=".InterstitialAdActivity" />
	
	<!-- Admob ===================================================== -->
	<activity android:name="com.google.ads.AdActivity"
		android:configChanges="keyboard|keyboardHidden|orientation" />
		
	<!-- Native burstly interstitials ======================================== -->
	<activity android:name="com.burstly.lib.component.networkcomponent.burstly.BurstlyFullscreenActivity" 
		android:configChanges="keyboard|keyboardHidden|orientation" 
		android:theme="@android:style/Theme.NoTitleBar.Fullscreen" />
	<!-- ========================================================= -->
#else
	<activity android:name="com.inmobi.androidsdk.IMBrowserActivity" android:configChanges="keyboardHidden|orientation|keyboard" />   
	<activity android:name="com.google.ads.AdActivity" android:configChanges="keyboard|keyboardHidden|orientation"/>
#endif