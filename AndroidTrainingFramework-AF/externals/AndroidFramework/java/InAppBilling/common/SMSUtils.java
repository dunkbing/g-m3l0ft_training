#if GAMELOFT_SHOP || USE_BILLING
package APP_PACKAGE.billing.common;

public class SMSUtils {
	
	SET_TAG("InAppBilling");
	public static final String 		SMS_TITLE_MESSAGE				=	"GameloftOrder";
	public static final String		SMS_ENCRYPTED_HEADER			=	"100000";
	public static final String		SMS_ENCRYPTED_HEADER_UNLOCK		=	"00";
	public static final String		SMS_ENCRYPTED_HEADER_INAPP		=	"01";
	//	binary operations
	public static final String		ENCRYPTION_GAMECODE			=	"000000";
	public static final String		ENCRYPTION_RANDOMCODE		=	"000001";
	public static final String		ENCRYPTION_PLATFORMID		=	"000010";
	public static final String		ENCRYPTION_PROFILEID		=	"000011";
	public static final String		ENCRYPTION_LANGUAGE			=	"000100";
	public static final String		ENCRYPTION_REQUESTTYPE		=	"000101";
	public static final String		ENCRYPTION_IMEI				=	"000110";
	public static final String		ENCRYPTION_DOWNLOADCODE		=	"000111";
	public static final String		ENCRYPTION_FMSMSID			=	"001000";
	public static final String		ENCRYPTION_ORIGINALIGP		=	"001001";
	public static final String		ENCRYPTION_SCREEN			=	"001010";
	public static final String		ENCRYPTION_GAMEID			=	"001011";
	public static final String		ENCRYPTION_OPERATIONID		=	"001100";
	public static final String		ENCRYPTION_GAMEORDER		=	"001101";
	public static final String		ENCRYPTION_CONTENTAMOUNT	=	"001110";
	public static final String		ENCRYPTION_SMSMOCOUNTER		=	"001111";
	public static final String		ENCRYPTION_CONTENTID		=	"010000";
	public static final String		ENCRYPTION_MEID				=	"010001";
	public static final String		ENCRYPTION_IMEI_VR			=	"010010";
	public static final String		ENCRYPTION_MEID_VR			=	"010011";
	public static final String		ENCRYPTION_MEID_36_VR		=	"010100";
	
	public static final int			GAMECODE_LENGTH				=	21;
	public static final int			RANDOMCODE_LENGTH			=	14;
	public static final int			PLATFORMID_LENGTH			=	15;
	public static final int			PROFILEID_LENGTH			=	14;
	public static final int			LANGUAGE_LENGTH				=	11;
	public static final int			REQUESTTYPE_LENGTH			=	6;
	public static final int			IMEI_LENGTH					=	50;
	public static final int			DOWNLOADCODE_LENGTH			=	57;
	public static final int			FMSMSID_LENGTH				=	32;
	public static final int			ORIGINALIGP_LENGTH			=	21;
	public static final int			SCREEN_LENGTH				=	3;
	public static final int			GAMEID_LENGTH				=	32;
	public static final int			OPERATIONID_LENGTH			=	15;
	public static final int			GAMEORDER_LENGTH			=	5;
	public static final int			CONTENTAMOUNT_LENGTH		=	20;
	public static final int			SMSMOCOUNTER_LENGTH			=	12;
	public static final int			CONTENTID_LENGTH			=	16;
	public static final int			MEID_LENGTH					=	56;
	public static 		int			IMEI_VR_LENGTH				=	0;//variable length
	public static 		int			MEID_VR_LENGTH				=	0;//variable length
	public static 		int			MEID_36_VR_LENGTH			=	0;//variable length
	
    public static byte[] BinaryStringToByteArray(String input)
    {
        int length = input.length()/8;
        byte[] result = new byte[length];

        int idx = 0;
        for(int i = 0; i < length; i++)
        {
            DBG("Billing", "Input byte:        " + input.substring(idx*8, idx*8 + 8));
            // result[idx] = Byte.parseByte(input.substring(idx*8, idx*8 + 8), 2);
            result[idx] = (byte)(Integer.parseInt(input.substring(idx*8, idx*8 + 8), 2) & 0xFF);
            // result[idx] = Byte.valueOf(input.substring(idx*8, idx*8 + 8), 2);
            idx ++;
        }

        return result;
    }
	
