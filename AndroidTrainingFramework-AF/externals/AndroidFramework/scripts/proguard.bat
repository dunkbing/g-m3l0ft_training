@echo off

if exist "%ANDROID_FRAMEWORK_CONFIG%\config.bat" (
	call %ANDROID_FRAMEWORK_CONFIG%\config.bat
)

REM ------------- Proguard Config -------------
echo ::: Proguard config
copy /Y proguard.properties %PROGUARD_FILE%

echo #config added in preprocess.bat keep jni required methods (marked as public static on classes).>>%PROGUARD_FILE%
echo -keep public class * extends android.app.Activity>>%PROGUARD_FILE%
echo -keep public class * extends android.opengl.GLSurfaceView>>%PROGUARD_FILE%
echo -keepclasseswithmembers class * {native ^<methods^>;}>>%PROGUARD_FILE%
echo -keepclasseswithmembers class * {static ^<methods^>;}>>%PROGUARD_FILE%
echo -keepclasseswithmembers class * { public byte[] getVersionGame();}>>%PROGUARD_FILE%

echo -keep public class * implements %APP_PACKAGE%.PackageUtils.PluginSystem.IPluginEventReceiver>>%PROGUARD_FILE%
echo -keep public class * implements IPluginEventReceiver>>%PROGUARD_FILE%

if "%USE_CMBILLING%"=="1" (
	echo -injars ../libs/CMBilling.jar>>%PROGUARD_FILE%	
	echo -injars ../libs/irdeto_anticopy.jar>>%PROGUARD_FILE%	
)

if "%USE_INSTALLER%"=="1" (
	echo -keepclassmembers class %APP_PACKAGE%.installer.GameInstaller{static ^<fields^>;}>>%PROGUARD_FILE%	
)

rem DEPRECATED
rem if "%USE_GLLIVE%"=="1" (
rem 	echo -keep final class %APP_PACKAGE%.GLiveMain$GLiveJavaScriptInterface>>%PROGUARD_FILE%
rem 	echo -keepclasseswithmembers class %APP_PACKAGE%.GLiveMain$GLiveJavaScriptInterface {public ^<methods^>;}>>%PROGUARD_FILE%
	
	
)

