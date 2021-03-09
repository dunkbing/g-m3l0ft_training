#if USE_INSTALLER || USE_BILLING || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
package APP_PACKAGE.GLUtils;

import java.util.Timer;
import java.util.TimerTask;

public class ConnectionTimer extends TimerTask
{
    private static Timer timer;
    
	public static void start(long timeToTask)
	{
	    timer = new Timer();
		timer.schedule(new ConnectionTimer(), timeToTask);				
	}
	
	public static void stop() {
	    timer.cancel();
	}
	
	public void run()
	{
	
	    HTTP http = XPlayer.getWHTTP();
	    http.cancel();
	    http.m_bError = true;
	    http.m_bInProgress = false;
	    http = null;
	}
}
#endif //#if USE_INSTALLER || USE_BILLING
