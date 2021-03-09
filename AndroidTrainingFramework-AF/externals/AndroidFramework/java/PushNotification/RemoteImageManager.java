#if SIMPLIFIED_PN
package APP_PACKAGE.PushNotification;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

//////////////////////////////////////////////////////////////
// RemoteImageManager
//////////////////////////////////////////////////////////////	

public class RemoteImageManager
{
	SET_TAG("PushNotification");
	
	private static Bitmap remoteImage 		= null;
	
	public static Bitmap GetAsset() 
	{
		INFO(TAG, "[GetAsset]");
		return remoteImage;
	}
	
	public static boolean GetLocalAsset(Context context)
	{
		INFO(TAG, "[GetLocalAsset]");
		
		int resId = 0;
		remoteImage = null;
		
		try 
		{	
			///////GET LOCAL IMAGES FROM RAW FOLDER
			resId = context.getResources().getIdentifier(PN_UTILS_CLASS.CustomImageName, "raw", context.getPackageName());
			remoteImage = BitmapFactory.decodeResource(context.getResources(), resId);
			//////////////////
		}
		catch (Exception e){remoteImage = null; DBG_EXCEPTION(e);}
		
		return (remoteImage != null && resId > 0);
	}
}
#endif