if "%USE_SOCIALLIB%"=="1" (
	echo -keepattributes Signature>>%PROGUARD_FILE%
	echo -dontwarn com.gameloft.GLSocialLib.GameAPI.**>>%PROGUARD_FILE%
	echo -keep class com.gameloft.GLSocialLib.GameAPI.** {*;}>>%PROGUARD_FILE%

	echo -dontwarn com.facebook.**>>%PROGUARD_FILE%
	echo -keep class com.facebook.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn com.facebook.widget.**>>%PROGUARD_FILE%
	echo -keep class com.facebook.widget.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn com.gameloft.GLSocialLib.facebook.**>>%PROGUARD_FILE%
	echo -keep class com.gameloft.GLSocialLib.facebook.** {*;}>>%PROGUARD_FILE%

	echo -dontwarn com.kakao.api.**>>%PROGUARD_FILE%
	echo -keep class com.kakao.api.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn com.kakao.api.story.**>>%PROGUARD_FILE%
	echo -keep class com.kakao.api.story.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn com.gameloft.GLSocialLib.kakao.**>>%PROGUARD_FILE%
	echo -keep class com.gameloft.GLSocialLib.kakao.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn org.apache.http.entity.mime.**>>%PROGUARD_FILE%
	echo -keep class org.apache.http.entity.mime.** {*;}>>%PROGUARD_FILE%

	echo -dontwarn com.amazon.**>>%PROGUARD_FILE%
	echo -keep class com.amazon.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn com.amazon.identity.auth.device.datastore.**>>%PROGUARD_FILE%
	echo -keep class com.amazon.identity.auth.device.datastore.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn com.gameloft.GLSocialLib.gamecircle.**>>%PROGUARD_FILE%
	echo -keep class com.gameloft.GLSocialLib.gamecircle.** {*;}>>%PROGUARD_FILE%

	
	echo -dontwarn android.support.v4.content.**>>%PROGUARD_FILE%
	echo -keep class android.support.v4.content.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn android.support.v4.os.**>>%PROGUARD_FILE%
	echo -keep class android.support.v4.os.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn android.support.v4.view.**>>%PROGUARD_FILE%
	echo -keep class android.support.v4.view.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn android.support.v4.widget.**>>%PROGUARD_FILE%
	echo -keep class android.support.v4.widget.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn android.net.http.**>>%PROGUARD_FILE%
	echo -keep class android.net.http.** {*;}>>%PROGUARD_FILE%
	
	echo -dontwarn com.weibo.sdk.android.**>>%PROGUARD_FILE%
	echo -keep class com.weibo.sdk.android.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn com.gameloft.GLSocialLib.weibo.**>>%PROGUARD_FILE%
	echo -keep class com.gameloft.GLSocialLib.weibo.** {*;}>>%PROGUARD_FILE%
	
	echo -dontwarn com.renren.**>>%PROGUARD_FILE%
	echo -keep class com.renren.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn com.renren.mobile.rmsdk.oauth.auth.internal.**>>%PROGUARD_FILE%
	echo -keep class com.renren.mobile.rmsdk.oauth.auth.internal.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn com.renren.mobile.rmsdk.oauth.auth.**>>%PROGUARD_FILE%
	echo -keep class com.renren.mobile.rmsdk.oauth.auth.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn com.gameloft.GLSocialLib.renren.**>>%PROGUARD_FILE%
	echo -keep class com.gameloft.GLSocialLib.renren.** {*;}>>%PROGUARD_FILE%
	
	
	echo -dontwarn org.apache.commons.net.ntp.**>>%PROGUARD_FILE%
	echo -keep class org.apache.commons.net.ntp.** {*;}>>%PROGUARD_FILE%
	echo -dontwarn com.jirbo.adcolony.**>>%PROGUARD_FILE%
	echo -keep class com.jirbo.adcolony.** {*;}>>%PROGUARD_FILE%
	
)
rem DEPRECATED
rem if "%USE_GLLIVE_HTML5%"=="1" (
rem 	echo -keep final class %APP_PACKAGE%.GLiveHTML.GLLiveActivity$GLiveJavascriptInterface>>%PROGUARD_FILE%
rem 	echo -keepclasseswithmembers class %APP_PACKAGE%.GLiveHTML.GLLiveActivity$GLiveJavascriptInterface {public ^<methods^>;}>>%PROGUARD_FILE%
rem )

if "%USE_IGP_FREEMIUM%"=="1" (
	if "%USE_IGP_REWARDS%"=="1" (
		echo -keep final class %APP_PACKAGE%.IGPFreemiumActivity$IGPInterface>>%PROGUARD_FILE%
		echo -keepclasseswithmembers class %APP_PACKAGE%.IGPFreemiumActivity$IGPInterface {public ^<methods^>;}>>%PROGUARD_FILE%
	)
)

rem DEPRECATED
rem if "%USE_DIRECT_IGP%"=="1" (
rem 	echo -keep final class %APP_PACKAGE%.DirectIGP.DirectIGPActivity$IGPInterface>>%PROGUARD_FILE%
rem 	echo -keepclasseswithmembers class %APP_PACKAGE%.DirectIGP.DirectIGPActivity$IGPInterface {public ^<methods^>;}>>%PROGUARD_FILE%
rem )

