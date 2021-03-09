package APP_PACKAGE.iab;

import android.app.Dialog;
import android.content.Context;
//import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.os.Bundle;
import android.util.Log;
import java.lang.reflect.Method;
import android.widget.TextView;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;


import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;

public class IABDialog extends Dialog{
	
	SET_TAG("InAppBilling");
	
	public static int GLD_SINGLE_BUTTON	= 0;
	public static int GLD_DOUBLE_BUTTON	= 1;
	public static int GLD_WAIT_MESSAGE	= 2;
	
	
	public final static int BUTTON_NEGATIVE	= 0;
	public final static int BUTTON_NEUTRAL	= 1;
	public final static int BUTTON_POSITIVE	= 2;
	private Button mButtons[] = new Button[3];
	
	private ProgressBar mBar = null;
	
	private TextView mTittle 	= null;
	private TextView mMessage 	= null;
	
	public int mType = -1;
		
	//private IABDialog mThis = null;
	
	public IABDialog(Context context, int type, int messageId)
	{
		super(context,R.style.Theme_IAB20Theme);
		CreateIABDialog(context, type, messageId, -1);
	}
	public IABDialog(Context context, int type, int messageId, int tittleId)
	{
		super(context,R.style.Theme_IAB20Theme);
		CreateIABDialog(context, type, messageId, tittleId);
	}
	public void CreateIABDialog(Context context, int type, int messageId, int tittleId)
	{
		mType = type;
			
		setContentView(R.layout.iab20_gldialogs);
		
		mMessage 	= (TextView)findViewById(R.id.tvInfo);
		mTittle 	= (TextView)findViewById(R.id.tvHeader);
		
		if (messageId > 0)
			mMessage.setText(messageId);
		if (tittleId > 0)
			mTittle.setText(tittleId);	
			
		mButtons[BUTTON_NEGATIVE] = (Button) findViewById(R.id.btId1);
		mButtons[BUTTON_NEUTRAL] = (Button) findViewById(R.id.btId2);
		mButtons[BUTTON_POSITIVE] = (Button) findViewById(R.id.btId3);
		mBar = (ProgressBar)findViewById(R.id.pbBarDialog);
		
		setCancelable(false);
		
		if (type == GLD_SINGLE_BUTTON)
		{
			mButtons[BUTTON_NEUTRAL].setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							DBG(TAG,"Default OnClickListener");
							dismiss();
						}
					});;
		}
		else if(type == GLD_DOUBLE_BUTTON)
		{
			mButtons[BUTTON_NEGATIVE].setVisibility(View.VISIBLE);
			mButtons[BUTTON_NEGATIVE].setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							DBG(TAG,"Default OnClickListener");
							dismiss();
						}
					});;
			
			mButtons[BUTTON_NEUTRAL].setVisibility(View.INVISIBLE);
			mButtons[BUTTON_POSITIVE].setVisibility(View.VISIBLE);
		}
		else if(type == GLD_WAIT_MESSAGE)
		{
			
			mButtons[BUTTON_NEUTRAL].setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							DBG(TAG,"Default OnClickListener");
							dismiss();
						}
					});;
			mBar.setVisibility(View.VISIBLE);
		}

		android.view.ViewGroup.LayoutParams params = getWindow().getAttributes();
		params.height = android.view.ViewGroup.LayoutParams.FILL_PARENT; 
		params.width = android.view.ViewGroup.LayoutParams.FILL_PARENT; 
		getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
	}
	
	public static IABDialog createDialog(Context context, int type, int messageId)
	{
		return new IABDialog(context, type, messageId);
	}
	public static IABDialog createDialog(Context context, int messageId)
	{
		return new IABDialog(context, GLD_SINGLE_BUTTON, messageId);
	}
	public IABDialog setButton(int whichButton, int text, View.OnClickListener listener)
	{
		if (whichButton > BUTTON_POSITIVE || whichButton < BUTTON_NEGATIVE)
		{
			ERR(TAG,"No button Added");
			return this;
		}
		if (text > 0)
			mButtons[whichButton].setText(text);
		mButtons[whichButton].setOnClickListener(listener);
		return this;
	}
	public IABDialog setButtonVisibility(int whichButton, int visibility)
	{
		if (whichButton > BUTTON_POSITIVE || whichButton < BUTTON_NEGATIVE)
		{
			ERR(TAG,"No button updated");
			return this;
		}
		mButtons[whichButton].setVisibility(visibility);

		if (mButtons[BUTTON_NEUTRAL].getVisibility() == View.INVISIBLE &&
			mButtons[BUTTON_NEGATIVE].getVisibility() == View.INVISIBLE &&
			mButtons[BUTTON_POSITIVE].getVisibility() == View.INVISIBLE)
		{
			findViewById(R.id.llyBotton).setVisibility(View.GONE);
		}
		else
		{
			findViewById(R.id.llyBotton).setVisibility(View.VISIBLE);
		}
		return this;
	}
	public Button getButton(int whichButton)
	{
		if (whichButton > BUTTON_POSITIVE || whichButton < BUTTON_NEGATIVE)
		{
			ERR(TAG,"No button returned");
			return null;
		}
		return mButtons[whichButton];
	}
	public IABDialog setMessage(int messageId)
	{
		if (mMessage != null)
			mMessage.setText(messageId);
		return this;
	}
	public IABDialog setMessage(String message)
	{
		if (mMessage != null)
			mMessage.setText(message);
		return this;
	}
	public IABDialog setTittle(int titleId)
	{
		if (mTittle != null)
			mTittle.setText(titleId);
		return this;
	}	
	public IABDialog setTittle(String title)
	{
		if (mTittle != null)
			mTittle.setText(title);
		return this;
	}
	public IABDialog setProgressBarVisibility(int visibility)
	{
		mBar.setVisibility(visibility);
		return this;
	}
}
