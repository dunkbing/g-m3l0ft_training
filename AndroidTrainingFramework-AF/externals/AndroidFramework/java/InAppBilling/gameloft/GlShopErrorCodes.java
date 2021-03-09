package APP_PACKAGE.iab;

import java.util.Map;
import APP_PACKAGE.GLUtils.XPlayer;

public class GlShopErrorCodes
{
	
	public final static int WAP_OTHER_ERROR_NOT_VALID_USER								= -1;
	public final static int WAP_OTHER_ERROR_OBB_FAILED									= -3;
	public final static int WAP_OTHER_ERROR_DB											= -5;
	public final static int WAP_OTHER_ERROR_USER_CANCEL									= -7;
	public final static int WAP_OTHER_ERROR_NO_PURCHASE_RECORDED						= -9;
	public final static int WAP_OTHER_ERROR_LIMITS_REACHED_BY_DOWNLOAD_CODE_OR_IMEI		= -3001;
	public final static int WAP_OTHER_ERROR_PURCHASE									= -11;
	public final static int WAP_OTHER_ERROR_NOT_VALID_USER_PARAMETER_NOT_RIGHT			= -13;
	public final static int WAP_OTHER_ERROR_OTHER_ERROR									= -15;
	public final static int WAP_OTHER_ERROR_TRANSACTION_LIMITS							= -43;
	public final static int WAP_OTHER_ERROR_FRAUD_SUSP_LIMITS_EXCEDED					= -3002;
	
	public final static int PHONE_BILL_ERROR_INVALID_SECURITY_CODE						= 1;
	public final static int PHONE_BILL_ERROR_USER_ID_NOT_FOUND							= 2;
	public final static int PHONE_BILL_ERROR_MISSING_B_PARAMETER						= 3;
	public final static int PHONE_BILL_ERROR_INVALID_REQUEST							= 4;
	public final static int PHONE_BILL_ERROR_INVALID_PRICE_POINTS 						= 6;
	public final static int PHONE_BILL_ERROR_PAYMENT_PROCESS_FAILED						= 7;
	public final static int PHONE_BILL_ERROR_MISSING_GGI								= 8;
	public final static int PHONE_BILL_ERROR_MISSING_USER_ONLINE_ACCOUNT_NUMBER			= 9;
	public final static int PHONE_BILL_ERROR_INVALID_PURCHASE_ID						= 10;
	public final static int PHONE_BILL_ERROR_INVALID_ONLINE_USER						= 11;
	public final static int PHONE_BILL_ERROR_INVALID_CONTENT_ID							= 12;
	public final static int PHONE_BILL_ERROR_BLOCK_AND_SET_LOCK_RENEW_TIME				= 13;
	public final static int PHONE_BILL_ERROR_UNKNOW_PORTAL_CODE							= 19;
	public final static int PHONE_BILL_ERROR_BLACKLISTER_USER							= 20;
	public final static int PHONE_BILL_ERROR_REACHED_HOURLY_AMOUNT_LIMIT				= 21;
	public final static int PHONE_BILL_ERROR_REACHED_HOURLY_SUM_LIMIT					= 22;
	public final static int PHONE_BILL_ERROR_REACHED_DAILY_AMOUNT_LIMIT					= 23;
	public final static int PHONE_BILL_ERROR_REACHED_DAYLY_SUM_LIMIT					= 24;
	public final static int PHONE_BILL_ERROR_REACHED_WEEKLY_AMOUNT_LIMIT				= 25;
	public final static int PHONE_BILL_ERROR_REACHED_WEEKLY_SUM_LIMIT					= 26;
	public final static int PHONE_BILL_ERROR_CONNECTION									= XPlayer.ERROR_CONNECTION;
	public final static int PHONE_BILL_ERROR_UNKNOWN									= XPlayer.ERROR_INIT;
	public final static int PHONE_BILL_ERROR_BAD_RESPONSE								= XPlayer.ERROR_BAD_RESPONSE;
	
