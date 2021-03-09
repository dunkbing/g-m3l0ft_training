#if USE_IN_APP_BILLING
package APP_PACKAGE.iab;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
/**
* AsyncTask
* 
*/
public abstract class IABAsyncTask
{
	
	private boolean mCancelled = false;
	private Context mContext = null;
	public IABAsyncTask ()
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
		if (mContext == null)
			return;
		((Activity)mContext).runOnUiThread(new Runnable ()
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
	
	public final void execute(final Bundle params, Context context) 
	{
		mContext = context;
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
		,"IABAsyncTask Thread-doInBG"
	#endif
		).start();
			
	}
}

abstract class IABCallBack
{
	public abstract void runCallBack(Bundle bundle);
}
#endif