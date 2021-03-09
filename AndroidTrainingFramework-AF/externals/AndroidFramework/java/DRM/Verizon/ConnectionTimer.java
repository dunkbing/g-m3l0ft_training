package APP_PACKAGE.DRM.Verizon;

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
	    HTTP http = XPlayer.whttp;
	    http.cancel();
	    http.m_bError = true;
	    http.m_bInProgress = false;
	    http = null;		
	}
}