	public final static int SMS_REG_MO_ERROR_INVALID_IGP_CODE 							= 1;
	public final static int SMS_REG_MO_ERROR_INVALID_GENERATED_CODE						= 2;
	public final static int SMS_REG_MO_ERROR_INVALID_CONTENT_ID 						= 3;
	public final static int SMS_REG_MO_ERROR_INVALID_PRICE 								= 4;
	public final static int SMS_REG_MO_ERROR_INVALID_MONEY								= 5;
	public final static int SMS_REG_MO_ERROR_INVALID_PROFILE_ID 						= 6;
	public final static int SMS_REG_MO_ERROR_INVALID_PLATFORM_ID						= 7;
	public final static int SMS_REG_MO_ERROR_INVALID_LANGUAGE_ID 						= 8;
	public final static int SMS_REG_MO_ERROR_INVALID_GGI 								= 9;
	public final static int SMS_REG_MO_ERROR_INVALID_GAMELOFT_LIVE_ACCOUNT_NUMBER 		= 10;
	public final static int SMS_REG_MO_ERROR_INVALID_DOWNLOAD_CODE 						= 11;
	public final static int SMS_REG_MO_ERROR_INVALID_IMEI 								= 12;
	public final static int SMS_REG_MO_ERROR_DUPLICATE_REQUEST_UNDER_5_MINUTES 			= 13;
	public final static int SMS_REG_MO_ERROR_REACHED_HOURLY_AMOUNT_LIMIT 				= 21;
	public final static int SMS_REG_MO_ERROR_REACHED_HOURLY_SUM_LIMIT 					= 22;
	public final static int SMS_REG_MO_ERROR_REACHED_DAILY_AMOUNT_LIMIT 				= 23;
	public final static int SMS_REG_MO_ERROR_REACHED_DAILY_SUM_LIMIT 					= 24;
	public final static int SMS_REG_MO_ERROR_REACHED_WEEKLY_AMOUNT_LIMIT 				= 25;
	public final static int SMS_REG_MO_ERROR_REACHED_WEEKLY_SUM_LIMIT 					= 26;
	public final static int SMS_REG_MO_ERROR_REACHED_MONTHLY_AMOUNT_LIMIT 				= 27;
	public final static int SMS_REG_MO_ERROR_INVALID_REACHED_MONTHLY_SUM_LIMIT 			= 28;
	public final static int SMS_REG_MO_ERROR_CONNECTION									= XPlayer.ERROR_CONNECTION;
	public final static int SMS_REG_MO_ERROR_UNKNOWN									= XPlayer.ERROR_INIT;
	public final static int SMS_REG_MO_ERROR_BAD_RESPONSE								= XPlayer.ERROR_BAD_RESPONSE;
	
	public final static int SMS_CHECK_ERROR_UNKNOWN_GENERIC 							= 0;
	public final static int SMS_CHECK_ERROR_NO_SMS_INFO_FOUND_IN_DB						= 1;
	public final static int SMS_CHECK_ERROR_PROFILE_NOT_FOUND_IN_DB 					= 2;
	public final static int SMS_CHECK_ERROR_BILLING_FILE_NOT_SET_IN_PROFILE 			= 3;
	public final static int SMS_CHECK_ERROR_BILLING_FILE_NOT_FOUND_ON_SERVER			= 4;
	public final static int SMS_CHECK_ERROR_IAPSILENTBILLING_FUNCT_NOT_FOUND_ON_SERVER 	= 5;
	public final static int SMS_CHECK_ERROR_GGI_NOT_SET_ON_HEADERS						= 9;
	public final static int SMS_CHECK_ERROR_ACCOUNT_NUMBER_NOT_SET_ON_HEADERS 			= 10;
	public final static int SMS_CHECK_ERROR_MSISDN_NOT_FOUND_IN_DB 						= 11;
	public final static int SMS_CHECK_PROFILE_ID_NOT_NOT_FOUND_IN_DB 					= 12;
	public final static int SMS_CHECK_MESSAGE_FAILED 									= 13;
	public final static int SMS_CHECK_ERROR_CONNECTION									= XPlayer.ERROR_CONNECTION;
	public final static int SMS_CHECK_ERROR_UNKNOWN										= XPlayer.ERROR_INIT;
	public final static int SMS_CHECK_ERROR_BAD_RESPONSE								= XPlayer.ERROR_BAD_RESPONSE;
	