if "%USE_ADS_SERVER%"=="1" (
	echo -keepclasseswithmembers class %APP_PACKAGE%.AdServerVideos$JSInterface {public ^<methods^>;}>>%PROGUARD_FILE%
	
	if "%ADS_USE_TAPJOY%"=="1" (
		echo -keep class com.tapjoy.** { *; }>>%PROGUARD_FILE%
		echo -keepattributes JavascriptInterface>>%PROGUARD_FILE%
	)
	
	if "%ADS_USE_ADCOLONY%"=="1" (
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/adcolony.jar(!META-INF/MANIFEST.MF^) >>%PROGUARD_FILE%
	)
	if "%ADS_USE_YUME%"=="1" (
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/YuMeAndroidSDK.jar(!META-INF/MANIFEST.MF^) >>%PROGUARD_FILE%
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/backport-util-concurrent-3.1.jar(!META-INF/MANIFEST.MF^) >>%PROGUARD_FILE%
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/commons-codec-1.3.jar(!META-INF/MANIFEST.MF^) >>%PROGUARD_FILE%
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/commons-lang-2.4.jar(!META-INF/MANIFEST.MF^) >>%PROGUARD_FILE%
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/commons-logging-1.1.1.jar(!META-INF/MANIFEST.MF^) >>%PROGUARD_FILE%
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/ical4j-1.0.jar(!META-INF/MANIFEST.MF^) >>%PROGUARD_FILE%
		echo -keep class com.yume.android.sdk.** {*;}>>%PROGUARD_FILE%
		echo -keep class net.fortuna.ical4j.** {*;}>>%PROGUARD_FILE%
		echo -keep class edu.emory.mathcs.backport.java.util.** {*;}>>%PROGUARD_FILE%
		echo -keep class org.apache.commons.codec.** {*;}>>%PROGUARD_FILE%
		echo -keep class org.apache.commons.lang.** {*;}>>%PROGUARD_FILE%
		echo -keep class org.apache.commons.logging.** {*;}>>%PROGUARD_FILE%
		echo -dontwarn sun.misc.**>>%PROGUARD_FILE%
		echo -dontwarn org.apache.**>>%PROGUARD_FILE%
		echo -dontwarn javax.servlet.**>>%PROGUARD_FILE%
	)
	if "%ADS_USE_CHARTBOOST%"=="1" (
		echo -keep class com.chartboost.** {*;}>>%PROGUARD_FILE%
		echo -keep class com.mongodb.** {*;}>>%PROGUARD_FILE%
		echo -keep class org.bson.** {*;}>>%PROGUARD_FILE%
		echo -dontwarn com.mongodb.**>>%PROGUARD_FILE%
		echo -dontwarn org.bson.**>>%PROGUARD_FILE%
	)
	if "%ADS_USE_FLURRY%"=="1" (
		echo -keep class com.flurry.** {*;}>>%PROGUARD_FILE%
		echo -dontwarn com.flurry.**>>%PROGUARD_FILE%
		echo -keepattributes *Annotation*>>%PROGUARD_FILE%
	)
)

REM DRMS
if "%VERIZON_DRM%"=="1" (
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/VzwProtectedApplicationLibrary.jar>>%PROGUARD_FILE%
	echo -keep public class com.android.vending.licensing.ILicensingService>>%PROGUARD_FILE%
	echo -keep public class com.verizon.vcast.apps.LicenseAuthenticator>>%PROGUARD_FILE%
) else if "%USE_SKT_DRM%"=="1" (
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/SK_DRM_CLASS.jar>>%PROGUARD_FILE%
	echo -keep public class com.skt.arm.aidl.IArmService>>%PROGUARD_FILE%
) else  if "%USE_LGU_DRM%"=="1" (
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/LGT_DRM.jar>>%PROGUARD_FILE%
	echo -keep public class com.lgt.arm.ArmInterface>>%PROGUARD_FILE%
) else if "%USE_LGW_DRM%"=="1" (
	if "%FOR_LGW_GLOBAL%"=="1" (
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/lgcoconut_gb.jar(!META-INF/MANIFEST.MF^)>>%PROGUARD_FILE%
		echo -keep public class com.lge.lgworld.coconut.client.LGLicenseChecker>>%PROGUARD_FILE%
		echo -keep public class com.lge.lgworld.coconut.client.LGLicenseCheckerCallback>>%PROGUARD_FILE%
		echo -keep public class com.lge.lgworld.coconut.client.LGLicenseObfuscator>>%PROGUARD_FILE%
	) else (
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/lgcoconut_kr.jar(!META-INF/MANIFEST.MF^)>>%PROGUARD_FILE%
		echo -keep public class com.lg.apps.cubeapp.coconut.client.LGLicenseChecker>>%PROGUARD_FILE%
		echo -keep public class com.lg.apps.cubeapp.coconut.client.LGLicenseCheckerCallback>>%PROGUARD_FILE%
		echo -keep public class com.lg.apps.cubeapp.coconut.client.LGLicenseObfuscator>>%PROGUARD_FILE%
		echo -keep public class com.lg.apps.cubeapp.coconut.client.util.CoconutLog>>%PROGUARD_FILE%
	)
) else  if "%GOOGLE_DRM%"=="1" (
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/GoogleAntiPiracyLibrary.jar(!META-INF/MANIFEST.MF^)>>%PROGUARD_FILE%	
) else  if "%USE_KT_DRM%"=="1" (
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/KAFINFO.jar>>%PROGUARD_FILE%
	echo -keep class com.kaf.** { *; }>>%PROGUARD_FILE%
) else if "%USE_SAMSUNG_DRM%"=="1" (
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/Zirconia.jar>>%PROGUARD_FILE%
	echo -keep class com.samsung.zirconia.* { *; }>>%PROGUARD_FILE%
)

