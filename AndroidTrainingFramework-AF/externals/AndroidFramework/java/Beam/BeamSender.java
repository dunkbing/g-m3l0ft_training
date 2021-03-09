
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


public class BeamSender implements CreateNdefMessageCallback, OnNdefPushCompleteCallback
{
	public static NfcAdapter mNfcAdapter;
	private static final int MESSAGE_SENT = 1;
	
	public  void init(Activity activity)
	{
		DBG("BEAM", "init1");
		mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
		
		if(mNfcAdapter == null)
		{
			DBG("BEAM", "Could not initialize the nfc adapter");
			return;
		}
		// Register callback to set NDEF message
		DBG("BEAM", "init2");
		mNfcAdapter.setNdefPushMessageCallback(this, activity);
		// Register callback to listen for message-sent success
		DBG("BEAM", "init3");
		mNfcAdapter.setOnNdefPushCompleteCallback(this, activity);
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
	public NdefMessage createNdefMessage(NfcEvent event)
	{
		DBG("BEAM", "createNdefMessage");
		String market_code = PORTAL_CODE;
		if(PORTAL_CODE.equals("google_market"))
			market_code = "ANMP";
		else
		if(PORTAL_CODE.equals("kr_skt"))
			market_code = "SKTS";
		else
		if(PORTAL_CODE.equals("kr_kt"))
			market_code = "KTOH";
		else
		if(PORTAL_CODE.equals("us_verizon"))
			market_code = "VRHD";
		else
		if(PORTAL_CODE.equals("amazon"))
			market_code = "AMAZ";
		else
		if(PORTAL_CODE.equals("samsung_a_store"))
			market_code = "SAST";
		else
		if(PORTAL_CODE.equals("sprint_wapshop"))
			market_code = "SPHD";
		else
			market_code = "FVGL";

		DBG("BEAM", "app: "+BEAM_APLICATION);		
		NdefMessage msg = new NdefMessage( new NdefRecord[] { 
	#if USE_MARKET_INSTALLER
			//NdefRecord.createApplicationRecord(BEAM_APLICATION),//add this to automayicly open the google shop
	#endif
			createMimeRecord(BEAM_APLICATION, "some text".getBytes())
			#if !USE_MARKET_INSTALLER
			,
			NdefRecord.createUri("http://ingameads.gameloft.com/redir/?from="+GGC_GAME_CODE+"&game="+GGC_GAME_CODE+"&op="+market_code)///search?q=" + STR_APP_PACKAGE),
			#else
			,
			NdefRecord.createUri("http://ingameads.gameloft.com/redir/?from="+GGC_GAME_CODE+"&game="+GGC_GAME_CODE+"&op=ANMP")
			#endif
		});
		return msg;
	}
	
	@Override
	public void onNdefPushComplete(NfcEvent arg0)
	{
		DBG("BEAM", "onNdefPushComplete");
		//android.widget.Toast toast = android.widget.Toast.makeText(getApplicationContext(), "NFC sent!", android.widget.Toast.LENGTH_LONG);
		//toast.show();
	}

	
	public NdefRecord createMimeRecord(String mimeType, byte[] payload)
	{
		DBG("BEAM", "createMimeRecord");
		byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
		NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
		return mimeRecord;
	}
}