	public static final java.util.Map <Integer,Integer> WapOtherErrorCodes;
	static {
		java.util.Hashtable<Integer,Integer> tmp = new java.util.Hashtable<Integer,Integer>();
		tmp.put(WAP_OTHER_ERROR_NOT_VALID_USER, -4000);
		tmp.put(WAP_OTHER_ERROR_OBB_FAILED, -4001);
		tmp.put(WAP_OTHER_ERROR_DB, -4002);
		tmp.put(WAP_OTHER_ERROR_USER_CANCEL, -4003);
		tmp.put(WAP_OTHER_ERROR_NO_PURCHASE_RECORDED, -4004);
		tmp.put(WAP_OTHER_ERROR_LIMITS_REACHED_BY_DOWNLOAD_CODE_OR_IMEI, -4005);
		tmp.put(WAP_OTHER_ERROR_PURCHASE, -4006);
		tmp.put(WAP_OTHER_ERROR_NOT_VALID_USER_PARAMETER_NOT_RIGHT, -4007);
		tmp.put(WAP_OTHER_ERROR_OTHER_ERROR, -4008);
		tmp.put(WAP_OTHER_ERROR_TRANSACTION_LIMITS, WAP_OTHER_ERROR_TRANSACTION_LIMITS);
		tmp.put(WAP_OTHER_ERROR_FRAUD_SUSP_LIMITS_EXCEDED, -4010);

		WapOtherErrorCodes = java.util.Collections.unmodifiableMap(tmp);
	}
	
	public static final java.util.Map <Integer,Integer> PhoneBillErrorCodes;
	static {
		java.util.Hashtable<Integer,Integer> tmp = new java.util.Hashtable<Integer,Integer>();
		
		tmp.put(PHONE_BILL_ERROR_INVALID_SECURITY_CODE, -4020);
		tmp.put(PHONE_BILL_ERROR_USER_ID_NOT_FOUND, -4021);
		tmp.put(PHONE_BILL_ERROR_MISSING_B_PARAMETER, -4022);
		tmp.put(PHONE_BILL_ERROR_INVALID_REQUEST, -4023);
		tmp.put(PHONE_BILL_ERROR_INVALID_PRICE_POINTS, -4024);
		tmp.put(PHONE_BILL_ERROR_PAYMENT_PROCESS_FAILED, -4025);
		tmp.put(PHONE_BILL_ERROR_MISSING_GGI, -4026);
		tmp.put(PHONE_BILL_ERROR_MISSING_USER_ONLINE_ACCOUNT_NUMBER, -4027);
		tmp.put(PHONE_BILL_ERROR_INVALID_PURCHASE_ID, -4028);
		tmp.put(PHONE_BILL_ERROR_INVALID_ONLINE_USER, -4029);
		tmp.put(PHONE_BILL_ERROR_INVALID_CONTENT_ID, -4030);
		tmp.put(PHONE_BILL_ERROR_BLOCK_AND_SET_LOCK_RENEW_TIME, -4031);
		tmp.put(PHONE_BILL_ERROR_UNKNOW_PORTAL_CODE, -4032);
		tmp.put(PHONE_BILL_ERROR_BLACKLISTER_USER, -4033);
		tmp.put(PHONE_BILL_ERROR_REACHED_HOURLY_AMOUNT_LIMIT, -4034);
		tmp.put(PHONE_BILL_ERROR_REACHED_HOURLY_SUM_LIMIT, -4035);
		tmp.put(PHONE_BILL_ERROR_REACHED_DAILY_AMOUNT_LIMIT, -4036);
		tmp.put(PHONE_BILL_ERROR_REACHED_DAYLY_SUM_LIMIT, -4037);
		tmp.put(PHONE_BILL_ERROR_REACHED_WEEKLY_AMOUNT_LIMIT, -4038);
		tmp.put(PHONE_BILL_ERROR_REACHED_WEEKLY_SUM_LIMIT, -4039);
		tmp.put(PHONE_BILL_ERROR_CONNECTION, -4040);
		tmp.put(PHONE_BILL_ERROR_UNKNOWN, -4041);
		tmp.put(PHONE_BILL_ERROR_BAD_RESPONSE, -4042);

		PhoneBillErrorCodes = java.util.Collections.unmodifiableMap(tmp);
	}
	