	public static String addPaddingToByte(String data, int size)
	{
		StringBuilder tmp = new StringBuilder(data);
		int count = 0;
		while (data.length() + count < size)
		{
			tmp.insert(0,"0");
			count++;
		}
		return tmp.toString();
	}
	
	public static boolean isInteger(String str) {
	    int size = str.length();

	    for (int i = 0; i < size; i++) {
	        if (!Character.isDigit(str.charAt(i))) {
	            return false;
	        }
	    }
	    return size > 0;
	}
	
	public static boolean isHexadecimal(String str)
	{
		if (str.matches("^[\\da-fA-F]+$"))
		{
			return true;
		}
		return false;
	}
	
	public static String getIMEIType(String IMEI)
	{
		if (IMEI == null || IMEI.length() <= 0)
			return null;
		
		if (isInteger(IMEI))
		{
			if (IMEI.length() == 15)
			{
				DBG(TAG, "IMEI_15");
				return ENCRYPTION_IMEI;
			}else
			{
				DBG(TAG, "IMEI_VR");
				return ENCRYPTION_IMEI_VR;
			}
		}else if(isHexadecimal(IMEI))
		{
			if (IMEI.length() == 14)
			{
				DBG(TAG, "MEID_14");
				return ENCRYPTION_MEID;
			}else
			{
				DBG(TAG, "MEID_VR");
				return ENCRYPTION_MEID_VR;
			}
		}else
		{
			//Alphanumeric
			DBG(TAG, "MEID_36_VR");
			return ENCRYPTION_MEID_36_VR;
		}
	}
	
