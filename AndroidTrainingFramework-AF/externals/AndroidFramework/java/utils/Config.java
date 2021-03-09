package APP_PACKAGE.GLUtils;

public interface Config {

	public static String            	GGC = GAME_ACTIVITY_NAME_STR;
	
	//*************************************************************************************************************
	//* DEBUG SETTINGS.
	//*************************************************************************************************************
	
	/** This value "MUST BE NULL" for gold releases.
    * x_up_subno is a value that nomally would be attached by the sim, but in emulator or roaming test, tis vamue must be hardcoded 
    * AT&T tests    : "swm_10a57682ca8_vmag.mycingular.net"
	* Rogers tests	: "1171470365-9685569_rogerspush.gprs.rogers.com"
    * TMobile tests : null
    */
	String x_up_subno = null;
	
	/** These values can be gathered from the SIM card.
	* Rogers tests	: 1514519213
	*/
	String x_up_calling_line_id = null;
	
	/** These values can be gathered from the SIM card.
	* Rogers tests	: rogerspush.gprs.rogers.com
	*/
	String x_up_uplink = null;
	
	/** These values can be gathered from the SIM card.
	* TMobile tests	: 16467757343
	*/
	String x_nokia_msisdn = null;

	/** Set to false when connecting from an emulator, or from a not ATandT phone. */
    public static boolean ENABLE_PROXY_CONNECTION = false;

    /** To enable debugging. */
    public static boolean ENABLE_DEBUG = false;

    /** This is useful to avoid charges to the SIM card. */
    boolean use_test_values = true;
//#if USE_BILLING	
	public final boolean USE_TRACKING_BILLING = (USE_TRACKING_FEATURE_BILLING == 1);
	//public final boolean USE_TRACKING_BILLING = false;
//#endif

//#if USE_TRACKING_FEATURE_INSTALLER
	public final boolean USE_TRACKING_INSTALLER = (USE_TRACKING_FEATURE_INSTALLER == 1);
	
	public final int 			GAME_LAUNCHED_FOR_THE_FIRST_TIME = 0;
	public final int 			RESOURCE_FILE_DOWNLOADED = 1;
	public final int 			CC_BILLING_SUCCESS = 2;
	public final int 			CC_BILLING_ERROR = 3;
	public final int 			HTTP_BILLING_SUCCESS = 4;
	public final int 			HTTP_BILLING_ERROR = 5;
	public final int 			PSMS_BILLING_SUCCESS = 6;
	public final int 			PSMS_BILLING_ERROR = 7;
	public final int 			RESOURCE_FILE_DOWNLOAD_START = 8;
//#endif
}

