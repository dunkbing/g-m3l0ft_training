package APP_PACKAGE.PushNotification;

import APP_PACKAGE.GLUtils.SUtils;
import android.os.Bundle;
/**
* AsyncTask
* 
*/
public abstract class AsyncTask
{
	
	private boolean mCancelled = false;
	public AsyncTask ()
	{
		mCancelled = false;
	}
	
	protected abstract Integer doInBackground(Bundle params);
	
	protected void onPreExecute() {	}
	protected void onPostExecute(Integer result) {	}
	
	protected void onCancelled() 				{	}
	public final void cancel(boolean cancel) 	{	mCancelled = cancel;	}
	public final boolean isCancelled() 			{	return mCancelled;		}
	
	
	private void finishTask(final Integer result)
	{
		SUtils.runOnUiThread(new Runnable ()
		{
			public void run ()
			{
				try
				{
					if (mCancelled) 
						onCancelled();
					else
						onPostExecute(result);
				}
				catch (Exception e)
				{
					DBG_EXCEPTION(e);
				}
			}
		});
	}
	
	public final void execute(final Bundle params) 
	{
		onPreExecute();
		new Thread( new Runnable()
		{
			public void run()
			{
				int Result = doInBackground(params);
				finishTask(Result);
			}
		}
	#if !RELEASE_VERSION
		,"Thread-doInBG"
	#endif
		).start();
			
	}
}