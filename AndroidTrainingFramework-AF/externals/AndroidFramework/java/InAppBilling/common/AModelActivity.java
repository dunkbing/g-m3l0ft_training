#if GAMELOFT_SHOP || USE_BILLING
package APP_PACKAGE.billing.common;

import APP_PACKAGE.R;

import android.app.Activity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;


public abstract class AModelActivity extends Activity{

	SET_TAG("AModelActivity");
	
	public abstract void updateBillingResult(boolean success, int message);
	public abstract void updateCCLogin(boolean success, int message);
	public abstract void updateCCUserBill(boolean success, int message);
	public abstract void updateCCNewUserBill(boolean success, int message);
	public void updateCCAddCardBill(boolean success, int message) {}
	public abstract void updateCCSingleBill(boolean success, int message);
	public abstract void updateCCForgotPassword(boolean success, int message);
	public void updateValidationResult(boolean success, int message) { }
	protected abstract boolean canGoBack();
	protected abstract void handleBackKey();
	
	public AModel mBilling = null;
	
	public AModel getModelBilling() { return mBilling; };
	protected boolean IsVisible(int index)
	{
		TextView lbl;
		try
		{
		lbl = (TextView) findViewById(index);
		return(lbl.getVisibility()== View.VISIBLE);
		}catch(Exception vex)
		{
			return false;
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		JDUMP(TAG, keyCode);
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN 
		|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
		|| keyCode == KeyEvent.KEYCODE_CAMERA)
		{
			return false;
		}
		if (keyCode == KeyEvent.KEYCODE_SEARCH)
		{}
		// if (keyCode == KeyEvent.KEYCODE_BACK)
	        // {
			// if(canGoBack())
	        	// {
	        		// handleBackKey();
	        	// }
	        // }
		
		return true;
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN 
		|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
		|| keyCode == KeyEvent.KEYCODE_SEARCH
		// || keyCode == KeyEvent.KEYCODE_BACK
		|| keyCode == KeyEvent.KEYCODE_CAMERA)
		{
			return false;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK)
	        {
			if(canGoBack())
	        	{
	        		handleBackKey();
	        	}
	        }
		return true;
	}
	
	protected void addDateValidation() 
	{
		EditText text;
		text = (EditText) findViewById(R.id.etExpirationMonth);
		text.addTextChangedListener(OnMonthChange);
		text = (EditText) findViewById(R.id.etExpirationYear);
		text.addTextChangedListener(OnYearChange);
	}
	
	    /**
     * Change the text in the TextView, with the new newText, and a new color (-1) no color will be change 
     * @param id [The id of the TextView in the layout]
     * @param newText [The id of the new text from the R class]
     * @param color [The new color]
     * @see #setTextViewNewText(int, String, int)
     */
    protected void setTextViewNewText(int id, int newText, int color)
    {
    	setTextViewNewText (id, getString(newText), color);
    }
    /**
     * Change the text in the TextView, with the new newText, and a new color (-1) no color will be change 
     * @param id [The id of the TextView in the layout]
     * @param newText [The new String text]
     * @param color [The new color]
     * @see #setTextViewNewText(int, int, int)
     */
    private void setTextViewNewText(final int id, final String newText, final int color)
    {
    	this.runOnUiThread(new Runnable () 
    	{
	    	public void run () 
	    	{	
	    		TextView lbl;
				lbl = (TextView) findViewById(id);
				if (lbl != null)
					lbl.setText(newText);
				if (color != -1)
					lbl.setTextColor(color);
	    	}
    	});
    }
    /**
     * Change the visibility status of the ProgressBar 
     * @param id [The id of the ProgressBar in the layout]
     * @param visible
     */
    protected void setProgressBarVisibility(final int id, final int visible)
    {
    	this.runOnUiThread(new Runnable () 
    	{
	    	public void run () 
	    	{	
	    		ProgressBar pg;
				pg = (ProgressBar) findViewById(id);
				pg.setVisibility(visible);
	    	}
    	});
    }
    /**
     /**
     * Change the visibility status of the Button 
     * @param id [The id of the ProgressBar in the layout]
     * @param visible
     */
    protected void setButtonVisibility(final int id, final int visible)
    {
    	this.runOnUiThread(new Runnable () 
    	{
	    	public void run () 
	    	{	
	    		Button bt;
	    		bt = (Button) findViewById(id);
	    		bt.setVisibility(visible);
	    	}
    	});
    }
    
	
	/** The list that contains all the buttons of the current view */
    private ArrayList<Button>       buttonList;
	
	/**
     * Set the current button list
     * @param buttonList
     */
    protected void setButtonList(ArrayList<Button> buttonList)
    {
        this.buttonList = buttonList;
    }

    /**
     * returns the current button list
     * @return
     */
    protected ArrayList<Button> getButtonList()
    {
        return buttonList;
    }
	
	protected static final String md5(final String s)
	{
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
	 
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();
	 
		} catch (NoSuchAlgorithmException e) {
			DBG_EXCEPTION(e);
		}
		return "";
	}
	
	protected TextWatcher OnMonthChange = new TextWatcher()
    {
	 	public void afterTextChanged (Editable s)
		{
			if (s.length() > 0)
			{		
				int month = Integer.parseInt(s.toString());
				if (month > 1 && s.length() == 1)
				{	
					s.replace(0, 1, "0"+s);
				}
				if ((month > 0  && month <= 12) || (month == 0 && s.length() == 1))
					return;
				s.clear();
			}
		}
		public void beforeTextChanged (CharSequence s, int	start, int count, int after)
		{
			// do nothing
		}
		public void onTextChanged (CharSequence s, int start, int before, int count) 
		{
			// do nothing
		}
    };
    
	protected TextWatcher OnYearChange = new TextWatcher()
    {
	 	public void afterTextChanged (Editable s)
		{
	 		Date date = new Date();
	 		int cyear = date.getYear() + 1900;
	 		if (s.length() > 0)
	 		{
		 		int year = Integer.parseInt(s.toString());
		 		if (s.length() == 4 
		 			&& year >= cyear 
		 			// && year - cyear < 10
		 			|| s.length() < 4 )
		 			return;
		 		s.clear();
	 		}
		}
		public void beforeTextChanged (CharSequence s, int	start, int count, int after)
		{
			// do nothing
		}
		public void onTextChanged (CharSequence s, int start, int before, int count) 
		{
			// do nothing
		}
    };
	
	protected String getStringFormated(int index, String rx, String rp)
	{
		return getStringFormated(getString(index), rx, rp);
	}
	
	protected String getStringFormated(String src, String rx, String rp)
	{
		String result = src;
		if (rx == null)
		{
			rx = "{SIZE}";
		}
		
		if (rx.equals("{PRICE}"))
			result = result.replace(rx, rp);
		else
			result = result.replace(rx, rp);
		
		return result;
	}
	
	private InputStream mIS;
	private byte[] getRawResource(int id)
	{
		try
		{
			mIS = getResources().openRawResource(id);
			int resLength = mIS.available();
			byte[]temp = new byte[resLength];
			mIS.read(temp, 0, resLength);
			mIS.close();
			mIS = null;
			return temp;
		}
		catch (Exception e) {
			DBG_EXCEPTION(e);
			return null;
		}
	}
	
	//Create New Account
	protected UserInfo mUserInfo;

	/**
	 * @return the mUserInfo
	 */
	public UserInfo getUserInfo() {
		return mUserInfo;
	}

	/**
	 * @param mUserInfo the mUserInfo to set
	 */
	public void setUserInfo(UserInfo mUserInfo) {
		this.mUserInfo = mUserInfo;
	}
	
	protected void ResetFormData()
	{
		//Create New Account
		mUserInfo = new UserInfo();		
	}
	
}
#endif //#if GAMELOFT_SHOP || USE_BILLING