#if USE_VIRTUAL_KEYBOARD
package APP_PACKAGE.GLUtils;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import java.lang.ref.WeakReference;
import android.app.Activity;
import APP_PACKAGE.PackageUtils.VirtualKeyboardPlugin;
import APP_PACKAGE.PackageUtils.JNIBridge;

#if USE_LOW_PROFILE_MENU
import APP_PACKAGE.GLUtils.LowProfileListener;
#endif

public class VirtualKeyboard extends EditText implements OnFocusChangeListener
{
	SET_TAG("VirtualKeyboard");
	

	static VirtualKeyboard 		mInstance;
	static Activity 			mContextReference;
	
	static ViewGroup	   		m_viewGroup = null;
	static View            		m_lastFocus = null;
	
	public VirtualKeyboard(Activity context, ViewGroup viewGroup)
	{
		super(context);
		INFO(TAG, "[VirtualKeyboard]");
		
		// EditText Attributes
		setWidth(0);	
		setHeight(0);
    	setMaxHeight(0);
		setMaxWidth(0);

		this.setOnFocusChangeListener(this);
		
		
		// VirtualKeyboard attributes
		m_viewGroup       = viewGroup;
		mInstance         = this;
		mContextReference = context;
	}
	
	public static VirtualKeyboard getInstance() 
	{
		return mInstance;
	}
	
	
	//Options for ShowKeyboard are coming from c++ as integer values.
	//C++ enums for reference:
	
	// enum KeyboardType
	// {
		// KEYBOARD_UNDEFINED = 0,
		// KEYBOARD_NOKEYS,
		// KEYBOARD_QWERTY,
		// KEYBOARD_12KEY
	// };

	// enum EnterFunction
	// {
		// ACTION_DONE = 0,
		// ACTION_NEW_LINE,
	// };
	
	// enum FullscreenOption
	// {
		// NOT_FULLSCREEN = 0,
		// FULLSCREEN,
	// };
	
	//End Options
	
	public static void ShowKeyboard(String text, int kbType, int enterFunc, int fullscreen)
	{
		INFO(TAG, "[ShowKeyboard]");
		
		if(fullscreen == 0)
		{
			VirtualKeyboard.getInstance().setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN);
		}
		
