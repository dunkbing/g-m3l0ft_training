package APP_PACKAGE.installer.utils;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

class cDevice {

	public cDevice()
	{ }
	
	private boolean isPVRT_tex = false;
	private boolean isATC_tex = false;
	private boolean isETC_tex = false;
	private boolean isDXT_tex = false;
	
	private String manufacturer = "";
	private String model = "";
	
	
	/**
	 * @return true if the PVRT texture is supported
	 */
	public boolean usePVRT() {
		return isPVRT_tex;
	}
	
	/**
	 * @param value to set
	 */
	public void setPVRT(boolean value) {
		isPVRT_tex = value;
	}

	/**
	 * @return true if the ATC texture is supported
	 */
	public boolean useATC() {
		return isATC_tex;
	}
	
	/**
	 * @return true if the ATC texture is supported
	 */
	public boolean useETC() {
		return isETC_tex;
	}

	/**
	 * @param value to set
	 */
	public void setATC(boolean value) {
		isATC_tex = value;
	}
	
	/**
	 * @param value to set
	 */
	public void setETC(boolean value) {
		isETC_tex = value;
	}

		/**
	 * @return true if the PVRT texture is supported
	 */
	public boolean useDXT() {
		return isDXT_tex;
	}
	
	/**
	 * @param value to set
	 */
	public void setDXT(boolean value) {
		isDXT_tex = value;
	}
	
	/**
	 * @return the manufacturer
	 */
	public String getManufacturer() {
		return manufacturer;
	}
	
	/**
	 * @param value the name to set
	 */
	public void setManufacturer(String value) {
		manufacturer = value;
	}
	
	/**
	 * @return the model
	 */
	public String getModel() {
		return model;
	}
	
	/**
	 * @param value the name to set
	 */
	public void setModel(String value) {
		model = value;
	}
}

class cCarrier {

	private boolean wifi_only = true;
	private String name = "";
	
	/**
	 * @return true if the installer doesn't has to use the carrier network for the download proccess
	 */
	public boolean isWifiOnly() {
		return wifi_only;
	}
	
	/**
	 * @param value to set
	 */
	public void setWifiOnly(boolean value) {
		wifi_only = value;
	}
	
		/**
	 * @return the carrier name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param value the name to set
	 */
	public void setName(String value) {
		name = value;
	}
}

class SettingsInfo 
{
	
	private ArrayList<cDevice>  mDeviceList = null;
	private ArrayList<cCarrier> mCarrierList = null;
		
	
	/**
	 * @return the device List
	 */
	public ArrayList<cDevice> getDeviceList() {
		return mDeviceList;
	}

	/**
	 * @param deviceList the profileList to set
	 */
	public void setDeviceList(ArrayList<cDevice> list) {
		this.mDeviceList = list;
	}
	
	/**
	 * @return the carrier List
	 */
	public ArrayList<cCarrier> getCarrierList() {
		return mCarrierList;
	}

	/**
	 * @param list the profileList to set
	 */
	public void setCarrierList(ArrayList<cCarrier> list) {
		this.mCarrierList = list;
	}
}


public class GiXMLParser extends DefaultHandler
{

	boolean currentElement = false;
	boolean readingLanguages = false;
	String currentValue = null;
	
	public cDevice mDevice = null;
	public cCarrier mCarrier = null;
	
	private SettingsInfo mSInfo = null;
	
	private boolean addingDevices = false;
	private boolean addingCarriers = false;

	/**
	 * @return the msInfo
	 */
	public SettingsInfo getSInfo() 
	{
		return mSInfo;
	}
	
	@Override
	public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException 
	{
		if (currentElement)
			currentValue = "";
		currentElement = true;
		if (localName.equals("settings"))
		{
			mSInfo = new SettingsInfo();
		} 
		else if (localName.equals("carriers"))
		{
			addingCarriers = true;
			
			if (mSInfo.getCarrierList() == null)
				mSInfo.setCarrierList(new ArrayList<cCarrier>());
		} 
		else if (localName.equals("devices"))
		{
			addingDevices = true;
			
			if (mSInfo.getDeviceList() == null)
				mSInfo.setDeviceList(new ArrayList<cDevice>());
		} 
		else if (addingCarriers)
		{
			if (localName.equals("carrier")) 
			{
				mCarrier = new cCarrier();
				String attr = attributes.getValue("name");
				mCarrier.setName(attr);
			}
		}
		else if (addingDevices)
		{
			if (localName.equals("device"))
			{
				mDevice = new cDevice();
			}
			else if (localName.equals("manufacturer")) 
			{
				String attr = attributes.getValue("name");
				mDevice.setManufacturer(attr);
			}
			if (localName.equals("model")) 
			{
				String attr = attributes.getValue("name");
				mDevice.setModel(attr);
			}
		}
		
	}
	
	@Override
	public void endElement (String uri, String localName, String qName) 	throws SAXException 
	{
		currentElement = false;
		currentValue = currentValue.trim();
		if (addingCarriers)
		{
			if (localName.equals("carrier")) 
			{
				mSInfo.getCarrierList().add(mCarrier);
			} 
			else if (localName.equals("wifi_only"))
			{
				mCarrier.setWifiOnly((Integer.parseInt(currentValue)==1));
			}
			else if (localName.equals("carriers"))
			{
				addingCarriers = false;
			}
		}
		else if (addingDevices)
		{
			if (localName.equals("device")) 
			{
				mSInfo.getDeviceList().add(mDevice);
			} 
			else if (localName.equals("pvrt_textures"))
			{
				mDevice.setPVRT((Integer.parseInt(currentValue)==1));
			}
			else if (localName.equals("atc_textures"))
			{
				mDevice.setATC((Integer.parseInt(currentValue)==1));
			}
			else if (localName.equals("etc_textures"))
			{
				mDevice.setETC((Integer.parseInt(currentValue)==1));
			}
			else if (localName.equals("dxt_textures"))
			{
				mDevice.setDXT((Integer.parseInt(currentValue)==1));
			}
			else if (localName.equals("devices"))
			{
				addingDevices = false;
			} 
		}
		
		currentValue = "";
	}


	@Override
	public void characters (char[] ch, int start, int length) throws SAXException 
	{
		if (currentElement)
		{
			String value = new String(ch, start, length);
			if (value.equals("\n"))
				value = "";
			currentValue += value;
		}
	}
}
