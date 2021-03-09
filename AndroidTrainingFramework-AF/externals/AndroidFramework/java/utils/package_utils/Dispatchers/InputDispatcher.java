package APP_PACKAGE.PackageUtils.Dispatchers;


import android.content.Context;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.KeyEvent;



import APP_PACKAGE.PackageUtils.JNIBridge;

#if USE_HID_CONTROLLER
import APP_PACKAGE.GLUtils.controller.StandardHIDController;
#endif //USE_HID_CONTROLLER


public class InputDispatcher
{
    public InputDispatcher()
    {
    }
	
	public void StartDispatcher(Context context)
	{
		
	}

    public boolean OnTouchEvent(final MotionEvent event)
    {
        // get pointer index from the event object
        int pointerIndex = event.getActionIndex();

        // get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        boolean retCode = false;

        switch (maskedAction) 
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: 
            {
                JNIBridge.NativeOnTouch(0, event.getX(pointerIndex), event.getY(pointerIndex), pointerId);
                retCode = true;
                break;
            }
            case MotionEvent.ACTION_MOVE: 
            { 
                for (int size = event.getPointerCount(), i = 0; i < size; i++)
                {
                    //av:
                    //this sends move events to all active pointers...
                    //so even if I have a pointer which stands still
                    //i will still be getting events on it
                    //todo: check if the event needs sending in jni...
                    JNIBridge.NativeOnTouch(1, event.getX(i), event.getY(i), event.getPointerId(i));
                }   
                
                retCode = true;
                break;  
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            {
                JNIBridge.NativeOnTouch(2, event.getX(pointerIndex), event.getY(pointerIndex), pointerId);
                retCode = true;
                break;    
            }
            case MotionEvent.ACTION_CANCEL: 
            {
                JNIBridge.NativeOnTouch(3, event.getX(pointerIndex), event.getY(pointerIndex), pointerId);
                retCode = true;
                break;    
            }
            default:
            {   
                retCode = false;
                break;
            }
        }
        

        return retCode;
    }

    public boolean OnGenericMotionEvent(MotionEvent event)
    {
		#if USE_HID_CONTROLLER
		if(StandardHIDController.HandleMotionEvent(event))//make sure you are calling this code first thing
        {
			return true;
        }
		#endif //USE_HID_CONTROLLER
		
        return false;
    }

    public boolean OnKeyDown(final int keyCode, KeyEvent event)
    {
		#if USE_HID_CONTROLLER
		if(StandardHIDController.HandleInputEventPressed(event))//make sure you are calling this code first thing
        {
			return true;
        }
		#endif //USE_HID_CONTROLLER
		
		// Handle the back-key and menu pressed
		if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) 
		{			
			JNIBridge.NativeKeyAction(keyCode, true);
			return true; // And consume the event
		}
		
        return false;
    }

    public boolean OnKeyUp(final int keyCode, KeyEvent event)
    {
		#if USE_HID_CONTROLLER
		if(StandardHIDController.HandleInputEventReleased(event))//make sure you are calling this code first thing
        {
			return true;
        }
		#endif //USE_HID_CONTROLLER
		
		// Handle the back-key and menu pressed
		if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU)
		{
			JNIBridge.NativeKeyAction(keyCode, false);
			return true; // And consume the event
		}
	
        return false;
    }
}