		if(enterFunc == 0)
		{
			VirtualKeyboard.getInstance().setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEND);
		}
		
		ShowKeyboardInternal(VirtualKeyboard.getInstance(), m_viewGroup.findFocus(), text, kbType);			
	}

    public static void SetKeyboardText(final String text)
    {
        final VirtualKeyboard fdt = VirtualKeyboard.getInstance();

        try
		{
			mContextReference.runOnUiThread
            (
				new Runnable() 
				{ 
					public void run() 
					{
						if(isKeyboardVisible())
						{		
							INFO(TAG, "Virtual Keyboard Run On Ui Thread");
							fdt.setText(text);
							fdt.setSelection(fdt.getText().length());
						}
						else
						{
							android.util.Log.i("ACP_LOGGER", "Trying to set Keyboard text: '" + text + "' but keyboard is not visible.");
						}
					}
				}
			);
        }
        catch(Exception e)
        {
            ERR(TAG, "Exception: "+e);
        }   
    }   	

	private static void ShowKeyboardInternal(final VirtualKeyboard fdt, View focusedView, String withText, final int keyboardType)
	{
		final View     fview = focusedView;
		final String fstring = withText;
	
		try
		{
			mContextReference.runOnUiThread(
				new Runnable() 
				{ 
					public void run() 
					{
						if (!isKeyboardVisible())
						{
							INFO(TAG, "Virtual Keyboard Run On Ui Thread");
							fdt.setRawInputType(keyboardType);
							fdt.setText(fstring);
							fdt.setSelection(fdt.getText().length());
							fdt.m_lastFocus = fview;
							
							// Attach this to the group view
							m_viewGroup.addView(fdt, 0);
							//m_viewGroup.bringChildToFront(fview);//av, this is not needed and might cause a bug where the focus view covers other smaller views. 
							
							boolean success = fdt.requestFocus();
							if(!success)
							{
								INFO(TAG, "Virtual keyboard did not receive focus");
							}
							else
							{
								INFO(TAG, "Virtual keyboard requested and received focus");
							}
						}
						else
						{
							//nop
						}
					}
				}
			);
		}
		catch (Exception e) 
		{
			ERR(TAG, "Exception: "+e);
		}
	}
	
	public static void HideKeyboard()
	{
		INFO(TAG, "[HideKeyboard]");
		VirtualKeyboard.getInstance().HideVKeyboard();
	}
	
	public void HideVKeyboard()
	{
		try
		{
			mContextReference.runOnUiThread(
				new Runnable() 
				{ 
					public void run() 
					{
						if (isKeyboardVisible())
						{
							if (m_lastFocus != null)
							{
								m_lastFocus.requestFocus();
								m_viewGroup.removeView(mInstance);							
							}
							else
							{
								m_viewGroup.removeView(mInstance);
								INFO(TAG, "Last focus was null.");
							}
						}
						else
						{
							//no op
						}
					}
				}
			);
		}
		catch (Exception e) 
		{
			ERR(TAG, "Exception: "+e);
		}
	}
	
	public static boolean isKeyboardVisible()
	{
		INFO(TAG, "[IsKeyboardVisible]");
		
		boolean isVisible = (m_viewGroup.findFocus() == VirtualKeyboard.getInstance()) ? true : false;		
		DBG(TAG, "isVisible = "+isVisible);
		
		return isVisible;
	}
	
	public static String GetVirtualKeyboardText()
	{
		INFO(TAG, "[GetVirtualKeyboardText]");
		
		if (VirtualKeyboard.getInstance() != null)
			return VirtualKeyboard.getInstance().getText().toString();
		else
			return "";
	}
	
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) 
    {
		INFO(TAG, "[onTextChanged]");
		if(VirtualKeyboard.getInstance() != null && SUtils.getContext() != null && s.toString() != null)
		{
			DBG(TAG, "Text: " + s.toString());
			try
			{
				JNIBridge.NativeSendKeyboardData(s.toString());
			} 
			catch (Exception e) 
			{
				ERR(TAG, "Exception: "+e);
			}
		}
		super.onTextChanged(s, start, before, count);
    }
        
	@Override
	public void onFocusChange(View v, boolean hasFocus) 
    {
        INFO(TAG, "[onFocusChange]");
		DBG(TAG, "hasFocus = " + hasFocus);

		InputMethodManager mgr = (InputMethodManager) mContextReference.getSystemService(Context.INPUT_METHOD_SERVICE);
	    
		if (hasFocus)
		{
	    	mgr.showSoftInput(this, InputMethodManager.SHOW_FORCED);		 
		}
        else
        {
			INFO(TAG, "HideSoftFocus");
        	mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
			
			APP_PACKAGE.GLUtils.LowProfileListener.ActivateImmersiveMode(mContextReference);
        }
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) 
	{
		if(event.getAction() == KeyEvent.ACTION_UP)
		{
			if(KeyEvent.KEYCODE_BACK == event.getKeyCode() || KeyEvent.KEYCODE_ENTER == event.getKeyCode())
			{
				INFO(TAG, "[dispatchKeyEvent]");
				DBG(TAG, "KeyEvent.KEYCODE_BACK || KeyEvent.KEYCODE_ENTER");
				HideKeyboard();
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) 
	{
		if(event.getAction() == KeyEvent.ACTION_UP)
		{
			if(KeyEvent.KEYCODE_BACK == event.getKeyCode() || KeyEvent.KEYCODE_ENTER == event.getKeyCode())
			{
				INFO(TAG, "[dispatchKeyEvent]");
				DBG(TAG, "KeyEvent.KEYCODE_BACK || KeyEvent.KEYCODE_ENTER");
				HideKeyboard();
				return true;
			}
		}
		
	    return super.dispatchKeyEventPreIme(event);
	}
}
#endif