rem DEPRECATED
rem REM GLLIVE HTML5
rem if "%USE_GLLIVE_HTML5%"=="1" (
rem 	if "%USE_MARKET_INSTALLER%"=="0" (
rem 		if "%GLLIVE_EMBED_PAYPAL%" == "1" (
rem 			if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/PayPal_MPL.jar>>%PROGUARD_FILE%
rem 			echo -keep class com.paypal.android.** { *; }>>%PROGUARD_FILE%
rem 		)	
rem 		if "%GLLIVE_EMBED_BOKU%"=="1" (
rem 			if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/BokuSDK_v2.3.jar>>%PROGUARD_FILE%
rem 			echo -keep class com.boku.mobile.android.** { *; }>>%PROGUARD_FILE%
rem 		)
rem 	)
rem )

REM DIRECT IGP
if "%IGP_PAYPAL%" == "1" (
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/PayPal_MPL.jar>>%PROGUARD_FILE%
	echo -keep class com.paypal.android.** { *; }>>%PROGUARD_FILE%
)	
if "%IGP_BOKU%"=="1" (
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/BokuSDK_v2.3.jar>>%PROGUARD_FILE%
	echo -keep class com.boku.mobile.android.** { *; }>>%PROGUARD_FILE%
)

REM STORES
if "%VZW_STORE%"=="1" (
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/VZW-SDK.jar>>%PROGUARD_FILE%
	echo -keep class com.verizon.vcast.apps.** { *; }>>%PROGUARD_FILE%
)
if "%SAMSUNG_STORE%"=="1" (
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/plasma.jar>>%PROGUARD_FILE%
	echo -keep class com.samsungapps.plasma.** { *; }>>%PROGUARD_FILE%
)
if "%BOKU_STORE%"=="1" (
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/BokuSDK_v2.3.jar>>%PROGUARD_FILE%
	echo -keep class com.boku.mobile.android.** { *; }>>%PROGUARD_FILE%
)else if "%PANTECH_STORE%"=="1" (
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/IABL_Lib.jar>>%PROGUARD_FILE%
	if "%USE_PANTECH_ARM%"=="1" (
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/arm-client.jar>>%PROGUARD_FILE%
	)
	echo -keep class com.pantech.app.appsplay.iabl.** { *; }>>%PROGUARD_FILE%
	if "%USE_PANTECH_ARM%"=="1" (
		echo -keep class com.pantech.appcross.armagent.** { *; }>>%PROGUARD_FILE%
	)
) else  if "%SKT_STORE%"=="1" (

		echo -keep class com.skplanet.dodo.**{*;}>>%PROGUARD_FILE%
		echo -keep class com.skplanet.internal.dodo.**{*;}>>%PROGUARD_FILE%
		echo -keep class com.skplanet.internal.dodo.dev.**{*;}>>%PROGUARD_FILE%
		echo -keep class com.skplanet.internal.dodo.util.**{*;}>>%PROGUARD_FILE%
		echo -keep class com.skplanet.pmss.secure.**{*;}>>%PROGUARD_FILE%
		echo -keep public class android.net.http.SslError>>%PROGUARD_FILE%
		echo -keep public class android.webkit.WebViewClient>>%PROGUARD_FILE%
		echo -keep class com.tmoney.aidl.**{*;}>>%PROGUARD_FILE%
		
		echo -dontwarn android.webkit.WebView>>%PROGUARD_FILE%
		echo -dontwarn android.net.http.SslError>>%PROGUARD_FILE%
		echo -dontwarn android.webkit.WebViewClient>>%PROGUARD_FILE% 
		
		echo -keep class com.google.gson.**{*;}>>%PROGUARD_FILE%
		echo -keep class com.skplanet.dev.guide.pdu.**{*;}>>%PROGUARD_FILE%
		echo -keepclasseswithmembers class %APP_PACKAGE%.iab.pdu.Response{*;}>>%PROGUARD_FILE%
		echo -keepclasseswithmembers class %APP_PACKAGE%.iab.pdu.Response$Result{*;}>>%PROGUARD_FILE%
		echo -keepclasseswithmembers class %APP_PACKAGE%.iab.pdu.Response$Product{*;}>>%PROGUARD_FILE%
		
		echo -keepattributes Signature>>%PROGUARD_FILE%
		echo -dontshrink>>%PROGUARD_FILE%
		
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/android-support-v4.jar>>%PROGUARD_FILE%
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/gson-2.2.2.jar>>%PROGUARD_FILE%
		
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/iap_plugin-14.01.01.jar>>%PROGUARD_FILE%


REM echo -keep class com.feelingk.iap.** { *; }>>%PROGUARD_FILE%
) else if "%GOOGLE_STORE_V3%" == "1" (
		echo -keep class com.android.vending.billing.**	>>%PROGUARD_FILE%
) else if "%BAZAAR_STORE%" == "1" (
		echo -keep class com.android.vending.billing.**	>>%PROGUARD_FILE%
) else if "%VXINYOU_STORE%" == "1" (
		echo -dontwarn com.vxinyou.**>>%PROGUARD_FILE%
		echo -keep class com.vxinyou.box.sdk.**	>>%PROGUARD_FILE%
        echo -keep class com.vxinyou.box.tools.**>>%PROGUARD_FILE%
        echo -keep class com.vxinyou.boxclient.common.utils.net.**>>%PROGUARD_FILE%
        echo -keep class com.vxinyou.boxclient.control.**>>%PROGUARD_FILE%
) else if "%AMAZON_STORE%" == "1" (
	echo -dontwarn com.amazon.**>>%PROGUARD_FILE%
	echo -keep class com.amazon.** {*;}>>%PROGUARD_FILE%
	echo -keepattributes *Annotation*>>%PROGUARD_FILE%
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/com.amazon.inapp.purchasing.jar>>%PROGUARD_FILE%
) else if "%KT_STORE%"=="1" (
	echo -dontwarn com.kt.olleh.inapp.IRemoteInapService>>%PROGUARD_FILE%
	echo -dontwarn com.kt.olleh.inapp.IRemoteInapService$Stub>>%PROGUARD_FILE%
	if "%KT_TABLET_API%" == "1" (
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/InAppTablet.jar>>%PROGUARD_FILE%
	) else (
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/InApp.jar>>%PROGUARD_FILE%
	)
) else if "%YANDEX_STORE%" == "1" (
	echo -keep class com.yandex.store.service.**	>>%PROGUARD_FILE%
) else if "%ATET_STORE%" == "1" (

	echo -keep public class com.iapppay.pay.mobile.iapppaysecservice.R$*{ public static final int *;  } >>%PROGUARD_FILE%

	echo -keepattributes Exceptions,InnerClasses >>%PROGUARD_FILE%
	echo -keep public class com.alipay.android.app.** { >>%PROGUARD_FILE%
	echo public ^<fields^>; >>%PROGUARD_FILE%
	echo public ^<methods^>; } >>%PROGUARD_FILE%

	echo -keep public class com.tenpay.android.service.** { >>%PROGUARD_FILE%
	echo public ^<fields^>; >>%PROGUARD_FILE%
	echo public ^<methods^>; } >>%PROGUARD_FILE%

	
	echo -keep class com.iapppay.mpay.** {*;} >>%PROGUARD_FILE%
	echo -keep public class com.iapppay.pay.mobile.iapppaysecservice.utils.DesProxy{ >>%PROGUARD_FILE%
	echo native ^<methods^>; } >>%PROGUARD_FILE%

	echo -keep public class com.iapppay.pay.mobile.iapppaysecservice.res2jar.String_List{ >>%PROGUARD_FILE%
	echo public ^<fields^>; } >>%PROGUARD_FILE%

	echo -keep public class com.iapppay.pay.mobile.iapppaysecservice.res2jar.Id_List{ >>%PROGUARD_FILE%
	echo public ^<fields^>; } >>%PROGUARD_FILE%

	echo -keep public class com.iapppay.oneclickpay.PayUtil{*;} >>%PROGUARD_FILE%
	echo -keep public class com.iapppay.oneclickpay.IPayCallBack{*;} >>%PROGUARD_FILE%
	echo -keep public class com.yintong.pay.sdk.** {*;} >>%PROGUARD_FILE%
	echo -keep public class com.iapppay.fastpay.util.DesProxy{ >>%PROGUARD_FILE%
	echo native ^<methods^>; } >>%PROGUARD_FILE%
)

