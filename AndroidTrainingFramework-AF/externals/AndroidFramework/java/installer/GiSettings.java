package APP_PACKAGE.installer.utils;
import APP_PACKAGE.R;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.util.Log;

public class GiSettings {
	private SAXParserFactory spf;
	private SAXParser sp;
	private XMLReader xr;
	private GiXMLParser mXMLParser;
	private SettingsInfo mSInfo = null;
	
	/**
	 * Constructor
	 */
	public GiSettings () {
		try {
			/** Handling XML */
			spf = SAXParserFactory.newInstance();
			sp = spf.newSAXParser();
			xr = sp.getXMLReader();
			
			mXMLParser = new GiXMLParser();
			xr.setContentHandler(mXMLParser);
			
		} catch (Exception e) {
			ERR("GameInstaller","XML Server Parsing Exception = " + e);
		}
	}

	/**
	 * Used to read the settings
	 */
	public void readSettings (Context context)
	{
		try {
			/** Handling XML */
			parseXML(new InputSource(context.getResources().openRawResource(R.raw.gi_settings)));
			mSInfo = mXMLParser.getSInfo();
		} catch (Exception e) {
			ERR("GameInstaller","parseXMLFileOffline Parsing Exception = " + e);
		}
	}
	
	/**
	 * Used to read the profiles from the InputSource given.
	 * @param InputSource the current InputSource to use
	 */
	private void parseXML (InputSource isrc)
	{
		try {
			xr.parse(isrc);
		} catch (Exception e) {
			ERR("GameInstaller","parseXML Parsing Exception = " + e);
		}
	}
	
	/*
	 * @return true if this Device support PVRT textures
	 * @param manuf the manufactuer of this device
	 * @param mdl the model of this device
	*/
	public boolean useTexPVRT (String manuf, String mdl)
	{
		manuf = (manuf == null)?"":manuf;
		mdl = (mdl == null)?"":mdl;
		ArrayList<cDevice>  mDList = mSInfo.getDeviceList();
		cDevice dvc = null;
		boolean result = false;
		if (!manuf.equals("default"))
			result = useTexPVRT("default",null);
		for (int i = 0; i < mDList.size(); i++)
		{
			dvc = mDList.get(i);
			if (dvc.getManufacturer().equalsIgnoreCase(manuf) && dvc.getModel().equalsIgnoreCase(mdl))
			{
				return dvc.usePVRT();
			}
			else if  (dvc.getManufacturer().equalsIgnoreCase(manuf) && dvc.getModel().length() == 0 )
			{
				result = dvc.usePVRT();
			}
		}
		return result;
	}
		
	/*
	 * @return true if this Device support ATC textures
	 * @param manuf the manufactuer of this device
	 * @param mdl the model of this device
	*/
	public boolean useTexATC (String manuf, String mdl)
	{
		manuf = (manuf == null)?"":manuf;
		mdl = (mdl == null)?"":mdl;
		ArrayList<cDevice>  mDList = mSInfo.getDeviceList();
		cDevice dvc = null;
		boolean result = false;
		if (!manuf.equals("default"))
			result = useTexATC("default",null); 
		for (int i = 0; i < mDList.size(); i++)
		{
			dvc = mDList.get(i);
			if (dvc.getManufacturer().equalsIgnoreCase(manuf) && dvc.getModel().equalsIgnoreCase(mdl))
			{
				return dvc.useATC();
			}
			else if  (dvc.getManufacturer().equalsIgnoreCase(manuf) && dvc.getModel().length() == 0 )
			{
				result = dvc.useATC();
			}
		}
		return result;
	}
		/*
	 * @return true if this Device support ETC textures
	 * @param manuf the manufactuer of this device
	 * @param mdl the model of this device
	*/
	public boolean useTexETC (String manuf, String mdl)
	{
		manuf = (manuf == null)?"":manuf;
		mdl = (mdl == null)?"":mdl;
		ArrayList<cDevice>  mDList = mSInfo.getDeviceList();
		cDevice dvc = null;
		boolean result = false;
		if (!manuf.equals("default"))
			result = useTexETC("default",null); 
		for (int i = 0; i < mDList.size(); i++)
		{
			dvc = mDList.get(i);
			if (dvc.getManufacturer().equalsIgnoreCase(manuf) && dvc.getModel().equalsIgnoreCase(mdl))
			{
				return dvc.useETC();
			}
			else if  (dvc.getManufacturer().equalsIgnoreCase(manuf) && dvc.getModel().length() == 0 )
			{
				result = dvc.useETC();
			}
		}
		return result;
	}	
	//End
	/*
	 * @return true if this Device support PVRT textures
	 * @param manuf the manufactuer of this device
	 * @param mdl the model of this device
	*/
	public boolean useTexDXT (String manuf, String mdl)
	{
		manuf = (manuf == null)?"":manuf;
		mdl = (mdl == null)?"":mdl;
		ArrayList<cDevice>  mDList = mSInfo.getDeviceList();
		cDevice dvc = null;
		boolean result = false;
		if (!manuf.equals("default"))
			result = useTexDXT("default",null); 
		for (int i = 0; i < mDList.size(); i++)
		{
			dvc = mDList.get(i);
			if (dvc.getManufacturer().equalsIgnoreCase(manuf) && dvc.getModel().equalsIgnoreCase(mdl))
			{
				return dvc.useDXT();
			}
			else if  (dvc.getManufacturer().equalsIgnoreCase(manuf) && dvc.getModel().length() == 0 )
			{
				result = dvc.useDXT();
			}
		}
		return result;
	}

	/*
	 * @return true if the installer doesn't has to use the carrier network for the download proccess
	 * @param carrier the manufactuer of this device
	*/
	public boolean useWifiOnly (String carrier)
	{
		carrier = (carrier == null)?"":carrier;
		ArrayList<cCarrier>  mCList = mSInfo.getCarrierList();
		cCarrier crr = null;
		boolean result = false;
		if (!carrier.equals("default"))
			result = useWifiOnly("default"); 
		for (int i = 0; i < mCList.size(); i++)
		{
			crr = mCList.get(i);
			if (crr.getName().equalsIgnoreCase(carrier))
			{
				return crr.isWifiOnly();
			}
		}
		return result;
	}
}