	public static String generateBinaryString(final String operation, final String data)
	{
		StringBuilder binaryData = new StringBuilder();
		int data_length=0;
		String tmp;
		String dbg_operation="";
		
		try{
			if (operation == ENCRYPTION_GAMECODE) {
				dbg_operation = "IGP_CODE";
				tmp = Long.toBinaryString(Long.parseLong(data, 36));
				binaryData.append(tmp);
				data_length = GAMECODE_LENGTH;
			}
			else if (operation == ENCRYPTION_RANDOMCODE) {
				dbg_operation = "GENERATED_CODE";
				tmp = Integer.toBinaryString(Integer.parseInt(data));
				DBG(TAG, "tmp="+tmp);
				binaryData.append(tmp);
				data_length = RANDOMCODE_LENGTH;
			}
			else if (operation == ENCRYPTION_PLATFORMID) {
				dbg_operation = "PLATFORM_ID";
				tmp = Integer.toBinaryString(Integer.parseInt(data));
				binaryData.append(tmp);
				data_length = PLATFORMID_LENGTH;
			}
			else if (operation == ENCRYPTION_PROFILEID) {
				dbg_operation = "PROFILE_ID";
				tmp = Integer.toBinaryString(Integer.parseInt(data));
				binaryData.append(tmp);
				data_length = PROFILEID_LENGTH;
			}
			else if (operation == ENCRYPTION_LANGUAGE) {
				dbg_operation = "LANGUAGE_ID";
				tmp = Long.toBinaryString(Long.parseLong(data, 36));
				binaryData.append(tmp);
				data_length = LANGUAGE_LENGTH;
			}
			else if (operation == ENCRYPTION_REQUESTTYPE) {
				dbg_operation = "TYPE";
				tmp = Integer.toBinaryString(Integer.parseInt(data));
				binaryData.append(tmp);
				data_length = REQUESTTYPE_LENGTH;
			}
			else if (operation == ENCRYPTION_IMEI) {
				dbg_operation = "IMEI";
				tmp = Long.toBinaryString(Long.parseLong(data));
				binaryData.append(tmp);
				data_length = IMEI_LENGTH;
			}
			else if (operation == ENCRYPTION_MEID) {
				dbg_operation = "MEID";
				tmp = Long.toBinaryString(Long.parseLong(data, 16));
				binaryData.append(tmp);
				data_length = MEID_LENGTH;
			}
			else if (operation == ENCRYPTION_IMEI_VR) {
				dbg_operation = "IMEI_VR";
				String binary = Long.toBinaryString(Long.parseLong(data));
				String digits = SMSUtils.addPaddingToByte(Integer.toBinaryString(data.length()), 6);
				int bytes_to_read = binary.length();
				String sBytesRead = SMSUtils.addPaddingToByte(Integer.toBinaryString(bytes_to_read), 7);
				tmp = sBytesRead + digits + binary;
				binaryData.append(tmp);
				IMEI_VR_LENGTH = sBytesRead.length() + digits.length() + bytes_to_read;
				data_length = IMEI_VR_LENGTH;
			}
			else if (operation == ENCRYPTION_MEID_VR) {
				dbg_operation = "MEID_VR";
				String binary = Long.toBinaryString(Long.parseLong(data,16));
				String digits = SMSUtils.addPaddingToByte(Integer.toBinaryString(data.length()), 6);
				int bytes_to_read = binary.length();
				String sBytesRead = SMSUtils.addPaddingToByte(Integer.toBinaryString(bytes_to_read), 7);
				tmp = sBytesRead + digits + binary;
				binaryData.append(tmp);
				MEID_VR_LENGTH = sBytesRead.length() + digits.length() + bytes_to_read;
				data_length = MEID_VR_LENGTH;
			}
			else if (operation == ENCRYPTION_MEID_36_VR) {
				dbg_operation = "MEID_36_VR";
				String binary = new java.math.BigInteger(data, 36).toString(2);
				String digits = SMSUtils.addPaddingToByte(Integer.toBinaryString(data.length()), 6);
				int bytes_to_read = binary.length();
				String sBytesRead = SMSUtils.addPaddingToByte(Integer.toBinaryString(bytes_to_read), 7);
				tmp = sBytesRead + digits + binary;
				binaryData.append(tmp);
				MEID_VR_LENGTH = sBytesRead.length() + digits.length() + bytes_to_read;
				data_length = MEID_VR_LENGTH;
			}
			else if (operation == ENCRYPTION_DOWNLOADCODE) {
				dbg_operation = "DOWNLOAD_CODE";
				tmp = Long.toBinaryString(Long.parseLong(data, 36));
				binaryData.append(tmp);
				data_length = DOWNLOADCODE_LENGTH;
			}
			else if (operation == ENCRYPTION_FMSMSID) {
				dbg_operation = "FM_SMS_ID";
				tmp = Integer.toBinaryString(Integer.parseInt(data));
				binaryData.append(tmp);
				data_length = FMSMSID_LENGTH;
			}
			else if (operation == ENCRYPTION_ORIGINALIGP) {
				dbg_operation = "ORIGINAL_IGP";
				tmp = Long.toBinaryString(Long.parseLong(data, 36));
				binaryData.append(tmp);
				data_length = ORIGINALIGP_LENGTH;
			}
			else if (operation == ENCRYPTION_SCREEN) {
				dbg_operation = "SCREEN";
				tmp = Integer.toBinaryString(Integer.parseInt(data));
				binaryData.append(tmp);
				data_length = SCREEN_LENGTH;
			}
			else if (operation == ENCRYPTION_GAMEID) {
				dbg_operation = "GAME_ID";
				tmp = Long.toBinaryString(Long.parseLong(data));
				binaryData.append(tmp);
				data_length = GAMEID_LENGTH;
			}
			else if (operation == ENCRYPTION_OPERATIONID) {
				dbg_operation = "OPERATION_ID";
				tmp = Long.toBinaryString(Long.parseLong(data));
				binaryData.append(tmp);
				data_length = OPERATIONID_LENGTH;
			}
			else if (operation == ENCRYPTION_CONTENTID) {
				dbg_operation = "CONTENT_ID";
				tmp = Long.toBinaryString(Long.parseLong(data));
				binaryData.append(tmp);
				data_length = CONTENTID_LENGTH;
			}else
			{
				WARN(TAG, "Operation not found "+operation);
			}
		
		}catch(Exception e){
			ERR(TAG, "Error with operation "+dbg_operation+" data="+data +" binary ");
		}
		
		//	Add padding 0 to left
		int count = data_length - binaryData.length();
		if (binaryData.length() < data_length)
		{
			DBG(TAG, "binaryData "+binaryData.length()+" - "+data_length);
			for (int x=0;x<count;x++)
				binaryData.insert(0,"0");
		}
		
		if (binaryData.length()!=data_length)
			WARN(TAG, dbg_operation + "length are not equal");
		
		binaryData.insert(0,operation);
		
		INFO(TAG, "["+dbg_operation+"|"+data_length+"|"+count+"] : "+data);
		INFO(TAG, binaryData.toString());
		return binaryData.toString();
	}

}
#endif