if "%GAMELOFT_SHOP%"=="1" (
    echo -keepclasseswithmembers class %APP_PACKAGE%.iab.InAppBillingActivity$MyJavaScriptInterface {public ^<methods^>;}>>%PROGUARD_FILE%
)

if "%USE_BILLING%" == "1" (
	if "%USE_BOKU_FOR_BILLING%" == "1" (
		echo -injars ../libs/BokuSDK_v2.3.jar>>%PROGUARD_FILE%
		echo -keep class com.boku.mobile.android.** { *; }>>%PROGUARD_FILE%
	)
	if "%USE_BILLING_FOR_CHINA%"=="1" (
		if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/huafubao_plugin_android.jar>>%PROGUARD_FILE%
	)
)

REM PUSH NOTIFICATIONS
if "%AMAZON_STORE%" == "1" goto DO_ADM
goto SKIP_ADM
:DO_ADM
	echo -libraryjars ../ext_libs/adm.jar>>%PROGUARD_FILE%
	echo -keep public class %APP_PACKAGE%.PushNotification.ADMMessageHandler>>%PROGUARD_FILE%
	goto SKIP_GCM
:SKIP_ADM

if "%USE_NOKIA_API%" == "1" (
	echo -keep class * extends PushBaseIntentService>>%PROGUARD_FILE%
	if %ANDROID_SDK_REVISION% LSS 14 echo -injars ../libs/nokia.jar>>%PROGUARD_FILE%
	goto SKIP_GCM
)

REM PUSH NOTIFICATIONS
if "%USE_VZW_BILLING%" == "1" (
	echo -keepclassmembers class %APP_PACKAGE%.vzb.vzb { public static android.app.Activity cA; }>>%PROGUARD_FILE%
)


if exist "%GAME_SPECIFIC_PROGUARD%" (
    type %GAME_SPECIFIC_PROGUARD%>>%PROGUARD_FILE%
)

if "%ADD_GOOGLE_PLAY_SERVICES%"=="1" (
	echo.>>%PROGUARD_FILE%
	echo -keep class com.google.android.gms.** >>%PROGUARD_FILE%
	echo -dontwarn com.google.android.gms.**>>%PROGUARD_FILE%
	REM NOTE: leave an empty line before including the proguard.txt from Google Play Services
	echo.>>%PROGUARD_FILE%
	type %ROOT_DIR%\res_apk\utils\external_libs\google-play-services_lib\proguard.txt>>%PROGUARD_FILE%
)