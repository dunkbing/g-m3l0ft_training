


const char* jclass_OS[] = 
{
	"java/lang/Class", 
	"android/os/Build",
	"android/content/Intent",
	"android/os/Bundle",
	"android/media/AudioTrack",
	"java/security/SecureRandom",
	"java/util/HashSet",
	"java/lang/Long",
	"java/security/spec/X509EncodedKeySpec",
	"java/security/KeyFactory",
	"java/security/NoSuchAlgorithmException",
	"java/security/spec/InvalidKeySpecException",
	"com/gameloft/glotv3/PortingJNIv3",
	"com/gameloft/gameoptions/DeviceOptions",
	"com/gameloft/GLSocialLib/PlatformAndroid",
	"com/gameloft/GLSocialLib/facebook/FacebookAndroidGLSocialLib",
#if AMAZON_STORE
	"com/gameloft/GLSocialLib/gamecircle/GameCircleAndroidGLSocialLib",
#else //GameApi
	"com/gameloft/GLSocialLib/GameAPI/GameAPIAndroidGLSocialLib",
#endif //AMAZON_STORE
};

const char* jclass_const[] = 
{
	"/PackageUtils/AndroidUtils",
	"/PackageUtils/LogoViewPlugin",
	"/DataSharing",
	"/GLUtils/controller/NativeBridgeHIDControllers",
	"/GLUtils/Device",
	"/iab/InAppBilling",
	"/iab/common/Base64",
	"/iab/common/Base64DecoderException",
	"/PushNotification/SimplifiedAndroidUtils",
	"/SendInfo",
	//av todo:  SplashScrenActivity should be via #if USE_WELCOME_SCREEN/ CRM
	"/SplashScreenActivity",
	"/InGameBrowser",
#if USE_ADS_SERVER
	"/PackageUtils/AdServerPlugin",
#endif 
#if USE_IGP_FREEMIUM
	"/PackageUtils/InGamePromotionPlugin",
#endif
	
};

