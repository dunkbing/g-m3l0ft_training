#if USE_BILLING || USE_INSTALLER || USE_TRACKING_FEATURE_INSTALLER || USE_IN_APP_BILLING || USE_PSS
package APP_PACKAGE.GLUtils;

import java.util.ArrayList;

public class Carrier {
	
    ///Proxy Support
	private boolean http_use_proxy;
	private String http_proxy_server;
	private int http_proxy_port;
	
	private String Name = null;
	/**
	 * @return the name
	 */
	public String getName() {
		return Name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		Name = name;
	}


	/**
	 * @return the mnc
	 */
	public ArrayList<String> getMnc() {
		return mnc;
	}


	/**
	 * @param mnc the mnc to set
	 */
	public void setMnc(ArrayList<String> mnc) {
		this.mnc = mnc;
	}
	private ArrayList<String> mnc = new ArrayList<String>();
    
	public Carrier()
	{
		http_use_proxy		= false;
		http_proxy_server 	= "";
		http_proxy_port		= 0;
	}
	
	public boolean useProxy()		{		return http_use_proxy;			}
	public String getProxyServer()	{		return http_proxy_server;		}
	public int getProxyPort()		{		return http_proxy_port;			}
}

#endif //#if USE_BILLING || USE_INSTALLER || USE_TRACKING_FEATURE_INSTALLER