	public static final java.util.Map <Integer,Integer> SMSRegisterMOErrorCodes;
	static {
		java.util.Hashtable<Integer,Integer> tmp = new java.util.Hashtable<Integer,Integer>();
		
		tmp.put(SMS_REG_MO_ERROR_INVALID_IGP_CODE, -4050);
		tmp.put(SMS_REG_MO_ERROR_INVALID_GENERATED_CODE, -4051);
		tmp.put(SMS_REG_MO_ERROR_INVALID_CONTENT_ID, -4052);
		tmp.put(SMS_REG_MO_ERROR_INVALID_PRICE, -4053);
		tmp.put(SMS_REG_MO_ERROR_INVALID_MONEY, -4054);
		tmp.put(SMS_REG_MO_ERROR_INVALID_PROFILE_ID, -4055);
		tmp.put(SMS_REG_MO_ERROR_INVALID_PLATFORM_ID, -4056);
		tmp.put(SMS_REG_MO_ERROR_INVALID_LANGUAGE_ID, -4057);
		tmp.put(SMS_REG_MO_ERROR_INVALID_GGI, -4058);
		tmp.put(SMS_REG_MO_ERROR_INVALID_GAMELOFT_LIVE_ACCOUNT_NUMBER, -4059);
		tmp.put(SMS_REG_MO_ERROR_INVALID_DOWNLOAD_CODE, -4060);
		tmp.put(SMS_REG_MO_ERROR_INVALID_IMEI, -4061);
		tmp.put(SMS_REG_MO_ERROR_DUPLICATE_REQUEST_UNDER_5_MINUTES, -4062);
		tmp.put(SMS_REG_MO_ERROR_REACHED_HOURLY_AMOUNT_LIMIT, -4063);
		tmp.put(SMS_REG_MO_ERROR_REACHED_HOURLY_SUM_LIMIT, -4064);
		tmp.put(SMS_REG_MO_ERROR_REACHED_DAILY_AMOUNT_LIMIT, -4065);
		tmp.put(SMS_REG_MO_ERROR_REACHED_DAILY_SUM_LIMIT, -4066);
		tmp.put(SMS_REG_MO_ERROR_REACHED_WEEKLY_AMOUNT_LIMIT, -4067);
		tmp.put(SMS_REG_MO_ERROR_REACHED_WEEKLY_SUM_LIMIT, -4068);
		tmp.put(SMS_REG_MO_ERROR_REACHED_MONTHLY_AMOUNT_LIMIT, -4069);
		tmp.put(SMS_REG_MO_ERROR_INVALID_REACHED_MONTHLY_SUM_LIMIT, -4070);
		tmp.put(SMS_REG_MO_ERROR_CONNECTION, -4071);
		tmp.put(SMS_REG_MO_ERROR_UNKNOWN, -4072);
		tmp.put(SMS_REG_MO_ERROR_BAD_RESPONSE, -4073);

		SMSRegisterMOErrorCodes = java.util.Collections.unmodifiableMap(tmp);
	}
	
	public static final java.util.Map <Integer,Integer> SMSCheckErrorCodes;
	static {
		java.util.Hashtable<Integer,Integer> tmp = new java.util.Hashtable<Integer,Integer>();
		
		tmp.put(SMS_CHECK_ERROR_UNKNOWN_GENERIC, -4080);
		tmp.put(SMS_CHECK_ERROR_NO_SMS_INFO_FOUND_IN_DB, -4081);
		tmp.put(SMS_CHECK_ERROR_PROFILE_NOT_FOUND_IN_DB, -4082);
		tmp.put(SMS_CHECK_ERROR_BILLING_FILE_NOT_SET_IN_PROFILE, -4083);
		tmp.put(SMS_CHECK_ERROR_BILLING_FILE_NOT_FOUND_ON_SERVER, -4084);
		tmp.put(SMS_CHECK_ERROR_IAPSILENTBILLING_FUNCT_NOT_FOUND_ON_SERVER, -4085);
		tmp.put(SMS_CHECK_ERROR_GGI_NOT_SET_ON_HEADERS, -4086);
		tmp.put(SMS_CHECK_ERROR_ACCOUNT_NUMBER_NOT_SET_ON_HEADERS, -4087);
		tmp.put(SMS_CHECK_ERROR_MSISDN_NOT_FOUND_IN_DB, -4088);
		tmp.put(SMS_CHECK_PROFILE_ID_NOT_NOT_FOUND_IN_DB, -4089);
		tmp.put(SMS_CHECK_MESSAGE_FAILED, -4090);
		tmp.put(SMS_CHECK_ERROR_CONNECTION, -4091);
		tmp.put(SMS_CHECK_ERROR_UNKNOWN, -4092);
		tmp.put(SMS_CHECK_ERROR_BAD_RESPONSE, -4093);

		SMSCheckErrorCodes = java.util.Collections.unmodifiableMap(tmp);
	}
}
