
package APP_PACKAGE;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.widget.Toast;

import java.nio.charset.Charset;


public class BeamReceiver extends Activity
{
	public static NfcAdapter mNfcAdapter;
	private static final int MESSAGE_SENT = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		DBG("BEAM", "onCreate");
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if(mNfcAdapter == null)
			DBG("BEAM", "Could not initialize the nfc adapter");
		// Register callback to set NDEF message
		//mNfcAdapter.setNdefPushMessageCallback(this, this);//, activity);
		// Register callback to listen for message-sent success
		//mNfcAdapter.setOnNdefPushCompleteCallback(this, this);//, activity);
		
	}
	
	public  void init(Activity activity)
	{
		DBG("BEAM", "init1");
		mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
		// Register callback to set NDEF message
		DBG("BEAM", "init2");
		//mNfcAdapter.setNdefPushMessageCallback(this, activity);
		// Register callback to listen for message-sent success
		//DBG("BEAM", "init3");
		//mNfcAdapter.setOnNdefPushCompleteCallback(this, activity);
	}
	
	public void reset(Activity activity)
	{
		mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
		
		if(mNfcAdapter == null)
			return;
			
		// Register callback to set NDEF message
		DBG("BEAM", "init2");
		mNfcAdapter.setNdefPushMessageCallback(null, activity);
		// Register callback to listen for message-sent success
		DBG("BEAM", "init3");
		mNfcAdapter.setOnNdefPushCompleteCallback(null, activity);
	}

	@Override
	public void onResume()
	{
		DBG("BEAM", "onResume");
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()))
		{
			processIntent(getIntent());
		}
		else
			finish();
	}
	
	@Override
	public void onNewIntent(Intent intent)
	{
		DBG("BEAM", "onNewIntent");
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}
	
	void processIntent(Intent intent)
	{
		DBG("BEAM", "processIntent");
		android.content.pm.PackageManager pm = getPackageManager();
		final Intent i2 = pm.getLaunchIntentForPackage(STR_APP_PACKAGE);//new Intent(Intent.ACTION_MAIN, null);
		i2.addCategory(Intent.CATEGORY_LAUNCHER);
		//i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i2);
		finish();
	}
	
	public NdefRecord createMimeRecord(String mimeType, byte[] payload)
	{
		DBG("BEAM", "createMimeRecord");
		byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
		NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
		return mimeRecord;
	}
}